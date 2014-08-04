package org.intrace.client.test.level3.request;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.intrace.client.DefaultFactory;
import org.intrace.client.IntraceException;
import org.intrace.client.connection.Callback;
import org.intrace.client.connection.ConnectState;
import org.intrace.client.connection.ConnectionException;
import org.intrace.client.connection.ConnectionTimeout;
import org.intrace.client.connection.DefaultCallback;
import org.intrace.client.connection.HostPort;
import org.intrace.client.connection.command.ClassInstrumentationCommand;
import org.intrace.client.connection.command.IAgentCommand;
import org.intrace.client.filter.IncludeAnyOfTheseEventsFilterExt;
import org.intrace.client.filter.IncludeAnyOfTheseMethodsFilterExt;
import org.intrace.client.filter.IncludeThisMethodFilterExt;
import org.intrace.client.model.ITraceEvent;
import org.intrace.client.model.ITraceEventParser;
import org.intrace.client.request.DefaultRequestSeparator;
import org.intrace.client.request.BadCompletedRequestListener;
import org.intrace.client.request.IRequest;
import org.intrace.client.request.RequestConnection;
import org.intrace.client.request.RequestWriter;
import org.intrace.client.test.TestConfig;
import org.intrace.client.test.level2.connection.lowLevel.ConnectionTestUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * This test requires the "test.weba" web application to be running on localhost:8080
 * To start this web application, run this script:  $INTRACE_HOME/startTests.sh
 * @author erikostermueller
 *
 */
public class TestRequestEventCollectionWithInterfaces {
	public static final AtomicInteger eventCount = new AtomicInteger();
	private TestConfig m_testConfig = new TestConfig();
	private List<ITraceEvent> m_requestTraceEvents = null;
	/**
	 * Would like to use a closure instead of this, but closures don't exist yet.
	 */
	List<IRequest> m_completedRequests = new CopyOnWriteArrayList<IRequest>();
	private CountDownLatch m_latch;
	protected boolean m_waitForInstrumentation= false;
	
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
		ITraceEvent t1 = parser.createEvent("[15:41:05.294]:[97]:java.sql.Connection:prepareStatement: Arg: INSERT INTO Location (name, address) VALUES(?, ?)", 0);
		//ITraceEvent t2 = parser.createEvent("[15:47:00.999]:[203]:javax.servlet.http.HttpServlet:doGet: }:50", 0);
		ITraceEvent t3 = parser.createEvent("[15:47:00.999]:[203]:javax.servlet.http.HttpServlet:service: {:50", 0);
		ITraceEvent t2 = parser.createEvent("[15:47:00.999]:[203]:javax.servlet.http.HttpServlet:service: }:50", 0);
		
		myCriteriaList.add(t1);myCriteriaList.add(t2);myCriteriaList.add(t3);
		IncludeAnyOfTheseMethodsFilterExt filter = new IncludeAnyOfTheseMethodsFilterExt(); 
		filter.setFilterCriteria(myCriteriaList);
		
		/**
		 *  C O N N E C T
		 */
		//FIFO style, only keep the most recent 100 requests, even though this test is only looking for 1.
		RequestConnection requestConnection = new RequestConnection(100);
		RequestWriter rw = (RequestWriter) requestConnection.getTraceWriter();
		DefaultRequestSeparator drs = (DefaultRequestSeparator) rw.getRequestSeparator();
		drs.eventCounter.set(0);//Reset the event count, for tracking/validation/test purposes.
		
		//org.intrace.client.connection.Callback testCallback = new Callback();
		m_latch = new CountDownLatch(1);
		DefaultCallback callback = new DefaultCallback() {
			@Override
			public void setProgress(Map<String, String> progress) {
				m_waitForInstrumentation = true;
				String result = progress.get("NUM_PROGRESS_DONE");
				System.out.print("."); // kinda a live status update, filling screen with dots.
				if (result!=null && result.equals("true"))
					m_latch.countDown();
			}
		};
		
		requestConnection.addCallback(callback);
		requestConnection.getTraceWriter().setTraceFilterExt(filter);
		//requestConnection.setRequestCompletionEvent(t2);

		IncludeThisMethodFilterExt completionMethodFilter = new IncludeThisMethodFilterExt();
		completionMethodFilter.setFilterCriteria(t2);
		requestConnection.setRequestCompletionFilter(completionMethodFilter);
		
		IncludeThisMethodFilterExt startMethodFilter = new IncludeThisMethodFilterExt();
		startMethodFilter.setFilterCriteria(t3);
		requestConnection.setRequestStartFilter(startMethodFilter);

		requestConnection.connect(
				m_testConfig.getInTraceAgentServer().hostNameOrIpAddress,
				m_testConfig.getInTraceAgentServer().port, 
				commandArray);
	
		java.util.List<ConnectState> myMessages = callback.getConnectStates();
		assertTrue("Expected to see 1 or more connection status messages", myMessages.size() > 0);
		//System.out.println("myMessages [" + myMessages.toString() + "]");
		boolean ynLocateMessage = ConnectionTestUtils.locateMessage2( myMessages,ConnectState.CONNECTED.toString());
		Assert.assertTrue("did not receive a CONNECTED status message", ynLocateMessage );
		Assert.assertFalse(	
				"whoops...most recent message is DISCONNECTED...thought we were still connected.", 
				ConnectionTestUtils.mostRecentMessageIsDisconnect2(myMessages)
				);
		
		if (m_waitForInstrumentation)
			m_latch.await();
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
		//assertEquals("Didn't capture the right number of events. ", 11 ,drs.eventCounter.get() );
		
		//When just filtering on method name, that opens up the field to 22 events instead of 11 when exact class name is matched.
		assertEquals("Didn't capture the right number of events. ", 24 ,drs.eventCounter.get() );
		
		requestConnection.disconnect();
		myMessages = callback.getConnectStates();
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
		assertEquals(20, myRequestEvents.getEvents().size());
		/**
		 * The following demonstrates one problem (a fixable one) with this API.
		 * Only 10 SQL statements were generated, but 20 events get fired.
		 * This is because the jdbc driver is wrapped by some other class
		 * that also implements some of the java.sql interfaces.
		 * org.intrace.client.filter.ContiguousEventFilter fixes this.
		 * JdbcRequestTest demonstrates how to use this.
		 */
		boolean ynFoundFirstSql = false;
		boolean ynFoundSecondSql = false;
		for (ITraceEvent e : myRequestEvents.getEvents()) {
			if (e.getValue()!= null && e.getValue().equals("INSERT INTO Location (name, address) VALUES(?, ?)"))
				ynFoundFirstSql = true;
			if (e.getValue()!= null && e.getValue().equals("INSERT INTO Event (name, description, date, location) VALUES(?, ?, ?, ?)"))
				ynFoundSecondSql = true;
		}

		assertTrue("Surely the first INSERT must be in at least one of these events",ynFoundFirstSql);
		assertTrue("Surely the second INSERT must be in at least one of these events",ynFoundSecondSql);
		
		
	}

}
