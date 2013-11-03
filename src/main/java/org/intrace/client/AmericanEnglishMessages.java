package org.intrace.client;

import java.util.Arrays;

import org.intrace.client.connection.HostPort;

public class AmericanEnglishMessages implements HumanReadableMessages {

	@Override
	public String getConnectionTimeoutMessage(long timeoutMs, String host,
			int port) {
		return String.format("[%ld]ms expired while waiting for an InTrace agent to respond on host [%s] and port [%ld]");
	}
	@Override
	public String getDisconnectionMessage(org.intrace.client.connection.HostPort hostPort) {
		return "InTrace Agent connection to [" + hostPort.toString3() + "]";
	}
	@Override
	public String getConnected() {
		return "Connected";
	}
	@Override
	public String getInvalidHostMessage() {
		return "Error: Please enter an proper hostname or IP address";
	}
	@Override
	public String getInvalidPortMessage() {
		return "Error:  Please enter a port number greater than 0 and less than 65535";
	}
	@Override
	public String getAttemptingToConnect(HostPort hostPort) {
		return "Attempting to connect to host [" + hostPort.key() + "]";
	}
	@Override
	public String getConnectionSuccessful(HostPort hostPort) {
		return "Connection to [" + hostPort.key() + "] was successful";
	}
	@Override
	public String getFailedConnection(HostPort hostPort, Exception e) {
		return "Error connecting to [" + hostPort.key() + "] message ["  + e.getLocalizedMessage() + "] stack trace [" + Arrays.toString(e.getStackTrace()) + "]";
	}
	@Override
	public String getTestSetupInstructions() {
		return "Here is how you run the unit tests.....";
	}
}
