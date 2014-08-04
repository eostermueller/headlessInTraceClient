package org.headlessintrace.client;

import org.headlessintrace.client.connection.HostPort;

public interface HumanReadableMessages {
	String getConnectionTimeoutMessage(long timeoutMs, String host, int port);
	String getDisconnectionMessage(HostPort hostPort);
	String getConnected();
	public abstract String getInvalidHostMessage();
	public abstract String getInvalidPortMessage();
	public abstract String getAttemptingToConnect(HostPort hostPort);
	public abstract String getConnectionSuccessful(HostPort hostPort);
	public abstract String getFailedConnection(HostPort hostPort, Exception e);
	public abstract String getTestSetupInstructions();
	public abstract String getInTraceAgentDisconnected();
}
