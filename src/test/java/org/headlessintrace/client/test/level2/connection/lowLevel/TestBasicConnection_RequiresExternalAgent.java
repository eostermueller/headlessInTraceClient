package org.headlessintrace.client.test.level2.connection.lowLevel;

import static org.junit.Assert.*;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.headlessintrace.client.connection.Callback;
import org.headlessintrace.client.connection.ConnectState;
import org.headlessintrace.client.connection.ConnectionTimeout;
import org.headlessintrace.client.connection.DefaultCallback;
import org.headlessintrace.client.connection.DefaultConnectionList;
import org.headlessintrace.client.connection.DefaultTraceEventWriterImpl;
import org.headlessintrace.client.connection.HostPort;
import org.headlessintrace.client.connection.IConnection;
import org.headlessintrace.client.connection.IConnectionList;
import org.headlessintrace.client.connection.NetworkDataReceiverThread2;
import org.headlessintrace.client.connection.command.ClassInstrumentationCommand;
import org.headlessintrace.client.connection.command.IAgentCommand;
import org.headlessintrace.client.model.ITraceEvent;
import org.junit.Assert;
import org.junit.Test;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
/**
 * This test does not launch an agent, so one must be launched separately on "localhost:9123".
 * Currently, it relies on a java process to run separate, along with an InTrace agent.
 * Here is the folder of the test:  C:\src\jsource\inTrace\eto-demo\pojo\build
 * The example uses a class named example.TraceExample
 * @author e0018740
 * 
 *
 */
public class TestBasicConnection_RequiresExternalAgent {

    private CountDownLatch m_latch;
	protected boolean m_ynWait;
	/*
    * The test verifies that the correct "CONNECT" and "DISCONNECT" messages show up in the right sequence after connecting/disconnecting.
    */
    @Test
	public void testStatusMessagesFromRealConnection() throws ConnectionTimeout {
		/**
		 * InTrace will notify this test object with connection status updates.
		 */
		Callback tc = new Callback();
		IConnectionList cl = DefaultConnectionList.getSingleton();
		HostPort hostPort = new HostPort(ConnectionTestUtils.DEFAULT_HOST_1,ConnectionTestUtils.DEFAULT_PORT_1);
		
		try {
			cl.connect(tc, hostPort, null /* no classes to instrument */);
			Callback testCallback = new Callback();
			assertTrue("Is the test app started?", cl.locateConnection(hostPort).isConnected() );
			assertTrue("B4 disconnect, expecting at least one message.  Is the test server started?", tc.getMessages().size() > 0 );
			assertEquals("B4 disconnect....checking count of status messages.  Is the test server started?", 3, tc.getMessages().size() );
			//assertEquals("checking first status message b4 dis-connecting", "DISCONNECTED",tc.getMessages().get(0));
			assertEquals("checking second status message b4 dis-connecting", "CONNECTED", tc.getMessages().get(1).toUpperCase() );
			//System.out.println("#####@found messages [" + tc.getMessages().toString() + "]");
			
			IConnection myConn = cl.locateConnection(hostPort);
			
			assertNotNull("locateConnection was unable to find the recently-added connection, even though 'CONNECTED' was the last status", myConn );
			Assert.assertTrue("did not receive any connection status change events", tc.getMessages().size()>0 );
			boolean ynLocateMessage = ConnectionTestUtils.locateMessage( tc.getMessages(),ConnectState.CONNECTED.toString());
			Assert.assertTrue("did not receive a CONNECTED status message", ynLocateMessage );
			Assert.assertFalse(	
					"whoops...most recent message is DISCONNECTED...thought we were still connected.", 
					ConnectionTestUtils.mostRecentMessageIsDisconnect(tc.getMessages())
					);

			Assert.assertEquals("didn't find the single callback used for this test's single connection", 1, myConn.getConnCallbackSize());
			if (myConn!=null) {
				//assertEquals("expected a single connection in the ConnectionList", 1, cl.size());
				//myConn.disconnect();
				cl.disconnect(myConn, tc);
				assertEquals("expected zero connections in the ConnectionList because we just disconnected.", 0, cl.size());
				
				
				//This doesn't work
				//ynLocateMessage = ConnectionTestUtils.locateMessage( tc.getMessages(),ConnectState.DISCONNECTED.toString());
				//Assert.assertTrue("did not receive a DISCONNECTED status message", ynLocateMessage );
				
				assertEquals(ConnectState.DISCONNECTED, myConn.getMasterCallback().getConnectState());
				
				//System.out.println("All connection messages: [" + tc.getMessages() + "]");
				
			} else {
				Assert.fail("Simple connection test, unable to locate connection in list");
			}

			//Does not work:
//			ynLocateMessage = ConnectionTestUtils.locateMessage( tc.getMessages(),ConnectState.DISCONNECTED.toString());
//			Assert.assertTrue("did not receive a DISCONNECTED status message", ynLocateMessage );
			Assert.assertEquals("Now that the disconnect has happend, all callbacks should have been removed", 0, testCallback.getConnectStates().size());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception trying to connect [" + e.getMessage() + "]");
		}
		
		//System.out.println("All connection messages: [" + tc.getMessages() + "]");
		
	}
}
