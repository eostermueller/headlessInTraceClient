package org.headlessintrace.client.test.level3.request;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.headlessintrace.client.DefaultFactory;
import org.headlessintrace.client.IFactory;
import org.headlessintrace.client.IntraceException;
import org.headlessintrace.client.connection.ConnectState;
import org.headlessintrace.client.connection.ConnectionException;
import org.headlessintrace.client.connection.ConnectionTimeout;
import org.headlessintrace.client.connection.DefaultConnectionList;
import org.headlessintrace.client.connection.HostPort;
import org.headlessintrace.client.connection.IConnection;
import org.headlessintrace.client.connection.command.ClassInstrumentationCommand;
import org.headlessintrace.client.connection.command.IAgentCommand;
import org.headlessintrace.client.filter.IncludeAnyOfTheseEventsFilterExt;
import org.headlessintrace.client.filter.IncludeThisMethodFilterExt;
import org.headlessintrace.client.model.ITraceEvent;
import org.headlessintrace.client.model.ITraceEventParser;
import org.headlessintrace.client.request.BadCompletedRequestListener;
import org.headlessintrace.client.request.DefaultRequestSeparator;
import org.headlessintrace.client.request.IRequest;
import org.headlessintrace.client.request.RequestConnection;
import org.headlessintrace.client.request.RequestWriter;
import org.headlessintrace.client.test.TestConfig;
import org.headlessintrace.client.test.level2.connection.lowLevel.ConnectionTestUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.headlessintrace.client.connection.DefaultCallback;

/**
 * This test requires the "test.webapp" web application to be running on localhost:8080
 * to start this, run this script:  $INTRACE_HOME/bin/startTests.sh
 * This example also demonstrates how to override many intrace classes by
 * installing your subclass of DefaultFactory.
 * @author erikostermueller
 *
 */
