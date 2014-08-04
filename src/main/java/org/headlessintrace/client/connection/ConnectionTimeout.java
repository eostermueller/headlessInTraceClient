package org.headlessintrace.client.connection;

import org.headlessintrace.client.DefaultFactory;
import org.headlessintrace.client.IntraceException;

public class ConnectionTimeout extends IntraceException {
	HostPort hostPort = null;
	long timeoutMs = -1;
	
	public ConnectionTimeout(HostPort hostPortVal, long timeoutMsVal) {
		hostPort = hostPortVal;
		timeoutMs = timeoutMsVal;
	}
	public String getMessage() {
		return DefaultFactory.getFactory().getMessages().getConnectionTimeoutMessage(timeoutMs, hostPort.hostNameOrIpAddress, hostPort.port);
	}
}
