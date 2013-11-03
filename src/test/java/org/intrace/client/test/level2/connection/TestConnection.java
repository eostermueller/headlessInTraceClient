package org.intrace.client.test.level2.connection;

import static org.junit.Assert.*;

import java.awt.List;

import org.intrace.client.DefaultFactory;
import org.intrace.client.connection.ConnectState;
import org.intrace.client.connection.ConnectionException;
import org.intrace.client.connection.DefaultConnection;
import org.intrace.client.connection.ConnectionTimeout;
import org.intrace.client.connection.DefaultConnectionList;
import org.intrace.client.connection.DefaultTraceEventWriterImpl;
import org.intrace.client.connection.HostPort;
import org.intrace.client.connection.IConnection;
import org.intrace.client.connection.NetworkDataReceiverThread2;
import org.intrace.client.connection.command.ClassInstrumentationCommand;
import org.intrace.client.connection.command.IAgentCommand;
import org.intrace.client.model.BeanTraceEventImpl;
import org.intrace.client.request.BadCompletedRequestListener;
import org.intrace.client.test.level2.connection.lowLevel.ConnectionTestUtils;
import org.intrace.client.test.level2.connection.lowLevel.TestCallback;
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
		TestCallback testCallback = new TestCallback();
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
