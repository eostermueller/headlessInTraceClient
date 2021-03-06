package org.headlessintrace.client.test.level2.connection;

import static org.junit.Assert.*;

import org.headlessintrace.client.DefaultFactory;
import org.headlessintrace.client.connection.ConnectionException;
import org.headlessintrace.client.connection.DefaultConnection;
import org.headlessintrace.client.connection.ConnectionTimeout;
import org.headlessintrace.client.connection.DefaultConnectionList;
import org.headlessintrace.client.connection.HostPort;
import org.headlessintrace.client.connection.IConnection;
import org.headlessintrace.client.connection.command.ClassInstrumentationCommand;
import org.headlessintrace.client.connection.command.IAgentCommand;
import org.headlessintrace.client.model.ITraceEvent;
import org.headlessintrace.client.request.BadCompletedRequestListener;
import org.headlessintrace.client.test.level2.connection.lowLevel.ConnectionTestUtils;
import org.junit.Assert;
import org.junit.Test;

public class TestCollectionOfSpecificEvents {

	@Test
	public void canCollectUnParsedEventsFromExternalAgent() throws BadCompletedRequestListener {
		ClassInstrumentationCommand cic = new ClassInstrumentationCommand();
		cic.setIncludeClassRegEx(ConnectionTestUtils.TEST_CLASS_TO_INSTRUMENT_1);
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
			assertEquals("Expecting an event from a specific class", "FirstTraceExample", event.getClassName());
			assertEquals("Expecting an event from a specific package", "example", event.getPackageName());
			assertNotNull("Expecting a non-null value for the event's thread id, but got a null");
			
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
