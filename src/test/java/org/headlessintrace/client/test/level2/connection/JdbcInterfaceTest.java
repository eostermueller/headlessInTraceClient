package org.headlessintrace.client.test.level2.connection;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.headlessintrace.client.DefaultFactory;
import org.headlessintrace.client.connection.ConnectionException;
import org.headlessintrace.client.connection.ConnectionTimeout;
import org.headlessintrace.client.connection.DefaultCallback;
import org.headlessintrace.client.connection.DefaultConnectionList;
import org.headlessintrace.client.connection.HostPort;
import org.headlessintrace.client.connection.IConnection;
import org.headlessintrace.client.connection.command.ClassInstrumentationCommand;
import org.headlessintrace.client.connection.command.IAgentCommand;
import org.headlessintrace.client.model.ITraceEvent;
import org.headlessintrace.client.request.BadCompletedRequestListener;
import org.headlessintrace.client.test.TestUtil;
import org.headlessintrace.client.test.level2.connection.lowLevel.ConnectionTestUtils;
import org.junit.Ignore;
import org.junit.Test;

import ca.odell.glazedlists.EventList;

/**
 * This test casts a wide net and collects a number of events.
 * Little effort is made to winnow these events down to a small set.
 * Little validation is done on the result set.
 * See JdbcRequetTest for a test that targets a small, precise set of events.
 * @author erikostermueller
 *
 */
public class JdbcInterfaceTest {

	private CountDownLatch latch = null;
	boolean m_ynWait = false;
	
	@Test
	public void canDetectImplementor() throws Exception {
		
		ClassInstrumentationCommand cic = new ClassInstrumentationCommand();
		cic.setIncludeClassRegEx("java.sql.Statement");
		IAgentCommand commandArray[] = { cic };
		HostPort hostPort = new HostPort(ConnectionTestUtils.DEFAULT_HOST_1,ConnectionTestUtils.WEB_REQUEST_INTRACE_PORT);
		
		//Test will fail unless we wait for instrumentation to complete.
		IConnection c = null;
		try {
			latch = new CountDownLatch(1);
			DefaultCallback callback = new DefaultCallback() {
				@Override
				public void setProgress(Map<String, String> progress) {
					m_ynWait = true;
					String result = progress.get("NUM_PROGRESS_DONE");
					System.out.print(".");
					if (result!=null && result.equals("true"))
						latch.countDown();
				}
			};
			/**
			 *  C O N N E C T
			 */
			
			c = DefaultConnectionList.getSingleton().connect(callback, hostPort, commandArray);
			//c = DefaultConnectionList.getSingleton().connect(null, hostPort, null); // may 15 2014
			System.out.println("x1 ####@after connect");
			System.out.println("x2 Connection [" + c.toString() + "]\n");
			//boolean ynWait = false;
			String classNames[] = c.getModifiedClasses();
			if (classNames==null || classNames.length==0) {
				throw new Exception("instrumented classes not set correctly");
			} else {
				boolean ynFound = false;
				for(String myClassName : classNames) {
					if (myClassName.equals("java.sql.Statement"))
						ynFound = true;
				}
				if (ynFound==false)
					throw new Exception("agent not configure with java.sql.Statement");
			}
			System.out.println("x3 just sent cmd array");
			
			assertNotNull("Is the test program started?  Didn't get a connection. [" + DefaultFactory.getFactory().getMessages().getTestSetupInstructions() + "]");
			assertEquals("Didn't not connect successfully. [" + DefaultFactory.getFactory().getMessages().getTestSetupInstructions() + "]", true, c.isConnected());
			
			//Thread.sleep(120000);
			System.out.println("x4 Waiting for agent to finish instrumentation");
			if (m_ynWait)
				latch.await();
			TestUtil.sendHttpGet("/test/helloExecuteQuery");
			/**
			 * Pause to collect some trace events from the Agent running on 9123.
			 */
			Thread.sleep(4*ConnectionTestUtils.EVENT_COLLECTION_TIME_MS);
			System.out.println("x5 ####%instrumentation finished");
			System.out.println("x6 Connection [" + c.toString() + "]\n");
			/**
			 *  V A L I D A T E
			 *  T R A C E
			 *  E V E N T S
			 */
			dispEvents(c.getTraceEvents());
			int traceEventCount = c.getTraceEvents().size();
			//System.out.println("Found [" + traceEventCount + "] events.");
			assertEquals("Didn't find exepected number of java.sql.PreparedStatement events", 71, traceEventCount);
			ITraceEvent event = c.getTraceEvents().get(0);
//			assertEquals("Expecting an event from a specific class", TEST_IMPLEMENTOR_1, event.getClassName());
//			assertEquals("Expecting an event from a specific package", TEST_PACKAGE, event.getPackageName());
//			assertNotNull("Expecting a non-null value for the event's thread id, but got a null", event.getThreadId());
			
			try {
				int intThreadId = Integer.parseInt(event.getThreadId());
				assertTrue("Expecting an integer > 0 for the thread id", intThreadId > 0);
			} catch (NumberFormatException e) {
				fail("Expected the thread id to be an integer, but instead got [" + event.getThreadId() + "]");
			}
			
			c.disconnect();

		} catch (ConnectionTimeout e) {
			e.printStackTrace();
			fail("Received a connection timeout.  Is the test program example.FirstTraceExample running? [" + e.getMessage() + "]");
		} catch (ConnectionException e) {
			e.printStackTrace();
			fail("Unknown connection error [" + e.getMessage() + "]");
		} catch (BadCompletedRequestListener e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} finally {
			latch.countDown();
			if (c!=null) {
				c.disconnect();
				System.out.println(" Connection list [" + DefaultConnectionList.getSingleton().size() + "]");
				System.out.println("@@ 1 @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
				System.out.println(" Connection [" + c.toString() + "]\n");
				System.out.println("** 2 *********************************************");
			}
		}
		
	
	}
	private void dispEvents(EventList<ITraceEvent> traceEvents) {
		for( ITraceEvent t : traceEvents) {
			System.out.println(" Event [" + t.getPackageAndClass()  + "#" + t.getMethodName() + "] type [" + t.getEventType() + "]");
		}
		
	}

}
