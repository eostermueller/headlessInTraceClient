package org.intrace.client.request;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.intrace.client.DefaultFactory;
import org.intrace.client.filter.IncludeThisEventFilterExt;
import org.intrace.client.model.ITraceEvent;
import org.intrace.client.request.IRequestEvents;
import org.intrace.client.test.level3.request.TestMultiThreadedRequestEventCollection;
public class DefaultRequestSeparator implements IRequestSeparator {
	/**
	 * Currently, this is used for testing/validation only
	 */
	public AtomicInteger eventCounter = new AtomicInteger();
	IncludeThisEventFilterExt m_requestCompletionFilter = null;
	ConcurrentHashMap<String, IRequestEvents> m_inFlightRequests = new ConcurrentHashMap<String,IRequestEvents>();
	ICompletedRequestCallback m_completedRequestCallback = null;
	
	public DefaultRequestSeparator() {
		m_requestCompletionFilter = new IncludeThisEventFilterExt(); 
	}
	@Override
	public void setRequestCompletionEvent(ITraceEvent requestCompletionEvent) {
		m_requestCompletionFilter.setFilterCriteria(requestCompletionEvent);
	}

	@Override
	public void setCompletedRequestCallback(
			ICompletedRequestCallback requestCallback) {
		m_completedRequestCallback = requestCallback;		
	}

	@Override
	public ConcurrentHashMap<String, IRequestEvents> getInFlightRequests() {
		return m_inFlightRequests;
	}
	@Override
	public int size() {
		return getInFlightRequests().size();
	}
	@Override
	public void add(ITraceEvent event) {
			eventCounter.incrementAndGet();
			IRequestEvents emptyRequest = DefaultFactory.getFactory().getRequestEvents();
			emptyRequest.setThreadId(event.getThreadId());
			//If the request isn't there, add it.  If it is there, retrieve it.
			IRequestEvents storedRequest = (IRequestEvents) getInFlightRequests().putIfAbsent(event.getThreadId(), emptyRequest);
			if (storedRequest==null) { //This will happen for the first traced event of each request.
				storedRequest = emptyRequest;
			}
			
			if (m_requestCompletionFilter.matches(event)) {
				//Now that the completion event has fired, we're removing it from the "in flight" list.
				//Also note that the completion event does not get propogated.
				IRequestEvents completed = getInFlightRequests().remove(event.getThreadId());
				fireRequestCompletion(completed);
			} else {
				//Correct behavior:  event.getThreadId()=X is added to storedRequest.getThreadId()=X
				//Without the surrounding sync block, the above does not hold true...lots of mismatched parents/children,
				//as shown by the following line:
				//System.out.println("About to add event [" + event.getAgentTimeMillisString() + "] thread [" + event.getThreadId() + "] to parent [" + storedRequest.getThreadId() + "]");
				storedRequest.getRequestEvents().add(event);
			}
			
	}
	private void fireRequestCompletion(IRequestEvents request) {
		//IRequestEvents events = (IRequestEvents) getInFlightRequests().get(threadId);
		m_completedRequestCallback.requestCompleted(request);
		//getInFlightRequests().remove(threadId);
	}

}
