
package org.headlessintrace.client.test.level2.connection;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

import org.headlessintrace.client.filter.ContiguousEventFilter;
import org.headlessintrace.client.DefaultFactory;
import org.headlessintrace.client.IntraceException;
import org.headlessintrace.client.connection.Callback;
import org.headlessintrace.client.connection.ConnectionException;
import org.headlessintrace.client.connection.ConnectionTimeout;
import org.headlessintrace.client.connection.DefaultCallback;
import org.headlessintrace.client.connection.DefaultConnectionList;
import org.headlessintrace.client.connection.HostPort;
import org.headlessintrace.client.connection.IConnection;
import org.headlessintrace.client.connection.IConnectionStateCallback;
import org.headlessintrace.client.connection.command.ClassInstrumentationCommand;
import org.headlessintrace.client.connection.command.IAgentCommand;
import org.headlessintrace.client.filter.IncludeAnyOfTheseEventsFilterExt;
import org.headlessintrace.client.filter.IncludeThisMethodFilterExt;
import org.headlessintrace.client.filter.UrlExtractor;
import org.headlessintrace.client.model.ITraceEvent;
import org.headlessintrace.client.model.ITraceEventParser;
import org.headlessintrace.client.model.ITraceEvent.EventType;
import org.headlessintrace.client.request.BadCompletedRequestListener;
import org.headlessintrace.client.request.ICompletedRequestCallback;
import org.headlessintrace.client.request.IRequest;
import org.headlessintrace.client.request.RequestConnection;
import org.headlessintrace.client.test.TestUtil;
import org.headlessintrace.client.test.level2.connection.lowLevel.ConnectionTestUtils;
import org.junit.Test;

import ca.odell.glazedlists.EventList;

/**
 * 95% of this test uses the approaches used in wuqiSpank / BackgroundSqlCollector.
 * @author erikostermueller
 *
 */
public class JdbcRequestTest {

	private CountDownLatch m_latch = null;
	boolean m_ynWait = false;
	private List<IRequest> m_completedRequests = new CopyOnWriteArrayList<IRequest>();
	
