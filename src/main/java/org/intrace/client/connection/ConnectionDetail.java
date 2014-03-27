package org.intrace.client.connection;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import org.intrace.client.DefaultFactory;
import org.intrace.client.connection.NetworkDataReceiverThread2.INetworkOutputConfig;
import org.intrace.client.connection.command.IAgentCommand;
import org.intrace.client.gui.helper.ControlConnectionThread;
import org.intrace.client.gui.helper.TraceFactory;
import org.intrace.client.gui.helper.ParsedSettingsData;
import org.intrace.client.gui.helper.ControlConnectionThread.IControlConnectionListener;
import org.intrace.client.model.BeanTraceEventImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class manages the control and trace threads for a single connection.
 * Theoretically, there is a one-to-many relationship between this class an a UI window displaying text/graph data from this connection. 
 * This holds true in practice with IConnectionStateCallbackHandlers.
 * In other words, there is one ConnectionController to many IConnectionStateCallbackHandlers.
 * You'll notice, however, that there is not a similar one-to-many for TraceEvents.
 * Instead, window have NatTable grids, and each grid for this connection "listens" to changes in this.traceEvents.
 * See NatTable doc for details on how this is done. 
 * It also performs the following tasks with the connection control thread:
 * <ul>
 * 		<li>maintains the reference to the thread</li>
 * 		<li>starts/initializes it</li>
 * 		<li>stops it</li>
 * 		<li>keeps up-to-date on the connection status</li>
 * </ul>
 * 
 * Much of the initial code was performed by the InTraceUI class.
 * 
 * There are two ways to filter the list of events.
 * 1) Take the EventList field of this class (getTraceEvents) and use it to construct a NatTable,
 * as shown in the NatTable example here:  C:\src\jdist\natTable\2.3.2\example (net.sourceforge.nattable.examples.examples._101_Data.Using_the_ListDataProvider_MT)
 * The filter should consider two pieces for include/exclude:
 * 		a) The window's list of classes
 * 		b) The window's list of filters
 * 2) For Windows not using NatTable (Gantt, aggregation chargs), need to create a FilteredDispatchClass
 * that subscribes to all events here:
 * 			getTraceEvents().addListEventListener(listChangeListener)
 *  ...but only dispatches those events that agree with the a) and b) data items detailed above.
 *  
 * @author Erik Ostermueller
 * @place San Francisco
 * @date May 1, 2012
 *
 */
public class ConnectionDetail implements IControlConnectionListener, IConnectionStateCallback {
	//private static final Logger LOG = Logger.getLogger( ConnectionDetail.class.getName() );
	private static final Logger LOG = LoggerFactory.getLogger(ConnectionDetail.class);
	  // Settings

	
	private List<IConnectionStateCallback> m_connCallbacks = new CopyOnWriteArrayList<IConnectionStateCallback>();
	  private ConnectState m_connectState = ConnectState.DISCONNECTED;
	  /**
	   * Commands that the user would like executed at startup time.
	   */
	  private IAgentCommand[] m_startupCommands = null;
	  private ParsedSettingsData m_settingsData = new ParsedSettingsData(
		      new HashMap<String, String>());
	  // Network details
	  private InetAddress m_remoteAddress;
	  private int port = HostPort.UNINITIALIZED_PORT;
	  public ConnectState getConnectState() {
		  return m_connectState;
	  }
	  /**
	   * Immediately after the connection is first established, execute all the commands in this.m_startupCommands.
	   * This method concatenates the commands of all those given in the array and attempts to execute them
	   * in a single round-trip call.
	   */
	  void executeStartupCommands() {
		  if (m_startupCommands !=null) {
			  StringBuilder sb = new StringBuilder();
			  for (IAgentCommand cmd : m_startupCommands) {
				  sb.append(cmd.getMessage());
			  }
			  getControlThread().sendMessage(sb.toString());
		  }
		  
	  }
	  @Override
	  public String toString() {
		  StringBuilder sb = new StringBuilder();
		  
		  
		  sb.append("Connection state [" + this.m_connectState + "]\n");
		  if (m_remoteAddress!=null) {
			  sb.append(m_remoteAddress.getCanonicalHostName()).append("-->CanonicalHostName\n");
			  sb.append(m_remoteAddress.getHostAddress()).append("-->HostAddress\n");
			  sb.append(m_remoteAddress.isLinkLocalAddress()).append("-->isLinkLocal\n");
		  }
		  
		  return sb.toString();
	  }

