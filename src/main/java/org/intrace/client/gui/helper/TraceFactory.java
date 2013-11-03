package org.intrace.client.gui.helper;


import java.io.IOException;
import java.net.InetAddress;

import org.intrace.client.DefaultFactory;
import org.intrace.client.model.BeanTraceEventImpl;
import org.intrace.client.model.DefaultTraceEventParser;
import org.intrace.client.model.ITraceEvent;
import org.intrace.client.model.ITraceEventParser;

public class TraceFactory {

	static ITraceEventParser m_eventParser = DefaultFactory.getFactory().getEventParser();
	
	public static BeanTraceEventImpl createTraceEvent(String traceLine) {
		// TODO Auto-generated method stub
		/**
		 * TODO:  this is a temporary measure.
		 * Ultimately, all the separate fields need to be parsed out.
		 */
		//TraceEvent te = new TraceEventImpl(0,"<threadId>", "<packageName>", "<className>", traceLine, "<eventType>");
		BeanTraceEventImpl te = new BeanTraceEventImpl(traceLine, "TRACE");
		return te;
	}


	public static BeanTraceEventImpl createExceptionTraceEvent(
			String traceCreationError, IOException ex,
			InetAddress m_remoteAddress, String networkTracePortStr) {
		// TODO Auto-generated method stub
		StringBuilder sb = new StringBuilder();
		sb.append(traceCreationError).append("-")
			.append(m_remoteAddress.toString()).append("-")
			.append(networkTracePortStr);
		
		
		BeanTraceEventImpl te = new BeanTraceEventImpl(sb.toString(), "EXCEPTION", ex);
		
		return te;
	}

}
