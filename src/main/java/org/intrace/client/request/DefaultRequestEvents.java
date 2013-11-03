package org.intrace.client.request;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.intrace.client.model.ITraceEvent;

public class DefaultRequestEvents implements IRequestEvents {
	private List<ITraceEvent> m_events = new CopyOnWriteArrayList<ITraceEvent>();

	private String m_threadId = null;
	@Override
	public List<ITraceEvent> getRequestEvents() {
		return m_events;
	}

	@Override
	public void setRequestEvents(List<ITraceEvent> events) {
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
