package org.intrace.client.request;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import org.intrace.client.model.ITraceEvent;

/**
 * A value object that holds all traced events for a particular request/thread.
 * @author erikostermueller
 *
 */
public interface IRequest extends Serializable {
	List<ITraceEvent> getEvents();
	void setEvents(List<ITraceEvent> events);
	void setThreadId(String threadId);
	public abstract String getThreadId();
	String getUniqueId();
	void setUniqueId(String uniqueId);
}
