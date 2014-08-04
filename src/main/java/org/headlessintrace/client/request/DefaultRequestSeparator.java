package org.headlessintrace.client.request;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.headlessintrace.client.DefaultFactory;
import org.headlessintrace.client.filter.ITraceFilterExt;
import org.headlessintrace.client.filter.ITraceFilterExt2;
import org.headlessintrace.client.model.ITraceEvent;
import org.headlessintrace.client.request.IRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultRequestSeparator implements IRequestSeparator {
	static Logger LOG = LoggerFactory.getLogger(DefaultRequestSeparator.class);
	
	/**
	 * Currently, this is used for testing/validation only
	 */
	public AtomicInteger eventCounter = new AtomicInteger();
	ITraceFilterExt m_requestCompletionFilter = null;
	ITraceFilterExt m_requestStartFilter = null;
	ConcurrentHashMap<String, IRequest> m_inFlightRequests = new ConcurrentHashMap<String,IRequest>();
	ICompletedRequestCallback m_completedRequestCallback = null;
	
	public DefaultRequestSeparator(ITraceFilterExt2 start, ITraceFilterExt2 completion) {
		m_requestStartFilter = start;
		m_requestCompletionFilter = completion;
	}

	public DefaultRequestSeparator() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setCompletedRequestCallback(
			ICompletedRequestCallback requestCallback) {
		m_completedRequestCallback = requestCallback;		
	}

	@Override
	public ConcurrentHashMap<String, IRequest> getInFlightRequests() {
		return m_inFlightRequests;
	}
	@Override
	public int size() {
		return getInFlightRequests().size();
	}
	/**
	 * All Events from all SUT threads are added here.
	 * This method sorts the events, putting all the events from one thread into a single IRequest object,
	 * which is stored in the getInFlightRequests() method.
	 * When this method determines the request is finished, then it 
	 * 	1) removes it from the list of inFlight requests.
	 *  2) invokes this#fireFrequestCompletion(IRequest request);
	 */
	@Override
	public void add(ITraceEvent event) {

		if (this.m_requestStartFilter == null)
			throw new RuntimeException("Must indicate which event marks the 'start' of each 'request'.  Use DefaultRequestSeparator.setRequestStartFilter(val).");
		if (this.m_requestCompletionFilter == null)
			throw new RuntimeException("Must indicate which event marks the 'end' of each 'request'.  Use DefaultRequestSeparator.setRequestCompletionFilter(val).");
		
		eventCounter.incrementAndGet();
		
		IRequest emptyRequest = DefaultFactory.getFactory().getRequest();
		emptyRequest.setThreadId(event.getThreadId());
		//If the request isn't there, add it.  If it is there, retrieve it.
		IRequest storedRequest = (IRequest) getInFlightRequests().putIfAbsent(event.getThreadId(), emptyRequest);
		if (storedRequest==null) { //This will happen for the first traced event of each request.
			//System.out.println("##!# Starting new request.  Event is [" + event.getEventType() + "]");
			storedRequest = emptyRequest;
		}
		boolean	ynStartEvent = this.m_requestStartFilter.matches(event); 
		if (ynStartEvent)
			storedRequest.setInitialized(true);
		
		if (m_requestCompletionFilter.matches(event)) {
			//Now that the completion event has fired, we're removing it from the "in flight" list.
			//Also note that the completion event itself does not get propagated.
			IRequest completed = getInFlightRequests().remove(event.getThreadId());
			if (completed !=null && completed.isInitialized()  )
				fireRequestCompletion(completed);
		} else {
			
			if (storedRequest.isInitialized() && !ynStartEvent)
				storedRequest.getEvents().add(event);
		}
			
	}
	private void fireRequestCompletion(IRequest request) {
		//IRequestEvents events = (IRequestEvents) getInFlightRequests().get(threadId);
		m_completedRequestCallback.requestCompleted(request);
		//getInFlightRequests().remove(threadId);
	}
	@Override
	public void setRequestCompletionFilter(ITraceFilterExt val) {
		this.m_requestCompletionFilter = val;
	}
	@Override
	public void setRequestStartFilter(ITraceFilterExt val) {
		this.m_requestStartFilter = val;
	}

}
