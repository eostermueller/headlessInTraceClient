package org.intrace.client.test.level2.connection.lowLevel;

import static org.junit.Assert.*;

import org.intrace.client.DefaultFactory;
import org.intrace.client.connection.ConnectState;
import org.intrace.client.connection.ConnectionException;
import org.intrace.client.connection.ConnectionTimeout;
import org.intrace.client.connection.DefaultCallback;
import org.intrace.client.connection.DefaultConnectionList;
import org.intrace.client.connection.HostPort;
import org.intrace.client.connection.IConnection;
import org.intrace.client.connection.IConnectionList;
import org.intrace.client.connection.IConnectionStateCallback;
import org.intrace.client.request.BadCompletedRequestListener;
//import org.intrace.client.connection.test.BasicConnectionTest_RequiresExternalAgent.DefaultCallback;
import org.junit.Assert;
import org.junit.Test;

public class TestMultipleConnections_requiresTwoExternalAgents {
	private static final String DEFAULT_HOST_1 = "localhost";
	private static final String DEFAULT_PORT_2 = "9123";
	private static final String TEST_CLASS_TO_INSTRUMENT_1 = "example.FirstTraceExample";

	/**
	 * 	This test shows that the API allows two parts of a single program to act independently.
	 *	Perhaps one wants to collect information on events of type X,
	 *	and perhaps a different part of the same program collects events of type Y.
	 * @throws ConnectionTimeout
	 * @throws ConnectionException
	 * @throws BadCompletedRequestListener 
	 */
	@Test
	public void testStatusMessagesFromTwoConnectionsToSamePort() throws ConnectionTimeout, ConnectionException, BadCompletedRequestListener {
		IConnectionList connList = DefaultConnectionList.getSingleton();

		//These two objects connect to the same machine:port (localhost:9123)
		HostPort hostPort_1 = new HostPort(ConnectionTestUtils.DEFAULT_HOST_1,ConnectionTestUtils.DEFAULT_PORT_1);
		HostPort hostPort_2 = new HostPort(ConnectionTestUtils.DEFAULT_HOST_1,ConnectionTestUtils.DEFAULT_PORT_1);

		/****************************************************************************************************
		 *    F   I   R   S   T      C   O   N   N   E   C   T   I   O   N 
		 */
		DefaultCallback tc_1 = new DefaultCallback();
		
		connList.connect(tc_1, hostPort_1, null /* no startup commands */);
		
		assertEquals("Just showing that all connections start out dead -- looking at the history of connect states.", ConnectState.DISCONNECTED,tc_1.getConnectStates().get(0));
		assertEquals("checking the most recent status messages", ConnectState.CONNECTED, tc_1.getConnectStates().get(tc_1.getConnectStates().size()-1) );
		
		IConnection myConn_1 = connList.locateConnection(hostPort_1);
		
		assertTrue("IS the test app started?", myConn_1.isConnected());
		assertNotNull("locateConnection was unable to find the recently-added connection, even though 'CONNECTED' was the last status", myConn_1 );
		Assert.assertTrue("did not receive any connection status change events", tc_1.getConnectionStatusMsgs().size()>0 );
		boolean ynLocateMessage = ConnectionTestUtils.locateMessage( tc_1.getConnectionStatusMsgs(),ConnectState.CONNECTED.toString());
		Assert.assertTrue("did not receive a CONNECTED status message", ynLocateMessage );
		Assert.assertFalse(	
				"whoops...most recent message is DISCONNECTED...thought we were still connected.", 
				ConnectionTestUtils.mostRecentMessageIsDisconnect(tc_1.getConnectionStatusMsgs())
				);

		Assert.assertEquals("didn't find the single callback used for this test's single connection", 1, myConn_1.getConnCallbackSize());
		
		/****************************************************************************************************
		 *    S   E   C   O   N   D      C   O   N   N   E   C   T   I   O   N 
		 */

		DefaultCallback tc_2 = new DefaultCallback();
		
		IConnection ret_2 = connList.connect(tc_2, hostPort_2, null /* no startup commands */);
		assertTrue("checking second status message ", ret_2.isConnected()  );
		
		IConnection myConn_2 = connList.locateConnection(hostPort_2);
		assertEquals("Expecting that con-1 and con-2 are equal, since both used same connection data",myConn_1, myConn_2);
		assertEquals("The return value from 'connect' should be the same as the object returned from locate", ret_2, myConn_2);
		assertNotNull("locateConnection was unable to find the recently-added connection, even though 'CONNECTED' was the last status", myConn_2 );

		Assert.assertEquals("Expecting two callbacks, one for each 'window' to a single connection ", 2, myConn_2.getConnCallbackSize());

		/****************************************************************************************************
		 *    D  I   S   C   O   N   N   E   C  T      S   E   C   O   N   D 
		 */
		
		if (myConn_2!=null) {
			//assertEquals("expected a single connection in the ConnectionList", 1, cl.size());
			//myConn.disconnect();
			connList.disconnect(myConn_2, tc_2);
			assertEquals("Expecting a single callback, now that the disconnect has removed one callback from the single connection ", 
					1, myConn_2.getConnCallbackSize());
			
		} else {
			Assert.fail("Simple connection test, unable to locate connection in list");
		}

		//The following two messages are not fired -- because only the callback is removed, the actual connection stays active.
		//ynLocateMessage = ConnectionTestUtils.locateMessage( tc_2.getConnectionStatusMsgs(),ConnectState.DISCONNECTED.toString());
		//Assert.assertTrue("did not receive a DISCONNECTED status message", ynLocateMessage );
		
		
		Assert.assertEquals("Now that the first disconnect has happend, one of the two callbacks should have been removed", 1, myConn_2.getConnCallbackSize());
		
		//System.out.println("All connection messages: [" + tc_2.getConnectionStatusMsgs() + "]");
		
		/****************************************************************************************************
		 *    D  I   S   C   O   N   N   E   C  T      F   I   R   S   T
		 */
		if (myConn_1!=null) {
			//assertEquals("expected a single connection in the ConnectionList", 1, cl.size());
			//myConn.disconnect();
			connList.disconnect(myConn_1, tc_1);
			assertEquals("expected zero connections in the ConnectionList because we just disconnected.", 0, connList.size());
			
			//System.out.println("All connection messages: [" + tc_1.getConnectionStatusMsgs() + "]");
			assertFalse("ConnectionPartTwo is not correctly reflecting that we're not disconnected", myConn_1.isConnected());
		} else {
			Assert.fail("Simple connection test, unable to locate connection in list");
		}

		Assert.assertEquals("Now that the disconnect has happend, all callbacks should have been removed", 0, myConn_2.getConnCallbackSize());
		
		//System.out.println("All connection messages: [" + tc_1.getConnectionStatusMsgs() + "]");
		
	}
	
