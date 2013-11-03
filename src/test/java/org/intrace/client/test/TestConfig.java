package org.intrace.client.test;

import org.intrace.client.connection.HostPort;

public class TestConfig {
	private HostPort httpIntraceTestWebAppServer = null;
	HostPort inTraceAgentServer = null;
	public HostPort getInTraceAgentServer() {
		return inTraceAgentServer;
	}
	public void setInTraceAgentServer(HostPort inTraceAgentServer) {
		this.inTraceAgentServer = inTraceAgentServer;
	}
	public HostPort getExampleWebApp() {
		return httpIntraceTestWebAppServer;
	}
	public void setHttpExampleWebApp(HostPort httpTestWebAppServer) {
		this.httpIntraceTestWebAppServer = httpTestWebAppServer;
	}

}
