package org.headlessintrace.client.test.level2.connection;

import static org.junit.Assert.*;

import org.headlessintrace.client.DefaultFactory;
import org.headlessintrace.client.connection.Callback;
import org.headlessintrace.client.connection.ConnectionException;
import org.headlessintrace.client.connection.DefaultConnection;
import org.headlessintrace.client.connection.ConnectionTimeout;
import org.headlessintrace.client.connection.DefaultConnectionList;
import org.headlessintrace.client.connection.HostPort;
import org.headlessintrace.client.connection.IConnection;
import org.headlessintrace.client.connection.command.ClassInstrumentationCommand;
import org.headlessintrace.client.connection.command.IAgentCommand;
import org.headlessintrace.client.request.BadCompletedRequestListener;
import org.headlessintrace.client.test.level2.connection.lowLevel.ConnectionTestUtils;
import org.junit.Assert;
import org.junit.Test;

public class TestEventCollection {

	@Test
	public void canCollectUnParsedEventsFromExternalAgent() throws BadCompletedRequestListener {
		ClassInstrumentationCommand cic = new ClassInstrumentationCommand();
		cic.setIncludeClassRegEx(ConnectionTestUtils.TEST_CLASS_TO_INSTRUMENT_1);
		IAgentCommand commandArray[] = { cic };
		
		/**
		 *  C O N N E C T
		 */
		IConnection c = null;
		HostPort hostPort = new HostPort(ConnectionTestUtils.DEFAULT_HOST_1,ConnectionTestUtils.DEFAULT_PORT_1_INT);
		org.headlessintrace.client.connection.Callback callback = new Callback();
		try {
			c = DefaultConnectionList.getSingleton().connect(callback, hostPort, commandArray);
			assertNotNull("Is the test program started?  Didn't get a connection. [" + DefaultFactory.getFactory().getMessages().getTestSetupInstructions() + "]");
			assertEquals("Didn't not connect successfully. [" + DefaultFactory.getFactory().getMessages().getTestSetupInstructions() + "]", true, c.isConnected());
			
			/**
			 * Pause to collect some trace events from the Agent running on 9123.
			 */
			Thread.sleep(ConnectionTestUtils.EVENT_COLLECTION_TIME_MS);			
			
			long traceEventCount = c.getTraceEvents().size();
			System.out.println("Found [" + traceEventCount + "] events.");
			assertTrue("Collected fewer than 50 trace events", traceEventCount > 50);
			
		} catch (ConnectionTimeout e) {
			e.printStackTrace();
			fail("Received a connection timeout.  Is the test program example.FirstTraceExample running? [" + e.getMessage() + "]");
		} catch (ConnectionException e) {
			e.printStackTrace();
			fail("Unknown connection error [" + e.getMessage() + "]");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			c.disconnect();
		}
		
	
	}

}
