package org.intrace.client.connection;

import org.intrace.client.ITraceWriter;
import org.intrace.client.connection.command.IAgentCommand;
import org.intrace.client.model.ITraceEvent;
import org.intrace.client.request.BadCompletedRequestListener;

import ca.odell.glazedlists.EventList;

public interface IConnection {

	EventList<ITraceEvent> getTraceEvents();
	int removeConnectionStatusCallback(IConnectionStateCallback cb);

	boolean connect(String host, int port) throws ConnectionTimeout,
			ConnectionException, BadCompletedRequestListener;

	boolean connect(String host, int port, IAgentCommand[] startupCommandAry)
			throws ConnectionTimeout, ConnectionException, BadCompletedRequestListener;
	boolean connect(HostPort hostPort, IAgentCommand[] startupCommandAry)
			throws ConnectionTimeout, ConnectionException, BadCompletedRequestListener;

	void disconnect();

	ITraceWriter getTraceWriter();

	void setTraceWriter(ITraceWriter traceWriter);

	boolean isConnected();

	void setCommandArray(IAgentCommand[] startupCommands);
	void executeStartupCommands();
	HostPort getHostPort();

	NetworkDataReceiverThread2 getNetworkTraceThread2();

	public abstract int getConnCallbackSize();

	public abstract void addCallback(IConnectionStateCallback callback);

	public abstract void setHostPort(HostPort hostPort);
	public abstract IConnectionStateCallback getMasterCallback();
	

}