	  // Threads
	  private NetworkDataReceiverThread2 m_networkTraceThread2 = null;
	  private ControlConnectionThread m_controlThread;
	  /**
	   * Multiple NatTable windows will listen for events being added to this single list for this single connection.
	   * CopyOnWriteArrayList would NOT work well, because the number of events could grow large....don't want to expend the 
	   * overhead of repeatedly copying a list that is frequently added to. 
	   */
	  private EventList<BeanTraceEventImpl> m_traceEvents = new BasicEventList<BeanTraceEventImpl>(); 
	  
	  /**
	 * @todo: Delete activity to all listening windows.
	 */
	@Override
	public void setProgress(Map<String, String> progress) {
		// TODO Multiplex to listening windows.
		for(IConnectionStateCallback cb : this.m_connCallbacks)
			cb.setProgress(progress);
	}

	/**
	 * @todo: Delete activity to all listening windows.
	 */
	@Override
	public void setStatus(Map<String, String> progress) {
		// TODO Multiplex to listening windows.
		for(IConnectionStateCallback cb : this.m_connCallbacks)
			cb.setStatus(progress);
	}

	/**
	 * @todo: Delegate activity to all listening windows.
	 */
	@Override
	public void setConfig(Map<String, String> settingsMap) {
		m_settingsData = new ParsedSettingsData(settingsMap);		
		LOG.debug("Client got updated config from server. gzip [" + m_settingsData.gzipEnabled + "] all settings[" + m_settingsData.toString() + "]");
		for(IConnectionStateCallback cb : this.m_connCallbacks)
			cb.setConfig(settingsMap);
	}

	
	/**
	 * TODO:  Would really like to make this private, but cannot because it is public in the super.
	 * Why do I want this to be private?  Because the disconnect should only happen when _all_ windows have finished using the connection,
	 * not just when a single window disconnects.
	 * 
	 * This should only used when a problem has been detected with the connection.
	 * If you must requests a disconnect, please use ConnectionList.disconnect() 
	 */
	@Override
	public void disconnect() {
		getNetworkTraceThread2().requestDisconnect();
		//throw new UnsupportedOperationException(ClientStrings.HARD_DISSCONNECT_NO_LONGER_SUPPORTED);
		
		//ControlConnectionThread#run is calling the above method, so can't throw the above exception quite yet.
		unHappyDisconnect();
	}
	void unHappyDisconnect() {
	    if (m_controlThread != null)
	    {
	      m_controlThread.disconnect();
	    }
	    if (m_networkTraceThread2 != null)
	    {
	      m_networkTraceThread2.disconnect();
	    }
	    setConnectState(ConnectState.DISCONNECTED);
		
	}
	public boolean isConnected() {
		return (getConnectState().equals(ConnectState.CONNECTED));
	}
	
	
/**
 * The following isHealthy() looks like good code, but not sure if I need it yet.	
 */
	
//	/**
//	 * TODO:  ask Martin whether there is a better way to implement a health check.
//	 * @return
//	 */
//	public boolean isHealthy() {
//		boolean ynHealthy = false; //assume that connection is bad.
//	  m_controlThread.sendMessage("[out-network");
//	  String networkTracePortStr = m_controlThread.getMessage();
//	  int networkTracePort = -1;
//	  try {
//		  networkTracePort = Integer.parseInt(networkTracePortStr);
//		  if (networkTracePort > 0) {
//			  ynHealthy = true;
//		  }
//	  } catch (Exception e) {
//		  ynHealthy = false;
//	  }
//	  return ynHealthy;
//	}

