package org.headlessintrace.client.request;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import org.headlessintrace.client.model.ITraceEvent;

/**
 * A value object that holds all traced events for a particular request/thread.
 * @author erikostermueller
 *
 */
public interface IRequest extends Serializable {
	boolean isInitialized();
	void setInitialized(boolean val);
	List<ITraceEvent> getEvents();
	void setEvents(List<ITraceEvent> events);
	void setThreadId(String threadId);
	public abstract String getThreadId();
	String getUniqueId();
	void setUniqueId(String uniqueId);
	String getUrl();
	void setUrl(String val);
	String getHttpResponseCode();
	void setHttpResponseCode(String val);
}
