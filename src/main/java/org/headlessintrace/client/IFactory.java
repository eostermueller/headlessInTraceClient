package org.headlessintrace.client;

import org.headlessintrace.client.connection.IConnection;
import org.headlessintrace.client.connection.IConnectionStateCallback;
import org.headlessintrace.client.model.ITraceEvent;
import org.headlessintrace.client.model.ITraceEventParser;
import org.headlessintrace.client.model.DefaultTraceEvent;
import org.headlessintrace.client.request.IRequest;
import org.headlessintrace.client.request.IRequestSeparator;
import org.headlessintrace.jdbc.IJdbcProvider;

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
