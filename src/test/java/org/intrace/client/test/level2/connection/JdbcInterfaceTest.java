package org.intrace.client.test.level2.connection;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.intrace.client.DefaultFactory;
import org.intrace.client.connection.ConnectionException;
import org.intrace.client.connection.ConnectionTimeout;
import org.intrace.client.connection.DefaultCallback;
import org.intrace.client.connection.DefaultConnectionList;
import org.intrace.client.connection.HostPort;
import org.intrace.client.connection.IConnection;
import org.intrace.client.connection.command.ClassInstrumentationCommand;
import org.intrace.client.connection.command.IAgentCommand;
import org.intrace.client.model.ITraceEvent;
import org.intrace.client.request.BadCompletedRequestListener;
import org.intrace.client.test.TestUtil;
import org.intrace.client.test.level2.connection.lowLevel.ConnectionTestUtils;
import org.junit.Test;

import ca.odell.glazedlists.EventList;

public class JdbcInterfaceTest {

	private static final String TEST_INTERFACE = "MyTestInterface";
	private static final String TEST_IMPLEMENTOR_1 = "MyFirstTestImplementor";
	private static final String TEST_IMPLEMENTOR_2 = "MySecondTestImplementor";
	private static final String TEST_PACKAGE = "example";
	private CountDownLatch latch = null;
	
//	@Test
	public void canConnectAndDisconnect() {
		IAgentCommand commandArray[] = {  };
		HostPort hostPort = new HostPort(ConnectionTestUtils.DEFAULT_HOST_1,ConnectionTestUtils.WEB_REQUEST_INTRACE_PORT);
		
		//Test will fail unless we wait for instrumentation to complete.
		IConnection c = null;
		try {
			/**
			 *  C O N N E C T
			 */
			
			c = DefaultConnectionList.getSingleton().connect(null, hostPort, commandArray);
			System.out.println("####@after connect");
			System.out.println(" Connection [" + c.toString() + "]\n");
			
			
			assertNotNull("Is the test program started?  Didn't get a connection. [" + DefaultFactory.getFactory().getMessages().getTestSetupInstructions() + "]");
			assertEquals("Didn't not connect successfully. [" + DefaultFactory.getFactory().getMessages().getTestSetupInstructions() + "]", true, c.isConnected());
			
			System.out.println("Waiting for agent to finish instrumentation");
			System.out.println("####%instrumentation finished");
			System.out.println(" Connection [" + c.toString() + "]\n");
			String rc[] = c.getModifiedClasses();
			System.out.println("Modified classes [" + rc + "]");
			assertEquals("unable to query for modified classes. ", "java.sql.Statement", rc);

			
			
	        //controlThread.sendMessage("[listmodifiedclasses");
	        //String modifiedClasses = controlThread.getMessage();			
			
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
		} finally {
			if (c!=null) {
				c.disconnect();
				System.out.println(" Connection list [" + DefaultConnectionList.getSingleton().size() + "]");
				System.out.println("@@ 1 @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
				System.out.println(" Connection [" + c.toString() + "]\n");
				System.out.println("** 2 *********************************************");
			}
		}
		
	}
	@Test
	public void canDetectImplementor() {
		
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
					String result = progress.get("NUM_PROGRESS_DONE");
					System.out.println("Found [" + result + "]");
					if (result!=null && result.equals("true"))
						latch.countDown();
				}
			};
			/**
			 *  C O N N E C T
			 */
			
			
			c = DefaultConnectionList.getSingleton().connect(callback, hostPort, null);
			//c = DefaultConnectionList.getSingleton().connect(null, hostPort, null); // may 15 2014
			System.out.println("x1 ####@after connect");
			System.out.println("x2 Connection [" + c.toString() + "]\n");
			boolean ynWait = false;
			String classNames[] = c.getModifiedClasses();
			if (classNames==null || classNames.length==0) {
				c.setCommandArray(commandArray);
				c.executeStartupCommands();
				ynWait = true;
			}
			System.out.println("x3 just sent cmd array");
			
			assertNotNull("Is the test program started?  Didn't get a connection. [" + DefaultFactory.getFactory().getMessages().getTestSetupInstructions() + "]");
			assertEquals("Didn't not connect successfully. [" + DefaultFactory.getFactory().getMessages().getTestSetupInstructions() + "]", true, c.isConnected());
			
			//Thread.sleep(120000);
			System.out.println("x4 Waiting for agent to finish instrumentation");
			if (ynWait)
				latch.await();
			TestUtil.sendHttpGet("/test/helloExecuteQuery");
			/**
			 * Pause to collect some trace events from the Agent running on 9123.
			 */
			Thread.sleep(ConnectionTestUtils.EVENT_COLLECTION_TIME_MS);
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
