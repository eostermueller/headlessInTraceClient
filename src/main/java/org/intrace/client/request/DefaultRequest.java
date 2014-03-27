package org.intrace.client.request;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import org.intrace.client.model.ITraceEvent;

public class DefaultRequest implements IRequest {
	private List<ITraceEvent> m_events = new CopyOnWriteArrayList<ITraceEvent>();
	private String m_uniqueId = null;
	
	public DefaultRequest() {
		setUniqueId(UUID.randomUUID().toString());
	}

	@Override
	public String getUniqueId() {
		return m_uniqueId.toString();
	}

	@Override
	public void setUniqueId(String uniqueId) {
		this.m_uniqueId = uniqueId;
	}
	private String m_threadId = null;
	@Override
	public List<ITraceEvent> getEvents() {
		return m_events;
	}

	@Override
	public void setEvents(List<ITraceEvent> events) {
		m_events = events;
	}

	@Override
	public void setThreadId(String threadId) {
		m_threadId = threadId;
	}
	@Override
	public String getThreadId(){ 
		return m_threadId;
	}

}
