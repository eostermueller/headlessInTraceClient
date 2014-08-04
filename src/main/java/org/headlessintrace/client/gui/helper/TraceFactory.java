package org.headlessintrace.client.gui.helper;


import java.io.IOException;
import java.net.InetAddress;

import org.headlessintrace.client.DefaultFactory;
import org.headlessintrace.client.model.BeanTraceEventImpl;
import org.headlessintrace.client.model.DefaultTraceEventParser;
import org.headlessintrace.client.model.ITraceEvent;
import org.headlessintrace.client.model.ITraceEventParser;

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
