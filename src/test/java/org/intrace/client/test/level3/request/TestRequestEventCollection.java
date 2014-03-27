package org.intrace.client.test.level3.request;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.intrace.client.DefaultFactory;
import org.intrace.client.IntraceException;
import org.intrace.client.connection.ConnectState;
import org.intrace.client.connection.ConnectionException;
import org.intrace.client.connection.ConnectionTimeout;
import org.intrace.client.connection.HostPort;
import org.intrace.client.connection.command.ClassInstrumentationCommand;
import org.intrace.client.connection.command.IAgentCommand;
import org.intrace.client.filter.IncludeAnyOfTheseEventsFilterExt;
import org.intrace.client.model.ITraceEvent;
import org.intrace.client.model.ITraceEventParser;
import org.intrace.client.request.DefaultRequestSeparator;
import org.intrace.client.request.BadCompletedRequestListener;
import org.intrace.client.request.IRequest;
import org.intrace.client.request.RequestConnection;
import org.intrace.client.request.RequestWriter;
import org.intrace.client.test.TestConfig;
import org.intrace.client.test.level2.connection.lowLevel.ConnectionTestUtils;
import org.intrace.client.test.level2.connection.lowLevel.TestCallback;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * This test requires the "test.weba" web application to be running on localhost:8080
 * To start this web application, run this script:  $INTRACE_HOME/startTests.sh
 * @author erikostermueller
 *
 */
public class TestRequestEventCollection {
	public static final AtomicInteger eventCount = new AtomicInteger();
	private TestConfig m_testConfig = new TestConfig();
	private List<ITraceEvent> m_requestTraceEvents = null;
	/**
	 * Would like to use a closure instead of this, but closures don't exist yet.
	 */
	List<IRequest> m_completedRequests = new CopyOnWriteArrayList<IRequest>();
	
	@Before
	public void setup() {
		m_testConfig.setHttpExampleWebApp(new HostPort("localhost:8080"));
		m_testConfig.setInTraceAgentServer( new HostPort("localhost:9125") );
	}
	
