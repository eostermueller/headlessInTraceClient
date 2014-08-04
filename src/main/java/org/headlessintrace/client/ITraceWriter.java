package org.headlessintrace.client;

import org.headlessintrace.client.connection.HostPort;
import org.headlessintrace.client.filter.ITraceFilter;
import org.headlessintrace.client.filter.ITraceFilterExt;
import org.headlessintrace.client.model.ITraceEvent;

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
