package org.intrace.client;

import org.intrace.client.connection.IConnection;
import org.intrace.client.connection.IConnectionStateCallback;
import org.intrace.client.model.ITraceEvent;
import org.intrace.client.model.ITraceEventParser;
import org.intrace.client.model.DefaultTraceEvent;
import org.intrace.client.request.IRequest;
import org.intrace.client.request.IRequestSeparator;
import org.intrace.jdbc.IJdbcProvider;

public interface IFactory {
	Config getConfig();
	void setConfig(Config val);
	HumanReadableMessages getMessages();
	void setMessages(HumanReadableMessages val);
	ITraceEventParser getEventParser();
	ITraceWriter getTraceWriter();
	IConnection getDormantConnection();
	ITraceEvent getTraceEvent();
	IRequestSeparator getRequestSeparator();
	IRequest getRequest();
	IConnectionStateCallback getCallback();
	IJdbcProvider getJdbcProvider();
}