public class TestMultiThreadedRequestEventCollection {
	public static final AtomicInteger eventCount = new AtomicInteger();
	private TestConfig m_testConfig = new TestConfig();
	private List<ITraceEvent> m_requestTraceEvents = null;
	/**
	 * Would like to use a closure instead of this, but closures don't exist yet.
	 */
	List<IRequest> m_completedRequests = new CopyOnWriteArrayList<IRequest>();
	protected IConnection m_preConfiguredRequestConnection;
	
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
	 * @throws ConnectionException 
	 * @throws InterruptedException 
	 * @throws IntraceException 
	 */
	@Test
	public void canCollectRequestEventsFromMultiThreadedProcess() throws BadCompletedRequestListener, IOException, ConnectionException, InterruptedException, IntraceException {
		//Currently, there is a multi-threaded bug that fails at these larger volumes:
//		final int iterations = 500;
//		final int numThreads = 50;
		// ...but works at these smaller ones:
		final int iterations = 20;
		final int numThreads = 2;
		final int numEventsPerIteration = 10;//We will use this to cap the number of events stored, to avoid memory leaks.  10 is much larger than the 2-3 events per each web request.

		/**
		 *   F I L T E R
		 */
		ITraceEventParser parser = DefaultFactory.getFactory().getEventParser();
		List<ITraceEvent> myCriteriaList = new ArrayList<ITraceEvent>();
		//This line helps us filter for any prepareStatement call, not just the one with this particular INSERT statement
		ITraceEvent t1 = parser.createEvent("[15:41:05.294]:[97]:org.hsqldb.jdbc.jdbcConnection:prepareStatement: Arg: INSERT INTO Location (name, address) VALUES(?, ?)", 0);
		ITraceEvent t2 = parser.createEvent("[15:47:00.909]:[203]:org.headlessintrace.test.webapp.servlet.HelloWorld:doGet: }:50", 0);
		ITraceEvent t3 = parser.createEvent("[15:47:00.909]:[203]:org.headlessintrace.test.webapp.servlet.HelloWorld:doGet: {:50", 0);
		myCriteriaList.add(t1);myCriteriaList.add(t2);myCriteriaList.add(t3);
		IncludeAnyOfTheseEventsFilterExt filter = new IncludeAnyOfTheseEventsFilterExt(); 
		filter.setFilterCriteria(myCriteriaList);
		m_preConfiguredRequestConnection = new RequestConnection(iterations*numThreads*numEventsPerIteration);

		m_preConfiguredRequestConnection.getTraceWriter().setTraceFilterExt(filter);
		RequestConnection r = (RequestConnection) m_preConfiguredRequestConnection;
		//r.setRequestCompletionEvent(t2);
		IncludeThisMethodFilterExt completionMethodFilter = new IncludeThisMethodFilterExt();
		completionMethodFilter.setFilterCriteria(t2);
		r.setRequestCompletionFilter(completionMethodFilter);
		
		IncludeThisMethodFilterExt startMethodFilter = new IncludeThisMethodFilterExt();
		startMethodFilter.setFilterCriteria(t3);
		r.setRequestStartFilter(startMethodFilter);
		RequestWriter requestWriter = (RequestWriter) m_preConfiguredRequestConnection.getTraceWriter();
		
		//Now, we're telling our factory to return our pre-configured connection 
		//object to anyone (in this case, we're referring to DefaultConnectionList) calling IFactory.getDormantConnection().
		IFactory testFactory = new DefaultFactory() {
			@Override
			public IConnection getDormantConnection() {
				return m_preConfiguredRequestConnection;
			}
		};
		DefaultFactory.setFactory(testFactory);
		
		m_completedRequests.clear();
		ClassInstrumentationCommand cic = new ClassInstrumentationCommand();
		IAgentCommand commandArray[] = { };

		/**
		 *  C O N N E C T
		 */
		DefaultRequestSeparator defaultRequestSeparator = (DefaultRequestSeparator) requestWriter.getRequestSeparator();
		defaultRequestSeparator.eventCounter.set(0);//Reset the event count, for tracking/validation/test purposes.

		DefaultCallback callback = new DefaultCallback();
		IConnection requestConnection = DefaultConnectionList.getSingleton().connect(callback, m_testConfig.getInTraceAgentServer(), commandArray);
		assertTrue("Didn't not connect successfully. [" + DefaultFactory.getFactory().getMessages().getTestSetupInstructions() + "]", requestConnection.isConnected());

		java.util.List<ConnectState> myMessages = callback.getConnectStates();
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
	    CountDownLatch startSignal = new CountDownLatch(1);
	    CountDownLatch stopSignal = new CountDownLatch(numThreads);
	
		for (int i = 0; i < numThreads; i++) {
			new Thread( new HttpRequestor(startSignal, stopSignal, urlString, iterations)).start();
		}
		startSignal.countDown();//All threads begin processing
		/**
		 * Pause to collect some trace events from the Agent running on 9125.
		 */
		try {
			stopSignal.await();//After this, all threads are finished submitting.  Now wait for InTrace agent to trigger events on 9125
			Thread.sleep(1000/*ConnectionTestUtils.EVENT_COLLECTION_TIME_MS*/);
		} catch (InterruptedException ie) {
			fail("ThreadInterruptedExcpetion while collecting event data");
		}
		
		/**
		 *  V A L I D A T E    T R A C E
		 */
		//System.out.println("Was expecting [" + (iterations * numThreads * numEventsPerIteration) + "] events.  Actual number of events [" + defaultRequestSeparator.eventCounter.get() + "]");
		assertEquals("Didn't capture the right number of events. ", (iterations * numThreads * 3) ,defaultRequestSeparator.eventCounter.get() );
		
		requestWriter = (RequestWriter) requestConnection.getTraceWriter();
		Queue<IRequest> requests = requestWriter.getCompletedRequestQueue();
		assertEquals("Did not get the right number of multi-threaded requests to a web server", numThreads*iterations, requests.size() );
		//System.out.println("Expecting [" + numThreads*iterations + "] requests.  Actual requests["+ requests.size()+ "]");
		//System.out.println("Found these requests [" + requests.toString() + "]");
		IRequest myRequestEvents = requests.peek();
		assertEquals("Was expecting two SQL statements in this request", 2, myRequestEvents.getEvents().size());
		ITraceEvent event0 = myRequestEvents.getEvents().get(0);
		assertEquals("Couldn't not find the SQL statement in the first event from the web server", "INSERT INTO Location (name, address) VALUES(?, ?)", event0.getValue());
		ITraceEvent event1 = myRequestEvents.getEvents().get(1);
		assertEquals("Couldn't not find the SQL statement in the first event from the web server", "INSERT INTO Event (name, description, date, location) VALUES(?, ?, ?, ?)", event1.getValue());

		//################ START
		
		ConcurrentHashMap<String, IRequest> mapInFlight = requestWriter.getRequestSeparator().getInFlightRequests();
		assertEquals("Just submitted several web requests, but found some were unprocessed.",0,mapInFlight.size());
		for(IRequest request : requests) {
			if (request==null) {
				fail("Shouldn't have any nulls here.");
			} else {
				//System.out.println("#%#% Found non-null item in hash map");
				List<ITraceEvent> endEvents = request.getEvents();
				for(ITraceEvent x : endEvents) {
					//System.out.println("parent thread [" + request.getThreadId() + "] simulated thread id [" + x.getThreadId() + "] Event [" + x.getValue() + "]");
				}
				assertEquals("Expected 2 events for each request", 2, endEvents.size());
			}
		}
		
		//################ STOP
		requestConnection.disconnect();
		myMessages = callback.getConnectStates();
		ynLocateMessage = ConnectionTestUtils.locateMessage2( myMessages,ConnectState.DISCONNECTED.toString());
		//Why an NPE here?  System.out.println("myMessages [" + myMessages.toString() + "]");
		Assert.assertTrue("did not receive a DISCONNECTED status message", ynLocateMessage );
	}

}
/**
 * This class hits the same web page over and over again in a single thread.
 * Each hit triggers a servlet in a separate JVM, which in turn
 * fires InTrace events which are captured/validated by the above test case.
 * @author erikostermueller
 *
 */
class HttpRequestor implements Runnable {
	private String m_url = null;
	private int m_iterations = -1;
	CountDownLatch m_startLatch = null;
	CountDownLatch m_stopLatch = null;
	public HttpRequestor(CountDownLatch startLatch, CountDownLatch stopLatch, String url, int iternations) {
		m_startLatch = startLatch;
		m_stopLatch = stopLatch;
		m_url = url;
		m_iterations = iternations;
	}
    public void run() {
		URL url;
		
		try {
			m_startLatch.await();
			for(int i = 0; i < m_iterations; i++) {

				url = new URL(m_url);
				URLConnection conn = url.openConnection();
				InputStream is = conn.getInputStream();		
			}
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			m_stopLatch.countDown();
		}
   	
    }
}