	@Test
	public void canDetectImplementor() throws Exception, IntraceException {
		
		ClassInstrumentationCommand cic = new ClassInstrumentationCommand();
		IAgentCommand commandArray[] = { };
		
		/**
		 *   F I L T E R
		 */
		ITraceEventParser parser = org.headlessintrace.client.DefaultFactory.getFactory().getEventParser();
		List<ITraceEvent> myCriteriaList = new ArrayList<ITraceEvent>();
		myCriteriaList.add(parser.createEvent("[15:41:05.294]:[97]:java.sql.Statement:execute: Arg: INSERT INTO Location (name, address) VALUES(?, ?)", 0) );
		myCriteriaList.add(parser.createEvent("[15:41:05.294]:[97]:java.sql.Statement:executeUpdate: Arg: INSERT INTO Location (name, address) VALUES(?, ?)", 0) );
		myCriteriaList.add(parser.createEvent("[15:41:05.294]:[97]:java.sql.Statement:executeQuery: Arg: select foo from bar", 0) );
		myCriteriaList.add(parser.createEvent("[15:41:05.294]:[97]:java.sql.Connection:prepareStatement: Arg: INSERT INTO Location (name, address) VALUES(?, ?)", 0));
		myCriteriaList.add(parser.createEvent("[15:41:05.294]:[97]:java.sql.Connection:prepareCall: Arg: INSERT INTO Location (name, address) VALUES(?, ?)", 0));
		
		ITraceEvent t3_requestCompletion = parser.createEvent("[15:47:00.999]:[203]:javax.servlet.http.HttpServlet:service: }:50", 0);
		myCriteriaList.add(t3_requestCompletion);
		IncludeAnyOfTheseEventsFilterExt filter = new IncludeAnyOfTheseEventsFilterExt(); 
		filter.setFilterCriteria(myCriteriaList);

		/**
		 *  C O N N E C T
		 */
		//
		RequestConnection requestConnection = new RequestConnection(1000);//but we'll override this 1000 in a minute, hold on.
		ICompletedRequestCallback requestCallback = new ICompletedRequestCallback() {
			

			@Override
			public void requestCompleted(IRequest events) {
				m_completedRequests.add(events);
			}
		};
		
		ContiguousEventFilter cef = new ContiguousEventFilter(requestCallback);
		
		cef.keepMethod("execute");
		cef.keepMethod("executeUpdate");
		cef.keepMethod("executeQuery");
		cef.keepMethod("prepareStatement");
		cef.keepMethod("prepareCall");
		cef.keepMethod("addBatch");

		requestConnection.setCompletedRequestCallback(new UrlExtractor(cef));
		m_latch = new CountDownLatch(1);
		DefaultCallback callback = new DefaultCallback() {
			@Override
			public void setProgress(Map<String, String> progress) {
				m_ynWait = true;
				String result = progress.get("NUM_PROGRESS_DONE");
				System.out.print(".");
				if (result!=null && result.equals("true"))
					m_latch.countDown();
			}
		};
		
		requestConnection.addCallback(callback);
		requestConnection.getTraceWriter().setTraceFilterExt(filter);
		IncludeThisMethodFilterExt methodFilter = new IncludeThisMethodFilterExt();
		methodFilter.setFilterCriteria(t3_requestCompletion);
		requestConnection.setRequestCompletionFilter(methodFilter);

		ITraceEvent t3_requestStart = parser.createEvent("[15:47:00.999]:[203]:javax.servlet.http.HttpServlet:service: {:50", 0);
		IncludeThisMethodFilterExt methodStartFilter = new IncludeThisMethodFilterExt();
		methodStartFilter.setFilterCriteria(t3_requestStart);
		requestConnection.setRequestStartFilter(methodStartFilter);
		
		System.out.println("!!b4 connection");
		HostPort hostPort = new HostPort(ConnectionTestUtils.DEFAULT_HOST_1,ConnectionTestUtils.WEB_REQUEST_INTRACE_PORT);
		
		boolean rc = requestConnection.connect(hostPort, commandArray);
		System.out.println("!!after connection");
		System.out.println("New RequestConnection[" + requestConnection.hashCode() + "] success [" + rc + "] callback [" + callback.getConnectState().toString() + "] ");
		
		if (rc) {
			//DefaultConnectionList.getSingleton().add(config.getInTraceAgent(), myCast);
			DefaultConnectionList.getSingleton().add(hostPort, requestConnection);
			System.out.println("x4 Waiting for agent to finish instrumentation");
			if (m_ynWait)
				m_latch.await();
			TestUtil.sendHttpGet("/test/helloExecuteQuery");
			/**
			 * Pause to collect some trace events from the Agent running on 9123.
			 */
			Thread.sleep(4*ConnectionTestUtils.EVENT_COLLECTION_TIME_MS);
			
			assertEquals("Since we executed a single web request, we're expecting a single InTrace 'request' ",1,m_completedRequests.size());
			IRequest r = m_completedRequests.get(0);
			assertNotNull("the single traced request shouldn't be null",r);
			assertNotNull("the single traced request shouldn't be null",r.getEvents());
			assertEquals("expecting a specific number of events", r.getEvents().size(),4);
			assertEquals(r.getEvents().get(0).getEventType(),EventType.ENTRY);
			assertEquals(r.getEvents().get(1).getEventType(),EventType.ARG);
			assertEquals(r.getEvents().get(2).getEventType(),EventType.RETURN);
			assertEquals(r.getEvents().get(3).getEventType(),EventType.EXIT);
				
			assertEquals(r.getEvents().get(0).getMethodName(),"executeQuery");
			assertEquals(r.getEvents().get(1).getMethodName(),"executeQuery");
			assertEquals(r.getEvents().get(2).getMethodName(),"executeQuery");
			assertEquals(r.getEvents().get(3).getMethodName(),"executeQuery");
			
		} else {
			System.out.println( "Error connecting");
		}
				
	
	}
	private void dispEvents(EventList<ITraceEvent> traceEvents) {
		for( ITraceEvent t : traceEvents) {
			System.out.println(" Event [" + t.getPackageAndClass()  + "#" + t.getMethodName() + "] type [" + t.getEventType() + "]");
		}
		
	}

}
