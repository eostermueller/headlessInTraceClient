package org.intrace.client.connection;

import org.intrace.client.DefaultFactory;
import org.intrace.client.IntraceException;

public class ConnectionTimeout extends IntraceException {
	HostPort hostPort = null;
	long timeoutMs = -1;
	ConnectionTimeout(HostPort hostPortVal, long timeoutMsVal) {
		hostPort = hostPortVal;
		timeoutMs = timeoutMsVal;
	}
	public String getMessage() {
		return DefaultFactory.getFactory().getMessages().getConnectionTimeoutMessage(timeoutMs, hostPort.hostNameOrIpAddress, hostPort.port);
	}
}