	@Test
	public void testStatusMessagesFromTwoConnectionsToDifferentPorts() throws ConnectionTimeout {
		IConnectionList connList = DefaultConnectionList.getSingleton();
		
		/****************************************************************************************************
		 *    F   I   R   S   T      C   O   N   N   E   C   T   I   O   N 
		 */
		DefaultCallback tc_1 = new DefaultCallback();
		HostPort hostPort_1 = new HostPort(ConnectionTestUtils.DEFAULT_HOST_1,ConnectionTestUtils.DEFAULT_PORT_1);
		
		try {
			IConnection returnValue_1 = connList.connect(tc_1, hostPort_1 , null /* no startup commands */);
			assertNotNull("Is the _first_ test program started?  Didn't get a connection. [" + DefaultFactory.getFactory().getMessages().getTestSetupInstructions() + "]");
			
			IConnection myConn_1 = connList.locateConnection(hostPort_1);
			assertNotNull("locateConnection was unable to find the recently-added connection, even though 'CONNECTED' was the last status", myConn_1 );
			
			assertEquals("Testing two different ways of getting the same connection...didn't work", returnValue_1, myConn_1);
			
			assertTrue("This is the first way of testing connection status.  isConnected() getter should be showing true", myConn_1.isConnected());
			assertEquals("But if you need to know more than a boolean, you can look at the exact state", ConnectState.CONNECTED, myConn_1.getMasterCallback().getConnectState());
			
			Assert.assertTrue("did not receive any connection status change events", tc_1.getConnectionStatusMsgs().size()>0 );
			boolean ynLocateMessage = ConnectionTestUtils.locateMessage( tc_1.getConnectionStatusMsgs(),ConnectState.CONNECTED.toString());
			Assert.assertTrue("did not receive a CONNECTED status message", ynLocateMessage );
			Assert.assertFalse(	
					"whoops...most recent message is DISCONNECTED...thought we were still connected.", 
					ConnectionTestUtils.mostRecentMessageIsDisconnect(tc_1.getConnectionStatusMsgs())
					);

			Assert.assertEquals("didn't find the single callback used for this test's single connection", 1, myConn_1.getConnCallbackSize());
			
			/****************************************************************************************************
			 *    S   E   C   O   N   D      C   O   N   N   E   C   T   I   O   N 
			 */

			DefaultCallback tc_2 = new DefaultCallback();
			HostPort hostPort_2 = new HostPort(ConnectionTestUtils.DEFAULT_HOST_2,ConnectionTestUtils.DEFAULT_PORT_2);
			
			IConnection returnValue_2 = connList.connect(tc_2, hostPort_2, null /* no startup commands */);
			IConnection myConn_2 = connList.locateConnection(hostPort_2);
			
			assertNotNull("locateConnection was unable to find the recently-added connection, even though 'CONNECTED' was the last status", myConn_2 );
			
			assertEquals("Testing two different ways of getting the same connection...didn't work", returnValue_2, myConn_2);
			
			
			assertTrue("This is the first way of testing connection status.  isConnected() getter should be showing true", myConn_2.isConnected());
			assertEquals("But if you need to know more than a boolean, you can look at the exact state", ConnectState.CONNECTED, myConn_1.getMasterCallback().getConnectState());
			
			assertTrue("Perhaps the example.*TraceExample isn't started yet?", myConn_2.isConnected());
			assertNotNull("locateConnection was unable to find the recently-added connection, even though 'CONNECTED' was the last status", myConn_2 );
			Assert.assertTrue("did not receive any connection status change events", tc_2.getConnectionStatusMsgs().size()>0 );
			ynLocateMessage = ConnectionTestUtils.locateMessage( tc_2.getConnectionStatusMsgs(),ConnectState.CONNECTED.toString());
			Assert.assertTrue("did not receive a CONNECTED status message", ynLocateMessage );
			Assert.assertFalse(	
					"whoops...most recent message is DISCONNECTED...thought we were still connected.", 
					ConnectionTestUtils.mostRecentMessageIsDisconnect(tc_2.getConnectionStatusMsgs())
					);

//			Assert.assertEquals("didn't find the single callback used for this test's single connection", 1, myConn_2.getConnCallbackSize());
//			assertEquals("2 connections have been established.  expected four synonyms, but didn't find them.  2 pointing to the first connection, 2 pointing to the second).", 4, connList.size());

			/****************************************************************************************************
			 *    D  I   S   C   O   N   N   E   C  T      S   E   C   O   N   D 
			 */
			
			if (myConn_2!=null) {
				connList.disconnect(myConn_2, tc_2);
				assertFalse("shouldn't have indicated that we've already disconnected", myConn_2.isConnected());

				IConnection myConn_2_after_discon = connList.locateConnection(hostPort_2);
				assertNull("After a disconnection, the connection should have been gone from the list", myConn_2_after_discon);
				
			} else {
				Assert.fail("Simple connection test, unable to locate connection in list");
			}

			Assert.assertEquals("Now that the disconnect has happend, all callbacks should have been removed", 0, myConn_2.getConnCallbackSize());
			
			//System.out.println("All connection messages: [" + tc_2.getConnectionStatusMsgs() + "]");
			
			/****************************************************************************************************
			 *    D  I   S   C   O   N   N   E   C  T      F   I   R   S   T
			 */
			if (myConn_1!=null) {
				connList.disconnect(myConn_1, tc_1);
				assertFalse("ConnectionPartTwo did not reflect that we just disconnected", myConn_1.isConnected());
				assertEquals("expected zero connections in the ConnectionList because we just disconnected.", 0, connList.size());
				IConnection myConn_1_after_discon = connList.locateConnection(hostPort_1);
				assertNull("After a disconnection, the connection should have been gone from the list", myConn_1_after_discon);
				
			} else {
				Assert.fail("Simple connection test, unable to locate connection in list");
			}

			Assert.assertEquals("Now that the disconnect has happend, all callbacks should have been removed", 0, myConn_2.getConnCallbackSize());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception trying to connect [" + e.getMessage() + "]");
		}
		
		//System.out.println("All connection messages: [" + tc_1.getConnectionStatusMsgs() + "]");

		
	}

}