	/**
	 * This method shows some inconsistencies in the API right now.
	 * Once the inTrace server agent can instrument just individual methods, these inconsistencies will go away.
	 * Right now instrumentation happens for just classes, and has no regard for method names.
	 * Here is the inconsistency in this code:  Filters essentially have to be created in two different places:
	 * 1) IConnection.connect() -- must pass in a command array.  The server agent gets this.
	 * 2) Must pass in the _client_ side filter:  	IConnection#getTraceWriter()#setTraceFilterExt(IEventFilter);
	 * 
	 * @throws BadCompletedRequestListener
	 * @throws IOException
	 * @throws InterruptedException 
	 * @throws ConnectionException 
	 * @throws IntraceException 
	 */
	@Test
	public void canCollectMultipleEventsForSingleServletRequest() throws BadCompletedRequestListener, IOException, InterruptedException, ConnectionException, IntraceException {
		ClassInstrumentationCommand cic = new ClassInstrumentationCommand();
		//An example of how to provide more configuration options
		//For the InTrace server agent:
		//cic.setIncludeClassRegEx(ConnectionTestUtils.TEST_WEB_CLASS_TO_INSTRUMENT + "|" + ConnectionTestUtils.TEST_WEB_REQUEST_COMPLETION_CLASS);
		//IAgentCommand commandArray[] = { cic };
		IAgentCommand commandArray[] = { };
		
		/**
		 *   F I L T E R
		 */
		ITraceEventParser parser = DefaultFactory.getFactory().getEventParser();
		List<ITraceEvent> myCriteriaList = new ArrayList<ITraceEvent>();
		ITraceEvent t1 = parser.createEvent("[15:41:05.294]:[97]:org.hsqldb.jdbc.jdbcConnection:prepareStatement: Arg: INSERT INTO Location (name, address) VALUES(?, ?)", 0);
		ITraceEvent t2 = parser.createEvent("[15:47:00.999]:[203]:example.webapp.servlet.HelloWorld:doGet: }:50", 0);
		myCriteriaList.add(t1);myCriteriaList.add(t2);
		IncludeAnyOfTheseEventsFilterExt filter = new IncludeAnyOfTheseEventsFilterExt(); 
		filter.setFilterCriteria(myCriteriaList);
		
		
		/**
		 *  C O N N E C T
		 */
		//FIFO style, only keep the most recent 100 requests, event though this test is only looking for 1.
		RequestConnection requestConnection = new RequestConnection(100);
		RequestWriter rw = (RequestWriter) requestConnection.getTraceWriter();
		DefaultRequestSeparator drs = (DefaultRequestSeparator) rw.getRequestSeparator();
		drs.eventCounter.set(0);//Reset the event count, for tracking/validation/test purposes.
		
		TestCallback testCallback = new TestCallback();
		requestConnection.addCallback(testCallback);
		requestConnection.getTraceWriter().setTraceFilterExt(filter);
		requestConnection.setRequestCompletionEvent(t2);
		requestConnection.connect(
				m_testConfig.getInTraceAgentServer().hostNameOrIpAddress,
				m_testConfig.getInTraceAgentServer().port, 
				commandArray);
		
	
		java.util.List<ConnectState> myMessages = testCallback.getConnectStates();
		assertTrue("Expected to see 1 or more connection status messages", myMessages.size() > 0);
		//System.out.println("myMessages [" + myMessages.toString() + "]");
		boolean ynLocateMessage = ConnectionTestUtils.locateMessage2( myMessages,ConnectState.CONNECTED.toString());
		Assert.assertTrue("did not receive a CONNECTED status message", ynLocateMessage );
		Assert.assertFalse(	
				"whoops...most recent message is DISCONNECTED...thought we were still connected.", 
				ConnectionTestUtils.mostRecentMessageIsDisconnect2(myMessages)
				);
		
		/**
		 *  W E B    R E Q U E S T
		 */
		String urlString = "http://"  + m_testConfig.getExampleWebApp().toString() + "/test/hello";
		URL url = new URL(urlString);
		URLConnection conn = url.openConnection();
		InputStream is = conn.getInputStream();		
	  	
		/**
		 * Pause to collect some trace events from the Agent running on 9125.
		 */
		Thread.sleep(2000);
		assertEquals("Didn't capture the right number of events. ", 11 ,drs.eventCounter.get() );
		
		requestConnection.disconnect();
		myMessages = testCallback.getConnectStates();
		ynLocateMessage = ConnectionTestUtils.locateMessage2( myMessages,ConnectState.DISCONNECTED.toString());
		assertTrue("did not receive a DISCONNECTED status message", ynLocateMessage );
		
		/**
		 *  V A L I D A T E    T R A C E
		 */
		RequestWriter requestWriter = (RequestWriter) requestConnection.getTraceWriter();
		ConcurrentHashMap<String, IRequest> mapInFlight = requestWriter.getRequestSeparator().getInFlightRequests();
		if (mapInFlight.size() > 0) {
			IRequest ire = mapInFlight.get(0);
			if (ire==null) {
				System.out.println("##################### Found a null, unprocessed event");
			} else {
				System.out.println("##################### Start debugging here:  [" + ire.toString() + "]");
			}
		}
		assertEquals("Just submitted several web requests, but found some were unprocessed.",0,mapInFlight.size());
		
		
		Queue<IRequest> requests = requestWriter.getCompletedRequestQueue();
		assertEquals("Was expecting one completed request from a web server, but dind't", 1, requests.size() );
		//System.out.println("Found these requests [" + requests.toString() + "]");
		IRequest myRequestEvents = requests.peek();
		assertEquals("Was expecting two SQL statements in this request", 10, myRequestEvents.getEvents().size());
		ITraceEvent event0 = myRequestEvents.getEvents().get(0);
		assertEquals("Couldn't not find the SQL statement in the first event from the web server", "INSERT INTO Location (name, address) VALUES(?, ?)", event0.getValue());
		ITraceEvent event1 = myRequestEvents.getEvents().get(1);
		assertEquals("Couldn't not find the SQL statement in the first event from the web server", "INSERT INTO Event (name, description, date, location) VALUES(?, ?, ?, ?)", event1.getValue());
		
	}

}
