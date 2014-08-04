
package org.intrace.client.connection;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.intrace.client.DefaultFactory;
import org.intrace.client.ITraceWriter;
import org.intrace.client.filter.ITraceFilter;
import org.intrace.client.filter.ITraceFilterExt;
//import org.intrace.client.filter.IncludeThisEventFilterExt;
import org.intrace.client.model.ITraceEvent;
import org.intrace.shared.Base64;
import org.slf4j.LoggerFactory;

import ca.odell.glazedlists.EventList;

/**
 * Reads "trace events" from Socket, writes them to a List.
 * The version of this class that does _not_ have the "2" suffix is used for the pre-RCP InTrace.
 * Writing to the list is required for using the "NatTable" grid, which is a high-data-volume, high-performance data display component.
 * Multiple UI components can receive data from a single component -- this is a big requirement of RCP-InTrace.
 * For instance, user must be able to view both a graph and a text trace of activity from a single JVM.
 * <p>
 * I considered inheriting from the original NetworkDataReceiverThread, but decided against it b/c the parent class would, confusingly, have a superfluous "TraceFilterThread" field, which would be unused and be a "fifth wheel".
 * The "2" implementation adds a field and re-factors the "run" method to use add to the list instead of the TraceFilterThread. 
 * @author e0018740
 *
 */
public class NetworkDataReceiverThread2 implements Runnable
{
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(NetworkDataReceiverThread2.class);
    //private static final Logger LOG = Logger.getLogger( NetworkDataReceiverThread2.class.getName() );
  public static interface INetworkOutputConfig
  {
    public boolean isNetOutputEnabled();

	public boolean isGzipEnabled();
  }
  private List<ITraceWriter> m_traceWriters = new CopyOnWriteArrayList<ITraceWriter>();
  public List<ITraceWriter> getTraceWriters() {
	  //System.out.println("@#^$535 NetworkDataReceiverThread2 found TraceWriters list[" + m_traceWriters.hashCode() + "]");
	  
	  return m_traceWriters;
  }
  public void addTraceWriter(ITraceWriter tw) {
	  if (LOG.isDebugEnabled())
		  LOG.debug("This NetworkDataReceiverThread2 [" + this.hashCode() + "] is adding TraceWriter [" + tw.hashCode() + "]");
	  getTraceWriters().add(tw);
  }
  /**
   * Remove any trace writers that are writing to the given window name.
   * @param nameCriteria
   */
  public void removeTraceWriter(String nameCriteria) {
	  for(ITraceWriter tw : getTraceWriters()) {
		  if (LOG.isDebugEnabled()) LOG.debug("Trying to remove a trace writer.  Looking for [" + nameCriteria + "] found [" + tw.getName() + "]");
		  if (tw.getName().equals(nameCriteria))
			  getTraceWriters().remove(tw);
	  }
  }
  private void writeTraceEvent(String traceData) {
	  
	  if (LOG.isDebugEnabled()) LOG.debug("@%# event [" + traceData + "] writing to [" + getTraceWriters().size() + "] TraceWriters");
	  for (ITraceWriter tw : getTraceWriters() ) 
		  tw.writeTraceEvent(traceData, getHostPort());
  }
  
  
  private boolean m_consumerRequestedDisconnect = false;
  public boolean consumerRequestedDisconnect() {
	  return m_consumerRequestedDisconnect;
  }
  public void requestDisconnect() {
	  m_consumerRequestedDisconnect = true;
  }
  private final Socket traceSocket;
  private final INetworkOutputConfig outputConfig;
private HostPort m_hostPort = null;

	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("hash [" + this.hashCode() + "]\n");
		sb.append("m_consumerRequestedDisconnect [" + m_consumerRequestedDisconnect + "]\n");
		sb.append("traceSocket.isClosed() [" + traceSocket.isClosed() + "]\n");
		//sb.append("traceSocket.isConnected() [" + traceSocket.isConnected() + "]\n");
		sb.append("count of trace writers [" + getTraceWriters().size()  + "]\n");
		
		return sb.toString();
	}
  public NetworkDataReceiverThread2(InetAddress address, int networkTracePort,
      INetworkOutputConfig outputConfig)
      throws IOException
  {
	setHostPort(new HostPort(address.getHostAddress(), networkTracePort));
    this.outputConfig = outputConfig;
    traceSocket = new Socket();
    traceSocket.connect(new InetSocketAddress(address, networkTracePort));
  }

  private void setHostPort(HostPort hostPort) {
	m_hostPort = hostPort;
  }
  private HostPort getHostPort() {
	  return m_hostPort;
  }
public void start()
  {
    Thread t = new Thread(this);
    t.setDaemon(true);
    t.setName("Headless InTrace Network Data Receiver");
    t.start();
  }

  @Override
  public void run()
  {
    try
    {
      ObjectInputStream objIn = new ObjectInputStream(traceSocket.getInputStream());
      while (true)
      {
    	  Object data = null;
    	  try {
    	        data = objIn.readObject();    		  
    	  } catch (Exception e) {
    		  if (!consumerRequestedDisconnect())//If consumer asked for a disconnect, no need to throw one.
    		  throw new DisconnectionException(DefaultFactory.getFactory().getMessages().getDisconnectionMessage(getHostPort()));
    		  //Consider starting a timer to attempt to reconnected every minute or so?
    	  }

        if (data instanceof String)
        {
          String traceLine = (String) data;
          if (!"NOOP".equals(traceLine))
          {
        	  /**
        	   * Erik Ostermueller
        	   * May 9, 2012
        	   * TODO:  Can't figure out how to enable this flag, so I've
        	   * temporarily commented out this check.
        	   */
            //if (outputConfig.isNetOutputEnabled())
            {
                if (outputConfig.isGzipEnabled()) {
              	  byte[] tmp = Base64.decode(traceLine);
              	  traceLine = new String(tmp);
                }
            	writeTraceEvent(traceLine);
            }
          }
        }
      }
    }
    catch (Exception e)
    {
    	e.printStackTrace();
      disconnect();
    }
  }

  public void disconnect()
  {
    try
    {
      traceSocket.close();
    }
    catch (IOException e)
    {
      // Do nothing
    }
  }


}
