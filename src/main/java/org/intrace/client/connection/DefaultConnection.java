package org.intrace.client.connection;


import java.net.InetSocketAddress;

import java.net.Socket;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.intrace.client.DefaultFactory;
import org.intrace.client.HumanReadableMessages;
import org.intrace.client.ITraceWriter;
import org.intrace.client.connection.command.IAgentCommand;
import org.intrace.client.model.ITraceEvent;
import org.intrace.client.request.BadCompletedRequestListener;
import org.slf4j.LoggerFactory;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
/**
 * This class supports a simple use case where a single connection writes events to a single event list.
 * More complicated code can be written to
 * - have a single connection write to multiple event lists (pub-sub style)
 * - have multiple connections write to a single event list (two JVMs participate in a single workflow).
 * The "client" in the package name indicates that this class runs on the client side, maintaining a connection with something else (an intrace agent on a server).
 * @author erikostermueller
 *
 */
public class DefaultConnection implements IConnection {
//	private static final Logger LOG = Logger.getLogger( DefaultConnection.class.getName() );
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(DefaultConnection.class);	
	ConnectionDetail getConnectionDetail() {
		return m_connDetail;
	}
	@Override
	public int getConnCallbackSize() {
		return getConnectionDetail().getConnCallbacks().size();
	}
	private void setConnectionDetail(ConnectionDetail connectionDetail) {
		this.m_connDetail = connectionDetail;
	}
	private ITraceWriter m_traceWriter = null;
	private IAgentCommand[] getCommandArray() {
		return getConnectionDetail().getStartupCommands();
	}
	@Override
	public void setCommandArray(IAgentCommand[] commandArray) {
		getConnectionDetail().setCommandArray(commandArray);
	}
	@Override
	public IConnectionStateCallback getMasterCallback() {
		return getConnectionDetail();
	}
	@Override
	public void addCallback(IConnectionStateCallback callback) {
		getConnectionDetail().addConnCallback(callback);
	}
	private ConnectionDetail m_connDetail = null;
	HostPort m_hostPort = null;
	public DefaultConnection() {
		setConnectionDetail(new ConnectionDetail());
		setTraceWriter(DefaultFactory.getFactory().getTraceWriter());
		getTraceWriter().setTraceEvents(new BasicEventList<ITraceEvent>());
	}
	@Override
	public EventList<ITraceEvent> getTraceEvents() {
		return m_traceWriter.getTraceEvents();
		//return traceEvents;
	}

	@Override
	public boolean connect(String host, int port) throws ConnectionTimeout, ConnectionException, BadCompletedRequestListener {
		return connect(host,port, null);
	}

	/**
	 * The elegant implementation of the connection timeout was stolen from here:
	 * 	http://stackoverflow.com/questions/2275443/how-to-timeout-a-thread
	 */
	@Override
	public boolean connect(HostPort hostPort, IAgentCommand[] startupCommandAry ) throws ConnectionTimeout, ConnectionException {
		boolean ynRC = false;
		setHostPort( hostPort );
		List<String> errors = getHostPort().validate();
		
		if (errors.size()>0) { 
			ConnectionException e = new ConnectionException(getHostPort(), errors.toString() ); 
			throw e;
		} else {
	        HumanReadableMessages msgs = DefaultFactory.getFactory().getMessages();
	        long lngTimeoutMs = DefaultFactory.getFactory().getConfig().getConnectWaitMs();
			setCommandArray(startupCommandAry);
		
	        ExecutorService executor = Executors.newSingleThreadExecutor();
	        Future<Boolean> future = executor.submit(new Task());
	        try {
	            getMasterCallback().setConnectionStatusMsg(msgs.getAttemptingToConnect( getHostPort()) );
	            getMasterCallback().setConnectState(ConnectState.CONNECTING);

	            Boolean ynTaskRC = future.get(lngTimeoutMs, TimeUnit.MILLISECONDS); 
	            if (ynTaskRC.equals(Boolean.TRUE)) {
	        		NetworkDataReceiverThread2 ntt2 = getConnectionDetail().getNetworkTraceThread2();
	        		ntt2.addTraceWriter(m_traceWriter);
	        		ynRC = true;
	            }
	        } catch (TimeoutException e) {
	            getMasterCallback().setConnectionStatusMsg( e.toString() ); 
	            getMasterCallback().setConnectState(ConnectState.DISCONNECTED_ERR);
	        } catch (InterruptedException e) {
	            getMasterCallback().setConnectionStatusMsg( e.toString() ); 
	            getMasterCallback().setConnectState(ConnectState.DISCONNECTED_ERR);
			} catch (ExecutionException e) {
	            getMasterCallback().setConnectionStatusMsg( e.toString() ); 
	            getMasterCallback().setConnectState(ConnectState.DISCONNECTED_ERR);
			}
	        executor.shutdownNow();
		}
        return ynRC;
	}
	class Task implements Callable<Boolean> {
	    @Override
	    public Boolean call() throws Exception {
	    	Boolean ynRC = Boolean.FALSE;
	    	final Socket socket = new Socket();
	    	HumanReadableMessages msgs = DefaultFactory.getFactory().getMessages();
	        try {
	            socket.connect(new InetSocketAddress(getHostPort().hostNameOrIpAddress, getHostPort().port));
	            getHostPort().setIp(socket.getInetAddress().getHostAddress());
	            getConnectionDetail().setSocket(socket);
	            getMasterCallback().setConnectionStatusMsg( 
	            		msgs.getConnectionSuccessful( getHostPort() ) );
	            getMasterCallback().setConnectState(ConnectState.CONNECTED);
	            ynRC = Boolean.TRUE;
	        } catch (Exception e) {
	        	  getMasterCallback().setConnectionStatusMsg( msgs.getFailedConnection(getHostPort(), e) );
	        	  getMasterCallback().setConnectState(ConnectState.DISCONNECTED_ERR);
	        	  getConnectionDetail().setSocket(null);
	        	  ynRC = Boolean.FALSE;
	        }
	        return ynRC;
	    }
	}	
	public void connect_ORIG(String host, int port, IAgentCommand[] startupCommandAry ) throws ConnectionTimeout, ConnectionException, BadCompletedRequestListener {
		setHostPort( new HostPort(host,port));
		
		addCallback(new DefaultCallback());
		try {
			DefaultConnectionList.getSingleton().connect(getMasterCallback(), m_hostPort , startupCommandAry);
			Thread.sleep(DefaultFactory.getFactory().getConfig().getConnectWaitMs());
		} catch (InterruptedException e) {
			throw new ConnectionTimeout(m_hostPort, DefaultFactory.getFactory().getConfig().getConnectWaitMs());
		}
		
		IConnection c = DefaultConnectionList.getSingleton().locateConnection(m_hostPort);

		//TODO TODO TODO
//		NetworkDataReceiverThread2 ntt2 = c.getConn.getNetworkTraceThread2();
//		ntt2.addTraceWriter(m_traceWriter);
	}
	