	//TODO ?
//	/**
//	 * 
//	 * @return Returns null if IP was not available at time of call, probably b/c the connection was not complate.
//	 * @throws Exception
//	 */
//	public HostPort getHostPortIfConnected() {
//		HostPort rc = null;
//		if (m_remoteAddress!=null) {
//			rc = new HostPort();
//			rc.hostNameOrIpAddress = getIp();
//			rc.port = this.port;
//			rc.setIp(getIp());
//		}
//		return rc;
//	}
	/**
	 * Why do we have ConnectionPartOne and ConnectionPartTwo?
	 * There is a hand off, and it happens right here.
	 * Once ConnectionPartOne is finished with it's business, 
	 * it invokes this setCocket() method.
	 */
	public void setSocket(Socket socket) {
	    if (socket != null)
	    {
	      m_remoteAddress = socket.getInetAddress();
	      this.port = socket.getPort();
	      m_controlThread = new DebugControlConnectionThread(socket, this);
	      m_controlThread.start();
	      executeStartupCommands();
//	      m_controlThread.sendMessage(AgentConfigConstants.INSTRU_ENABLED+"true");
	      m_controlThread.sendMessage("getsettings");

	      m_controlThread.sendMessage("[out-network");
	      //m_controlThread.sendMessage("[out-network-true");
	      String networkTracePortStr = m_controlThread.getMessage();
	      int networkTracePort = Integer.parseInt(networkTracePortStr);
	      if (LOG.isDebugEnabled()) LOG.debug("Received network trace port [" + networkTracePort + "]");
	      
	      try
	      {
	        INetworkOutputConfig config = new INetworkOutputConfig()
	        {
		          @Override
		          public boolean isNetOutputEnabled()
		          {
		            return m_settingsData.netOutEnabled;
		          }

				@Override
				public boolean isGzipEnabled() {
					return m_settingsData.gzipEnabled;
				}
	        };
	        	        
	        m_networkTraceThread2 = new NetworkDataReceiverThread2(
	        							m_remoteAddress,
	        							networkTracePort, 
	        							config
	        							);
	        m_networkTraceThread2.start();
		      setConnectState(ConnectState.CONNECTED);
		      setConnectionStatusMsg(DefaultFactory.getFactory().getMessages().getConnected());
	        
	      } catch (IOException ex)
	      {
	    	  m_traceEvents.add(
	    			  	TraceFactory.createExceptionTraceEvent(
	    			  			org.intrace.rcp.ClientStrings.TRACE_CREATION_ERROR, 
	    			  			ex, 
	    			  			m_remoteAddress, 
	    			  			networkTracePortStr));
	      }
	      
	    } else
	    {
	      setConnectState(ConnectState.DISCONNECTED_ERR);
	    }
	}
	
	/**
	 * TODO:  Need to see if setConnectionState and this method do the same thing, so one can be eliminated.
	 */
	public void setConnectionStatusMsg(String statusText) {
		for(IConnectionStateCallback callback : this.m_connCallbacks)
			callback.setConnectionStatusMsg(statusText);
	}
	
	/**
	 * TODO:  Need to see if setConnectionStatus and this method do the same thing, so one can be eliminated.
	 * @param connectionState
	 */
	  public void setConnectState(ConnectState connectionState)
	  {
		  synchronized(this.m_connectState) {
			  this.m_connectState = connectionState;
		  }
	    broadcastConnectionState();
	  }
	  public void broadcastConnectionState() {
	    for(IConnectionStateCallback callback : this.m_connCallbacks)
	    	callback.setConnectState(getConnectState());
	    	
	  }
//	  public void broadcastConnectionState(IConnectionStateCallback callback) {
//		  callback.setConnectState(getConnectState());
//	  }
		  
//	  /**
//	   * TODO: This needs some serious unit testing.
//	   * @param connCallbackToRemove
//	   */
//	  public void removeConnCallback(IConnectionStateCallback connCallbackToRemove) {
//		  for(IConnectionStateCallback connCallback : this.m_connCallbacks) {
//			  if (connCallback==connCallbackToRemove) {
//				  this.m_connCallbacks.remove(connCallbackToRemove);
//			  }
//		  }
//	  }
	  public List<IConnectionStateCallback> getConnCallbacks() {
		  return m_connCallbacks;
	  }
	  
	public void addConnCallback(IConnectionStateCallback connCallback) {
		if (connCallback!=null) {
			this.m_connCallbacks.add(connCallback);
			connCallback.setConnectState(m_connectState);
		}
	}

	public EventList<BeanTraceEventImpl> getTraceEvents() {
		return m_traceEvents;
	}

	public void setTraceEvents(EventList<BeanTraceEventImpl> traceEvents) {
		this.m_traceEvents = traceEvents;
	}

	public NetworkDataReceiverThread2 getNetworkTraceThread2() {
		return m_networkTraceThread2;
	}

	public void setNetworkTraceThread2(
			NetworkDataReceiverThread2 networkTraceThread2) {
		this.m_networkTraceThread2 = networkTraceThread2;
	}

	public ControlConnectionThread getControlThread() {
		return m_controlThread;
	}

	public void setControlThread(ControlConnectionThread controlThread) {
		this.m_controlThread = controlThread;
	}
	public IAgentCommand[] getStartupCommands() {
		return m_startupCommands;
	}
	public void setCommandArray(IAgentCommand[] startupCommands) {
		this.m_startupCommands = startupCommands;
	}

}
class DebugControlConnectionThread extends ControlConnectionThread {
	private static final Logger LOG = LoggerFactory.getLogger(DebugControlConnectionThread.class);
	public DebugControlConnectionThread(Socket socket,
			IControlConnectionListener listener) {
		super(socket, listener);
	}
	@Override
	public void sendMessage(String xiString) {
		if (LOG.isDebugEnabled()) LOG.debug("sendMessage[" + xiString + "]");
		super.sendMessage(xiString);
	}
}