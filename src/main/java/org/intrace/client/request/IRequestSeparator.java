package org.intrace.client.request;

import java.util.concurrent.ConcurrentHashMap;

import org.intrace.client.filter.ITraceFilterExt;
import org.intrace.client.filter.ITraceFilterExt2;
import org.intrace.client.model.ITraceEvent;

/**
 * Generally, the implementor of this will be a singleton, processing all events.
 * @author erikostermueller
 *
 */
public interface IRequestSeparator {

	/**
	 * Calling this required method indicates to intrace the precise event that should trigger the end of
	 * event collection for a particular request (thread).
	 * The EXIT event for Thread.run is a very common choice, but might not work for all implementations.
	 * 
	 * @param m_requestCompletionEvent
	 * @when Generally, the consumer should invoke this just one time at startup.
	 */
	void setRequestCompletionFilter(ITraceFilterExt val);
	void setRequestStartFilter(ITraceFilterExt val);

	/**
	 * When the RequestCompletionEvent actually fires, intrace will invoke this method,
	 * passing all the events that were traced for that request/thread.
	 * @param requestCallback
	 * @when Generally, this consumer must invoke just one time at startup.
	 */
	void setCompletedRequestCallback(ICompletedRequestCallback requestCallback);

	/**
	 * Multiple threads may call this concurrently, so the implementation must be thread safe.
	 * @when This will be invoked repeatedly, once for every event fired by the intrace server agent.
	 */
	void add(ITraceEvent event);

	int size();

	ConcurrentHashMap getInFlightRequests();


}
