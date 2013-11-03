package org.intrace.client.connection;

import java.net.InetSocketAddress;


import java.net.Socket;
import java.util.Arrays;

//import org.intrace.client.gui.helper.Connection.ISocketCallback;

/**
 * This code was 99% stolen from the connection class
 * Removed UI references.
 * Once the socket is opened, this object is out of the picture.
 * @author e0018740
 *
 */
public class ConnectionPartOne {

	  public static interface _ISocketCallback
	  {
	    public void setSocket(Socket socket);
	    public void setConnectionStatus(final String statusText);
	  }
	  
	  public static void connectToAgent(final _ISocketCallback socketCallback,
	                                    final HostPort hostPort)
	  {
	    if (hostPort.hostNameOrIpAddress.length() == 0)
	    {
	      socketCallback.setConnectionStatus("Error: Please enter an address");
	      socketCallback.setSocket(null);
	    }
	    else if (hostPort.port <= 0)
	    {
	      socketCallback.setConnectionStatus("Error: Please enter a port");
	      socketCallback.setSocket(null);
	    }
	    else
	    {
	      new Thread(new Runnable()
	      {
	        @Override
	        public void run()
	        {
	          socketCallback.setConnectionStatus("Connecting...");
	          final Socket socket = new Socket();
	          try
	          {
	            socket.connect(new InetSocketAddress(hostPort.hostNameOrIpAddress, hostPort.port));            
	            socketCallback.setSocket(socket);
	          }
	          catch (Exception e)
	          {
	        	  
	            socketCallback.setConnectionStatus("Error connecting to [" + hostPort.key() + "--" + e.getMessage() + " stack: " + Arrays.toString(e.getStackTrace()));
	            socketCallback.setSocket(null);
	          }
	        }
	      }).start();
	    }
	  }
}
