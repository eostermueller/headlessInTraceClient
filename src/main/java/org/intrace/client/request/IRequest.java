package org.intrace.client.request;

import java.util.List;

import org.intrace.client.model.ITraceEvent;

/**
 * A value object that holds all traced events for a particular request/thread.
 * @author erikostermueller
 *
 */
public interface IRequestEvents {
	List<ITraceEvent> getRequestEvents();
	void setRequestEvents(List<ITraceEvent> events);
	void setThreadId(String threadId);
	public abstract String getThreadId();
}
