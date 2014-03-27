package org.intrace.client.connection;

import java.util.List;

import org.intrace.client.connection.command.IAgentCommand;
import org.intrace.client.filter.ITraceFilterExt;
import org.intrace.client.request.BadCompletedRequestListener;

public interface IConnectionList {

	/**
	 * TODO:  Add code to see if we're already connected to this host.
	 * @param host
	 * @param port
	 * @return
	 */
	IConnection locateConnection(HostPort hostPort);

	/**
	 * 
	 * @param conn2
	 * @param connectionCallback
	 */
	int disconnect(IConnection conn2,
			IConnectionStateCallback connectionCallback);

	IConnection connect(IConnectionStateCallback connectionCallback,
			HostPort hostPort, IAgentCommand[] startupCommands)
			throws ConnectionException, ConnectionTimeout, BadCompletedRequestListener;

	int size();
	List<IConnection> getConnections();

	IConnection connect(IConnectionStateCallback connectionCallback,
			HostPort hostPort, IAgentCommand[] startupCommands,
			ITraceFilterExt filter) throws ConnectionException,
			ConnectionTimeout, BadCompletedRequestListener;

	void add(HostPort hostPort, IConnection c);

}