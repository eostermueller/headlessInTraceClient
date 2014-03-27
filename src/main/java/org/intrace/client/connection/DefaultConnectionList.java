package org.intrace.client.connection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.intrace.client.DefaultFactory;
import org.intrace.client.connection.command.IAgentCommand;
import org.intrace.client.filter.ITraceFilterExt;
import org.intrace.client.request.BadCompletedRequestListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Singleton.  Holds all connections.
 * @author e0018740
 *
 */
public class DefaultConnectionList implements IConnectionList {
	private static final Logger LOG = LoggerFactory.getLogger(DefaultConnectionList.class);
	private static IConnectionList m_connectionList = new DefaultConnectionList();
	/**
	 * The synchronization seems to only be required for the first call to instantiate the singleton.
	 * @return
	 */
	public static synchronized IConnectionList getSingleton() {
		if (m_connectionList==null) {
			m_connectionList = new DefaultConnectionList();
		}
		return m_connectionList;
	}
	private Map<String, IConnection> m_mapConnections = null;
	private DefaultConnectionList() {
		//m_connections = new CopyOnWriteArrayList<ConnectionPartTwo>();
		m_mapConnections = new ConcurrentHashMap<String, IConnection>();
	}
	/**
	 * TODO:  Add code to see if we're already connected to this host.
	 * @param host
	 * @param port
	 * @return
	 */
	@Override
	public IConnection locateConnection(HostPort hostPort) {
		if (hostPort==null) {
			throw new RuntimeException("Not allowed to search for a null hostPort");
		}
		if (LOG.isDebugEnabled()) LOG.debug("locateConnection: Looking for ["  + hostPort.key() + "]");
		if (LOG.isDebugEnabled()) LOG.debug(" in this existing map [" + m_mapConnections.toString() + "]");
		
		IConnection cem = null;
		if (hostPort.getIpKeyName()!=null) {
			cem = m_mapConnections.get(hostPort.getIpKeyName());
		}
		if (cem==null) {
			if (hostPort.getHostNameKey()!=null) {
				cem = m_mapConnections.get(hostPort.getHostNameKey());
			}
		}
		return cem;//TODO:  assume that connection DOES NOT exist.....need to add code to search through list.
	}
	/**
	 * 
	 * @param conn
	 * @param connectionCallback
	 */
	@Override
	public int disconnect(IConnection conn, IConnectionStateCallback connectionCallback) {
		if (conn==null) {
			throw new RuntimeException("Don't support null connections");
		}
		int numRemainingCallbacks =  conn.removeConnectionStatusCallback(connectionCallback); // disconnect is called downstream, but only if no other callbacks are registered.
		
		if (numRemainingCallbacks > 0) {
			connectionCallback.setConnectState(ConnectState.DISCONNECTED);
		}
		
		return numRemainingCallbacks; 
			
	}
	/**
	 * 
	 * 
	 * Say that a user has two windows that both want to connect to a single machine.
	 * Window "A" uses a domain name to connect.
	 * Window "B" uses the IP address.
	 * InTrace will have trouble mapping both of these windows to a single connection in a Map.
	 * To help InTrace map multiple windows to a single connection, this method re-registers the connection,
	 * but using the "Cannonical" name....it is the callers responsibility to pass in the "right" cannonical name.
	 * This method was designed to be called from this#setSocket(), which is when the connection initialization completes. 
	 * 
	 * @param hostPort
	 */
	private void registerCannonicalSynonym(HostPort hostPort, IConnection c) {
		add(hostPort, c);
	}
	@Override
	public IConnection connect(IConnectionStateCallback connectionCallback, HostPort hostPort, IAgentCommand[] startupCommands) throws ConnectionException, ConnectionTimeout, BadCompletedRequestListener {
		return this.connect(connectionCallback, hostPort, startupCommands, null);
	}

	@Override
	public IConnection connect(IConnectionStateCallback connectionCallback, HostPort hostPort, IAgentCommand[] startupCommands, ITraceFilterExt filter) throws ConnectionException, ConnectionTimeout, BadCompletedRequestListener {
		IConnection c = locateConnection(hostPort);
		if (c==null) {
			c = DefaultFactory.getFactory().getDormantConnection();
			if (filter!=null) c.getTraceWriter().setTraceFilterExt(filter);
			c.addCallback(connectionCallback);
			add(hostPort,c);
			c.connect(hostPort, startupCommands);
		} else {
			//If we're already connected, then this is just a request for a second window
			//to listen on the events of the existing connection.
			c.addCallback(connectionCallback);
			//Make sure new callback (above line of code) gets a status update
			//connPartTwo.broadcastConnectionState();
			c.setCommandArray(startupCommands);
			c.executeStartupCommands();
			if (!c.isConnected()) {
				c.connect(hostPort, startupCommands);
			}
		}
		return c;
	}
	
	/**
	 * First order of business:  create socket  (ISocketCallback fires events when socket is successfully connected.)
	 * Second order of business:  create control and trace threads. (IConnectionStateCallback  notifies the 'window' when connection is complete.
	 *    (the connection is complete when the control and trace threads have successfully received msgs from the agent. 
	 * 
	 * @param connectionCallback -- This is the SWT window that wants to display trace information from this connection.
	 * @param host
	 * @param port
	 */
//	public void _connect(IConnectionStateCallback connectionCallback, HostPort hostPort, IAgentCommand[] startupCommands) throws ConnectionException {
//		
//		ConnectionDetail connPartTwo = locateConnection(hostPort);
//		
//		if (connPartTwo==null) { // not connected yet. 
//			connPartTwo = new ConnectionDetail();//second order of business
//			connPartTwo.setStartupCommands(startupCommands);
//			connPartTwo.addConnCallback(connectionCallback);
//			add(connPartTwo, hostPort);
//			ConnectionPartOne.connectToAgent(connPartTwo, hostPort);//First order of business
//		} else {
//			//TODO:  seems like we need to test to see if the connection if alive.
//			if (connPartTwo.isConnected()) {
//				//If we're already connected, then this is just a request for a second window
//				//to listen on the events of the existing connection.
//				connPartTwo.addConnCallback(connectionCallback);
//				//Make sure new callback (above line of code) gets a status update
//				//connPartTwo.broadcastConnectionState();
//				connPartTwo.setStartupCommands(startupCommands);
//				connPartTwo.executeStartupCommands();
//			} else {
//				throw new ConnectionException(hostPort, ClientStrings.CACHED_CONNECTION_IS_DEAD);
//			}
//			
//		}
//		
//	}
	/**
	 * 
	 * @param c
	 * @param hostPort
	 */
	@Override
	public void add(HostPort hostPort, IConnection c) {

		//m_connections.add(cem);
		m_mapConnections.put(hostPort.key(), c);
	}
	@Override
	public int size() {
		return m_mapConnections.size();
	}
	/**
	 * If user passes in DNS name instead of IP, m_mapConnecxtions will contain multiple entries for a single connection object.
	 * One entry with the _given_ DNS (and port) as the key, one for the IP(and port).
	 * @param criteria
	 */
	void remove(IConnection criteria) {
		HostPort critHostPort = criteria.getHostPort();
		for ( String key : m_mapConnections.keySet()) {
			IConnection c = m_mapConnections.get(key);
			if (c.getHostPort().equals(critHostPort)) {
				m_mapConnections.remove(key);
			}
			
		}
	}
	@Override
	public List<IConnection> getConnections() {
		List<IConnection> l = new ArrayList<IConnection>();
		for( Object o : m_mapConnections.values().toArray()) {
			IConnection c = (IConnection)o;
			l.add(c);
		}
		return l;
	}
	
}
