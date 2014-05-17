package org.intrace.client.test.level2.connection;

import static org.junit.Assert.*;

import org.intrace.client.DefaultFactory;
import org.intrace.client.connection.ConnectionException;
import org.intrace.client.connection.ConnectionTimeout;
import org.intrace.client.connection.DefaultConnectionList;
import org.intrace.client.connection.HostPort;
import org.intrace.client.connection.IConnection;
import org.intrace.client.connection.command.ClassInstrumentationCommand;
import org.intrace.client.connection.command.IAgentCommand;
import org.intrace.client.model.ITraceEvent;
import org.intrace.client.request.BadCompletedRequestListener;
import org.intrace.client.test.level2.connection.lowLevel.ConnectionTestUtils;
import org.junit.Test;

public class InterfaceTracingTest {

	private static final String TEST_INTERFACE = "MyTestInterface";
	private static final String TEST_IMPLEMENTOR_1 = "MyFirstTestImplementor";
	private static final String TEST_IMPLEMENTOR_2 = "MySecondTestImplementor";
	private static final String TEST_PACKAGE = "example";
	@Test
	public void canDetectImplementor() throws BadCompletedRequestListener {
		
		ClassInstrumentationCommand cic = new ClassInstrumentationCommand();
		//cic.setIncludeClassRegEx(ConnectionTestUtils.TEST_CLASS_TO_INSTRUMENT_1);
		cic.setIncludeClassRegEx(TEST_PACKAGE + "." + TEST_INTERFACE);
		IAgentCommand commandArray[] = { cic };
		HostPort hostPort = new HostPort(ConnectionTestUtils.DEFAULT_HOST_1,ConnectionTestUtils.DEFAULT_PORT_1_INT);
		
		try {

			/**
			 *  C O N N E C T
			 */
			IConnection c = DefaultConnectionList.getSingleton().connect(null, hostPort, commandArray);
			assertNotNull("Is the test program started?  Didn't get a connection. [" + DefaultFactory.getFactory().getMessages().getTestSetupInstructions() + "]");
			assertEquals("Didn't not connect successfully. [" + DefaultFactory.getFactory().getMessages().getTestSetupInstructions() + "]", true, c.isConnected());
			/**
			 * Pause to collect some trace events from the Agent running on 9123.
			 */
			Thread.sleep(ConnectionTestUtils.EVENT_COLLECTION_TIME_MS);			
			
			/**
			 *  V A L I D A T E
			 *  T R A C E
			 *  E V E N T S
			 */
			long traceEventCount = c.getTraceEvents().size();
			//System.out.println("Found [" + traceEventCount + "] events.");
			assertTrue("Collected fewer than 50 trace events", traceEventCount > 50);
			
			ITraceEvent event = c.getTraceEvents().get(0);
			assertEquals("Expecting an event from a specific class", TEST_IMPLEMENTOR_1, event.getClassName());
			assertEquals("Expecting an event from a specific package", TEST_PACKAGE, event.getPackageName());
			assertNotNull("Expecting a non-null value for the event's thread id, but got a null", event.getThreadId());
			
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
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	
	}

}
