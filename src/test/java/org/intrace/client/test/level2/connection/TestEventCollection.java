package org.intrace.client.test.level2.connection;

import static org.junit.Assert.*;

import org.intrace.client.DefaultFactory;
import org.intrace.client.connection.ConnectionException;
import org.intrace.client.connection.DefaultConnection;
import org.intrace.client.connection.ConnectionTimeout;
import org.intrace.client.connection.DefaultConnectionList;
import org.intrace.client.connection.HostPort;
import org.intrace.client.connection.IConnection;
import org.intrace.client.connection.command.ClassInstrumentationCommand;
import org.intrace.client.connection.command.IAgentCommand;
import org.intrace.client.request.BadCompletedRequestListener;
import org.intrace.client.test.level2.connection.lowLevel.ConnectionTestUtils;
import org.intrace.client.test.level2.connection.lowLevel.TestCallback;
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
		TestCallback testCallback = new TestCallback();
		try {
			c = DefaultConnectionList.getSingleton().connect(testCallback, hostPort, commandArray);
			assertNotNull("Is the test program started?  Didn't get a connection. [" + DefaultFactory.getFactory().getMessages().getTestSetupInstructions() + "]");
			assertEquals("Didn't not connect successfully. [" + DefaultFactory.getFactory().getMessages().getTestSetupInstructions() + "]", true, c.isConnected());
			
			/**
			 * Pause to collect some trace events from the Agent running on 9123.
			 */
			Thread.sleep(ConnectionTestUtils.EVENT_COLLECTION_TIME_MS);			
			
			long traceEventCount = c.getTraceEvents().size();
			System.out.println("Found [" + traceEventCount + "] events.");
			assertTrue("Collected fewer than 50 trace events", traceEventCount > 50);
			
			c.disconnect();
		} catch (ConnectionTimeout e) {
			e.printStackTrace();
			fail("Received a connection timeout.  Is the test program example.FirstTraceExample running? [" + e.getMessage() + "]");
		} catch (ConnectionException e) {
			e.printStackTrace();
			fail("Unknown connection error [" + e.getMessage() + "]");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	
	}

}
