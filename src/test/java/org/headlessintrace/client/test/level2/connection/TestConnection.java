package org.headlessintrace.client.test.level2.connection;

import static org.junit.Assert.*;


import java.awt.List;

import org.headlessintrace.client.DefaultFactory;
import org.headlessintrace.client.connection.Callback;
import org.headlessintrace.client.connection.ConnectState;
import org.headlessintrace.client.connection.ConnectionException;
import org.headlessintrace.client.connection.DefaultConnection;
import org.headlessintrace.client.connection.ConnectionTimeout;
import org.headlessintrace.client.connection.DefaultConnectionList;
import org.headlessintrace.client.connection.DefaultTraceEventWriterImpl;
import org.headlessintrace.client.connection.HostPort;
import org.headlessintrace.client.connection.IConnection;
import org.headlessintrace.client.connection.NetworkDataReceiverThread2;
import org.headlessintrace.client.connection.command.ClassInstrumentationCommand;
import org.headlessintrace.client.connection.command.IAgentCommand;
import org.headlessintrace.client.model.BeanTraceEventImpl;
import org.headlessintrace.client.request.BadCompletedRequestListener;
import org.headlessintrace.client.test.level2.connection.lowLevel.ConnectionTestUtils;
import org.junit.Assert;
import org.junit.Test;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;

public class TestConnection {

	@Test
	public void canConnectToExternalAgent() throws BadCompletedRequestListener {
		ClassInstrumentationCommand cic = new ClassInstrumentationCommand();
		cic.setIncludeClassRegEx(ConnectionTestUtils.TEST_CLASS_TO_INSTRUMENT_1);
		IAgentCommand commandArray[] = { cic };
		HostPort hostPort = new HostPort(ConnectionTestUtils.DEFAULT_HOST_1,ConnectionTestUtils.DEFAULT_PORT_1_INT);
		/**
		 *  C O N N E C T
		 */
		Callback testCallback = new Callback();
		IConnection c = null;
		try {
			c = DefaultConnectionList.getSingleton().connect(testCallback, hostPort, commandArray);
			assertNotNull("Is the test program started?  Didn't get a connection. [" + DefaultFactory.getFactory().getMessages().getTestSetupInstructions() + "]");
			assertEquals("Didn't not connect successfully", true, c.isConnected());
			java.util.List<ConnectState> myMessages = testCallback.getConnectStates();
			assertTrue("Expected to see 1 or more connection status messages", myMessages.size() > 0);
			//System.out.println("myMessages [" + myMessages.toString() + "]");
			boolean ynLocateMessage = ConnectionTestUtils.locateMessage2( myMessages,ConnectState.CONNECTED.toString());
			Assert.assertTrue("did not receive a CONNECTED status message", ynLocateMessage );
			Assert.assertFalse(	
					"whoops...most recent message is DISCONNECTED...thought we were still connected.", 
					ConnectionTestUtils.mostRecentMessageIsDisconnect2(myMessages)
					);
			
			c.disconnect();
			ynLocateMessage = ConnectionTestUtils.locateMessage2( 
					testCallback.getConnectStates(),
					ConnectState.DISCONNECTED.toString());
			Assert.assertTrue("did not receive a DISCONNECTED status message", ynLocateMessage );
		} catch (ConnectionTimeout e) {
			e.printStackTrace();
			fail("Received a connection timeout.  Is the test program example.FirstTraceExample running? [" + e.getMessage() + "]");
		} catch (ConnectionException e) {
			e.printStackTrace();
			fail("Unknown connection error [" + e.getMessage() + "]");
		}
	
				
	}

}
