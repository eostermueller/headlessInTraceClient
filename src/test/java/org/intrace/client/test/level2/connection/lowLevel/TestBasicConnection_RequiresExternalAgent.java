package org.intrace.client.test.level2.connection.lowLevel;

import static org.junit.Assert.*;

import org.intrace.client.connection.ConnectState;
import org.intrace.client.connection.ConnectionTimeout;
import org.intrace.client.connection.DefaultConnectionList;
import org.intrace.client.connection.DefaultTraceEventWriterImpl;
import org.intrace.client.connection.HostPort;
import org.intrace.client.connection.IConnection;
import org.intrace.client.connection.IConnectionList;
import org.intrace.client.connection.NetworkDataReceiverThread2;
import org.intrace.client.connection.command.ClassInstrumentationCommand;
import org.intrace.client.connection.command.IAgentCommand;
import org.intrace.client.model.ITraceEvent;
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

    /*
    * The test verifies that the correct "CONNECT" and "DISCONNECT" messages show up in the right sequence after connecting/disconnecting.
    */
/*    @Test */
	public void testStatusMessagesFromRealConnection() throws ConnectionTimeout {
		/**
		 * InTrace will notify this test object with connection status updates.
		 */
		TestCallback tc = new TestCallback();
		IConnectionList cl = DefaultConnectionList.getSingleton();
		HostPort hostPort = new HostPort(ConnectionTestUtils.DEFAULT_HOST_1,ConnectionTestUtils.DEFAULT_PORT_1);
		
		try {
			cl.connect(tc, hostPort, null /* no classes to instrument */);
			TestCallback testCallback = new TestCallback();
			assertTrue("Is the test app started?", cl.locateConnection(hostPort).isConnected() );
			assertTrue("B4 disconnect, expecting at least one message.  Is the test server started?", tc.getMessages().size() > 0 );
			assertEquals("B4 disconnect....checking count of status messages.  Is the test server started?", 2, tc.getMessages().size() );
			assertEquals("checking first status message b4 dis-connecting", "DISCONNECTED",tc.getMessages().get(0));
			assertEquals("checking second status message b4 dis-connecting", "CONNECTED", tc.getMessages().get(1) );
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
				ynLocateMessage = ConnectionTestUtils.locateMessage( tc.getMessages(),ConnectState.DISCONNECTED.toString());
				Assert.assertTrue("did not receive a DISCONNECTED status message", ynLocateMessage );
				
				//System.out.println("All connection messages: [" + tc.getMessages() + "]");
				
			} else {
				Assert.fail("Simple connection test, unable to locate connection in list");
			}

			ynLocateMessage = ConnectionTestUtils.locateMessage( tc.getMessages(),ConnectState.DISCONNECTED.toString());
			Assert.assertTrue("did not receive a DISCONNECTED status message", ynLocateMessage );
			Assert.assertEquals("Now that the disconnect has happend, all callbacks should have been removed", 0, testCallback.getConnectStates().size());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception trying to connect [" + e.getMessage() + "]");
		}
		
		//System.out.println("All connection messages: [" + tc.getMessages() + "]");
		
	}
    /*
	* The test verifies that the correct "CONNECT" and "DISCONNECT" messages show up in the right sequence after connecting/disconnecting.
	* This method started as a copy of testStatusMessagesFromRealConnection.
	* Therefore, there are some duplicate asssertions.
	*/
@Test	
	public void testStatusMessagesFromRealConnection_withTraceEvents() throws ConnectionTimeout {
		/**
		 * InTrace will notify this test object with connection status updates.
		 */
		TestCallback testCallback = new TestCallback();
		IConnectionList cl = DefaultConnectionList.getSingleton();

		HostPort hostPort = new HostPort(ConnectionTestUtils.DEFAULT_HOST_1, ConnectionTestUtils.DEFAULT_PORT_1);

		ClassInstrumentationCommand cic = new ClassInstrumentationCommand();
		cic.setIncludeClassRegEx(ConnectionTestUtils.TEST_CLASS_TO_INSTRUMENT_1);
		IAgentCommand commandArray[] = { cic };
		
		try {
			cl.connect(testCallback, hostPort, commandArray);
			IConnection myConn = cl.locateConnection(hostPort);
			//System.out.println("################################### Messages [" + tc.getMessages() + "]");
			assertNotNull("locateConnection was unable to find the recently-added connection, even though 'CONNECTED' was the last status", myConn );
			assertEquals("Perhaps the test application isn't started?", true, myConn.isConnected());
			boolean ynLocateMessage = ConnectionTestUtils.locateMessage( testCallback.getMessages(),ConnectState.CONNECTED.toString());
			Assert.assertTrue("did not receive a CONNECTED status message", ynLocateMessage );
			Assert.assertFalse(	
					"whoops...most recent message is DISCONNECTED...thought we were still connected.", 
					ConnectionTestUtils.mostRecentMessageIsDisconnect(testCallback.getMessages())
					);
			
			
			DefaultTraceEventWriterImpl traceWriter = new DefaultTraceEventWriterImpl();
			EventList<ITraceEvent> traceEvents = new BasicEventList<ITraceEvent>();
			traceWriter.setTraceEvents(traceEvents);
			
			NetworkDataReceiverThread2 ndrt2 = myConn.getNetworkTraceThread2();
			//System.out.println("ConnectionPartTwo [" + myConn.hashCode() + "] NetworkDataReceiver [" + ndrt2.hashCode() + "] TraceWriter [" +  traceWriter.hashCode() + "]");
			ndrt2.addTraceWriter(traceWriter);

			/**
			 * Pause to collect some trace events from the Agent running on 9123.
			 */
			Thread.sleep(ConnectionTestUtils.EVENT_COLLECTION_TIME_MS);			
			
			long traceEventCount = traceEvents.size();
			//System.out.println("Found [" + traceEventCount + "] events.");
			assertTrue("Collected fewer than 50 trace events", traceEventCount > 50);
			
			if (myConn!=null) {
				//assertEquals("expected a single connection in the ConnectionList", 1, cl.size());
				//myConn.disconnect();
				int numRemainingCallbacks = cl.disconnect(myConn, testCallback);
				assertEquals("This disconnect should have removed the last callback.",0,numRemainingCallbacks);
				
				myConn.disconnect();
				
				
				assertEquals( "should have received a disconnected message", 
						ConnectState.DISCONNECTED, 
						myConn.getMasterCallback().getConnectState());
				//ynLocateMessage = ConnectionTestUtils.locateMessage( testCallback.getMessages(),ConnectState.DISCONNECTED.toString());
				//Assert.assertTrue("did not receive a DISCONNECTED status message", ynLocateMessage );
				
				//System.out.println("All connection messages: [" + tc.getMessages() + "]");
				
			} else {
				Assert.fail("Simple connection test, unable to locate connection in list");
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception trying to connect [" + e.getMessage() + "]");
		}
		
	}
}
