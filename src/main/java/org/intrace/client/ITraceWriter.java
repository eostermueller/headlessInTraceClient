package org.intrace.client;

import org.intrace.client.connection.HostPort;
import org.intrace.client.filter.ITraceFilter;
import org.intrace.client.filter.ITraceFilterExt;
import org.intrace.client.model.ITraceEvent;

import ca.odell.glazedlists.EventList;

public interface ITraceWriter {
	  public void writeTraceEvent(String traceData, HostPort hostPort);
		public String getName();
		public void setName(String name);
		public void setTraceEvents(EventList<ITraceEvent> traceEvents);
		public EventList<ITraceEvent> getTraceEvents();
		public void setTraceFilter(ITraceFilter filter);
		public ITraceFilter getTraceFilter();
		public void setTraceFilterExt(ITraceFilterExt filter);
		public ITraceFilterExt getTraceFilterExt();

}