	@Override
	public HostPort getHostPort() {
		return m_hostPort;
	}
	@Override
	public void setHostPort(HostPort hostPort) {
		this.m_hostPort = hostPort;
	}
	@Override
	public void disconnect() {
		IConnectionList connectionList = DefaultConnectionList.getSingleton();
		//IConnection cpt = connectionList.locateConnection(m_hostPort);
		try {
			connectionList.disconnect(this, getMasterCallback());
		} catch(DisconnectionException de) {
			//Since our goal is to disconnect, no need to do anything with the exception.
		}
	}
	@Override
	public ITraceWriter getTraceWriter() {
		return m_traceWriter;
	}
	@Override
	public void setTraceWriter(ITraceWriter traceWriter) {
		this.m_traceWriter = traceWriter;
	}
	@Override
	public boolean connect(String host, int port, IAgentCommand[] startupCommandAry)
			throws ConnectionTimeout, ConnectionException,
			BadCompletedRequestListener {
		HostPort hostPort = new HostPort(host,port);
		return connect(hostPort,startupCommandAry);
		
	}
	  /**
	   * Returns the count of callbacks still listening to 'this'.
	   * This method will perform a hard disconnect if 0 is returned.
	   * @param criteria
	   * @return
	   */
	  public int removeCallback(IConnectionStateCallback criteria) {
		  
		  List<IConnectionStateCallback> listCallbacks = getConnectionDetail().getConnCallbacks();
		  /**
		   * If this method is about to delete the last callback, then
		   * there are no windows left listening, so it is safe to
		   * do a hard disconnect.
		   */
		  if (listCallbacks.size() <= 1) {
			  getConnectionDetail().unHappyDisconnect();
			  listCallbacks.remove(criteria);
			  
			  DefaultConnectionList connList = (DefaultConnectionList) DefaultConnectionList.getSingleton();
			  connList.remove(this);
			  
		  } else {
			//utter a last dying gasp (DISCONNECTED status) before removing the callback.
			  //criteria.setConnectionState(ConnectState.DISCONNECTED);
			  //broadcastConnectionState(criteria);
			  listCallbacks.remove(criteria);
		  }
		  return listCallbacks.size();
	  }
	
	@Override
	public int removeConnectionStatusCallback(IConnectionStateCallback cb) {
		return removeCallback(cb);
		
	}
	@Override
	public boolean isConnected() {
		boolean ynRC = false;
		if (getConnectionDetail().getConnectState().equals(ConnectState.CONNECTED))
			ynRC = true;
		return ynRC;
	}
	@Override
	public void executeStartupCommands() {
		getConnectionDetail().executeStartupCommands();
		
	}
	@Override
	public NetworkDataReceiverThread2 getNetworkTraceThread2() {
		return getConnectionDetail().getNetworkTraceThread2();
	}
}
