package org.intrace.client.gui.helper;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.intrace.client.DefaultFactory;
import org.intrace.client.request.RequestWriter;
import org.intrace.shared.AgentConfigConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**   No changes were made to this class, when creating the headless intrace connection API
  *
  */
public class ControlConnectionThread implements Runnable
{
    private static final Logger LOG = LoggerFactory.getLogger( ControlConnectionThread.class );
    public static interface IControlConnectionListener
  {
    public void setProgress(Map<String,String> progress);
    public void setStatus(Map<String,String> progress);
    public void setConfig(Map<String,String> progress);
    public void disconnect();
  }
    public String toString() {
    	StringBuffer sb = new StringBuffer();
    	sb.append("hash [" + this.hashCode() + "]\n");
    	sb.append("socket.isClosed [" + socket.isClosed() + "]\n");
    	sb.append("socket.isConnected [" + socket.isConnected() + "]\n");
    	//sb.append("Listener [" + listener.toString() + "]");
    	sb.append("senderThread [" + senderThread.toString() + "]\n");
    	sb.append("sendThread [" + sendThread.toString() + "]\n");
    	return sb.toString();
    }
  
  private final Socket socket;
  private final IControlConnectionListener listener;
  private final BlockingQueue<String> incomingMessages = new LinkedBlockingQueue<String>();
  private final BlockingQueue<String> outgoingMessages = new LinkedBlockingQueue<String>();
  private final ControlConnectionSenderThread senderThread = new ControlConnectionSenderThread();
  private Thread sendThread;

  public ControlConnectionThread(Socket socket, IControlConnectionListener listener)
  {
    this.listener = listener;
    this.socket = socket;
  }

  public void start()
  {
    Thread receiveThread = new Thread(this);
    receiveThread.setDaemon(true);
    receiveThread.setName("Control Receive Thread");
    receiveThread.start();

    sendThread = new Thread(senderThread);
    sendThread.setDaemon(true);
    sendThread.setName("Control Sender Thread");
    sendThread.start();
  }

  public String getMessage()
  {
    try
    {
      return incomingMessages.take();
    }
    catch (InterruptedException e)
    {
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public void run()
  {
    try
    {
      while (true)
      {
        ObjectInputStream objIn = new ObjectInputStream(socket.getInputStream());
        Object receivedMessage = objIn.readObject();
        if (receivedMessage instanceof Map<?, ?>)
        {
          Map<String, String> map = (Map<String, String>) receivedMessage;
          if (map.containsKey(AgentConfigConstants.NUM_PROGRESS_ID))
          {
            listener.setProgress(map);
          }
          else if (map.containsKey(AgentConfigConstants.STID))
          {
            listener.setStatus(map);
          }
          else
          {
            listener.setConfig(map);
          }
        }
        else
        {
          String strMessage = (String) receivedMessage;
          if (!"OK".equals(strMessage))
          {
            incomingMessages.put(strMessage);
          }
        }
      }
    }
    catch (Exception ex)
    {
      if (ex==null || ex.getMessage()==null) {
    	  listener.disconnect();
    	  LOG.error(DefaultFactory.getFactory().getMessages().getInTraceAgentDisconnected());
      } else if (!ex.getMessage().contains("ocket closed") &&
          !ex.getMessage().contains("onnection reset"))
      {
        ex.printStackTrace();
      }
      listener.disconnect();
    }
  }

  public void disconnect()
  {
    if (sendThread != null)
    {
      sendThread.interrupt();
    }
    try
    {
      socket.close();
    }
    catch (IOException e)
    {
      // Throw away
    }
  }

  public void sendMessage(String xiString)
  {
    try
    {
      outgoingMessages.put(xiString);
    }
    catch (InterruptedException e1)
    {
      // Throw away
    }
  }

  private class ControlConnectionSenderThread implements Runnable
  {
    @Override
    public void run()
    {
      try
      {
        while (true)
        {
          String message = outgoingMessages.take();
          OutputStream out = socket.getOutputStream();
          ObjectOutputStream objOut = new ObjectOutputStream(out);
          objOut.writeObject(message);
          objOut.flush();
        }
      }
      catch (Exception e)
      {
        listener.disconnect();
      }
    }
  }
}
