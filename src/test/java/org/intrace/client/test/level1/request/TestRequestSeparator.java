package org.intrace.client.test.level1.request;

import static org.junit.Assert.*;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.intrace.client.DefaultFactory;
import org.intrace.client.IntraceException;
import org.intrace.client.filter.IncludeThisEventFilterExt;
import org.intrace.client.model.ITraceEvent;
import org.intrace.client.request.ICompletedRequestCallback;
import org.intrace.client.request.IRequest;
import org.intrace.client.request.IRequestSeparator;
import org.intrace.client.test.TestUtil;
import org.junit.Before;
import org.junit.Test;

public class TestRequestSeparator {
	/**
	 * Would like to use a closure instead of this, but closures don't exist yet.
	 */
	List<IRequest> m_completedRequests = new CopyOnWriteArrayList<IRequest>();
	private static final String THREAD_COMPLETION_EVENT = "[08:36:43.885]:[412]:java.lang.Thread:run: }:682";

	private ITraceEvent m_requestCompletionEvent = null;

	@Before
	public void setup() throws IntraceException {
		m_requestCompletionEvent = DefaultFactory.getFactory().getEventParser().createEvent(THREAD_COMPLETION_EVENT, 0);
	}
	

	
	@Test
	public void canSeparteMultipleRequests_trivial() throws IntraceException {
		/**
		 * These events represent the same two events getting fired repeatedly, but synchronously, where one thread doesn't start until the previous one ends.
		 */
		final String EVENTS_FROM_MULTIPLE_REQUESTS_PRISTINE = 
				  "[08:36:43.885]:[412]:java.lang.Thread:run: }:682\n"
				+ "[08:36:43.885]:[413]:java.lang.Thread:run: }:682\n"
				+ "[08:36:43.885]:[414]:java.lang.Thread:run: }:682\n";
		List<ITraceEvent> events = TestUtil.createEvents(EVENTS_FROM_MULTIPLE_REQUESTS_PRISTINE);
		IRequestSeparator requestSeparator = DefaultFactory.getFactory().getRequestSeparator();
		requestSeparator.setRequestCompletionEvent(m_requestCompletionEvent);//Whenever this particular event first, that marks the end of a thread/request.
		
		m_completedRequests.clear();
		ICompletedRequestCallback requestCallback = new ICompletedRequestCallback() {
			
			@Override
			public void requestCompleted(IRequest events) {
				m_completedRequests.add(events);
				
			}
		};
		requestSeparator.setCompletedRequestCallback(requestCallback);
		
		//Simulate events from the InTrace server agent
		for(ITraceEvent e : events) {
			requestSeparator.add(e);
		}
		
		assertEquals("Added three requests, each with zero events, but didn't get 3", 3 /* events.size() */, m_completedRequests.size());
		for(IRequest request : m_completedRequests) {
			assertEquals("Expected zero events for each request", 0, request.getEvents().size());
		}
		assertEquals("Whoops.  The RequestSeparator should have evicted all the requests, but some were left",0,requestSeparator.size());
		
	}
	@Test
	public void canSeparateManyEventsIntoEightRequests() throws IntraceException {
		/**
		 * These events represent the same two events getting fired repeatedly, but synchronously, where one thread doesn't start until the previous one ends.
		 */
		final String EVENTS_FROM_MULTIPLE_THREADS_PRISTINE_8 = 
			       "[08:36:43.885]:[412]:org.hsqldb.jdbc.jdbcConnection:prepareStatement: Arg: INSERT INTO Event (name, description, date, location) VALUES(?, ?, ?, ?)\n"
			     + "[08:36:43.885]:[412]:java.lang.Thread:run: }:682\n"
			     + "[08:36:43.885]:[413]:org.hsqldb.jdbc.jdbcConnection:prepareStatement: Arg: INSERT INTO Event (name, description, date, location) VALUES(?, ?, ?, ?)\n"
			     + "[08:36:43.885]:[413]:java.lang.Thread:run: }:682\n"
			     + "[08:36:43.885]:[414]:org.hsqldb.jdbc.jdbcConnection:prepareStatement: Arg: INSERT INTO Event (name, description, date, location) VALUES(?, ?, ?, ?)\n"
			     + "[08:36:43.885]:[414]:java.lang.Thread:run: }:682\n"
			     + "[08:36:43.885]:[415]:org.hsqldb.jdbc.jdbcConnection:prepareStatement: Arg: INSERT INTO Event (name, description, date, location) VALUES(?, ?, ?, ?)\n"
			     + "[08:36:43.885]:[415]:java.lang.Thread:run: }:682\n"
			     + "[08:36:43.885]:[416]:org.hsqldb.jdbc.jdbcConnection:prepareStatement: Arg: INSERT INTO Event (name, description, date, location) VALUES(?, ?, ?, ?)\n"
			     + "[08:36:43.885]:[416]:java.lang.Thread:run: }:682\n"
			     + "[08:36:43.885]:[417]:org.hsqldb.jdbc.jdbcConnection:prepareStatement: Arg: INSERT INTO Event (name, description, date, location) VALUES(?, ?, ?, ?)\n"
			     + "[08:36:43.885]:[417]:java.lang.Thread:run: }:682\n"
			     + "[08:36:43.885]:[418]:org.hsqldb.jdbc.jdbcConnection:prepareStatement: Arg: INSERT INTO Event (name, description, date, location) VALUES(?, ?, ?, ?)\n"
			     + "[08:36:43.885]:[418]:java.lang.Thread:run: }:682\n"
			     + "[08:36:43.885]:[419]:org.hsqldb.jdbc.jdbcConnection:prepareStatement: Arg: INSERT INTO Event (name, description, date, location) VALUES(?, ?, ?, ?)\n"
			     + "[08:36:43.885]:[419]:java.lang.Thread:run: }:682\n";
				
		List<ITraceEvent> events = TestUtil.createEvents(EVENTS_FROM_MULTIPLE_THREADS_PRISTINE_8);
		IRequestSeparator requestSeparator = DefaultFactory.getFactory().getRequestSeparator();
		requestSeparator.setRequestCompletionEvent(m_requestCompletionEvent);//Whenever this particular event first, that marks the end of a thread/request.
		
		m_completedRequests.clear();
		ICompletedRequestCallback requestCallback = new ICompletedRequestCallback() {
			
			@Override
			public void requestCompleted(IRequest events) {
				m_completedRequests.add(events);
			}
		};
		requestSeparator.setCompletedRequestCallback(requestCallback);
		
		//Simulate events from the InTrace server agent
		for(ITraceEvent e : events) {
			requestSeparator.add(e);
		}
		//We're expecting one event per request.  Each event will look like this:
		String rawEventText = "[08:36:43.885]:[419]:org.hsqldb.jdbc.jdbcConnection:prepareStatement: Arg: INSERT INTO Event (name, description, date, location) VALUES(?, ?, ?, ?)\n";
		ITraceEvent testEvent = 
				DefaultFactory.getFactory().getEventParser().createEvent(rawEventText, 0);
		IncludeThisEventFilterExt myTestFilter = new IncludeThisEventFilterExt();
		myTestFilter.setFilterCriteria(testEvent);
		
		assertEquals("Added eight requests, each with one events, but didn't get eight", 8 /* events.size() */, m_completedRequests.size());
		for(IRequest request : m_completedRequests) {
			assertEquals("Expected one event for each request", 1, request.getEvents().size());
			
			assertTrue("Expected a particular event type, class name and package", 
					myTestFilter.matches(request.getEvents().get(0)));
		}
		
		assertEquals("Whoops.  The RequestSeparator should have evicted all the requests, but some were left",0,requestSeparator.size());
	}
	
	/**
	 * Validate that incomplete requests are not forwarded to ICompletedRequestCallback.
	 * Incomplete requests are ones for which the {@link IRequestSeparator#setRequestCompletionEvent(ITraceEvent)} has 
	 * not yet been called.
	 * @throws IntraceException 
	 */
	@Test
	public void canHoldIncompleteRequests() throws IntraceException {
		/**
		 * Request/thread 412, below, is complete.  See the Thread:run?
		 * However, request/thread 413 is incomplete, because there is no line with 413 and Thread:run
		 * This test validates that the events for 413 do not get sent.
		 */
		final String ONE_COMPLETE_AND_ONE_INCOMPLETE = 
			       "[08:36:43.885]:[412]:org.hsqldb.jdbc.jdbcConnection:prepareStatement: Arg: INSERT INTO Event (name, description, date, location) VALUES(?, ?, ?, ?)\n"
			     + "[08:36:43.885]:[412]:java.lang.Thread:run: }:682\n"
			     + "[08:36:43.885]:[413]:org.hsqldb.jdbc.jdbcConnection:prepareStatement: Arg: INSERT INTO Event (name, description, date, location) VALUES(?, ?, ?, ?)\n";
				
		List<ITraceEvent> events = TestUtil.createEvents(ONE_COMPLETE_AND_ONE_INCOMPLETE);
		IRequestSeparator requestSeparator = DefaultFactory.getFactory().getRequestSeparator();
		requestSeparator.setRequestCompletionEvent(m_requestCompletionEvent);//Whenever this particular event first, that marks the end of a thread/request.
		
		m_completedRequests.clear();
		ICompletedRequestCallback requestCallback = new ICompletedRequestCallback() {
			
			@Override
			public void requestCompleted(IRequest events) {
				m_completedRequests.add(events);
			}
		};
		requestSeparator.setCompletedRequestCallback(requestCallback);
		
		//Simulate events from the InTrace server agent
		for(ITraceEvent e : events) {
			requestSeparator.add(e);
		}
		//We're expecting one event per request.  Each event will look like this:
		String rawEventText = "[08:36:43.885]:[419]:org.hsqldb.jdbc.jdbcConnection:prepareStatement: Arg: INSERT INTO Event (name, description, date, location) VALUES(?, ?, ?, ?)\n";
		ITraceEvent testEvent = 
				DefaultFactory.getFactory().getEventParser().createEvent(rawEventText, 0);
		IncludeThisEventFilterExt myTestFilter = new IncludeThisEventFilterExt();
		myTestFilter.setFilterCriteria(testEvent);
		
		assertEquals("Added a single complete request along with an event for an incomplete request.  Expecting just a single completed request", 1 , m_completedRequests.size());
		IRequest request = m_completedRequests.get(0);
		assertEquals("Expected one event for the single completed request", 1, request.getEvents().size());
			
		assertTrue("Expected a particular event type, class name and package", 
			myTestFilter.matches(request.getEvents().get(0)));
		assertEquals("Expected one event from a particular thread, but did not find that thread", "412", request.getEvents().get(0).getThreadId());
		assertEquals("Whoops. The RequestSeparator should not evicted one request, because it hadn't completed yet",1,requestSeparator.size());
	}
	@Test
	public void canSeparateInterleavedEventsIntoEightRequests() throws IntraceException {
		/**
		 * Processing/events never happen in a tidy fashion, like this, but logically this situation must be handled.
		 * 8 threads trigger two events each.
		 * All 8 threads first process their first event (and in sequential order of thread id)
		 * Once those 8 events are fired, all 8 threads fire their second event.
		 */
		final String EVENTS_FROM_MULTIPLE_THREADS_INTERLEAVED_8 = 
			       "[08:36:43.885]:[412]:org.hsqldb.jdbc.jdbcConnection:prepareStatement: Arg: INSERT INTO Event (name, description, date, location) VALUES(?, ?, ?, ?)\n"
			     + "[08:36:43.885]:[413]:org.hsqldb.jdbc.jdbcConnection:prepareStatement: Arg: INSERT INTO Event (name, description, date, location) VALUES(?, ?, ?, ?)\n"
			     + "[08:36:43.885]:[414]:org.hsqldb.jdbc.jdbcConnection:prepareStatement: Arg: INSERT INTO Event (name, description, date, location) VALUES(?, ?, ?, ?)\n"
			     + "[08:36:43.885]:[415]:org.hsqldb.jdbc.jdbcConnection:prepareStatement: Arg: INSERT INTO Event (name, description, date, location) VALUES(?, ?, ?, ?)\n"
			     + "[08:36:43.885]:[416]:org.hsqldb.jdbc.jdbcConnection:prepareStatement: Arg: INSERT INTO Event (name, description, date, location) VALUES(?, ?, ?, ?)\n"
			     + "[08:36:43.885]:[417]:org.hsqldb.jdbc.jdbcConnection:prepareStatement: Arg: INSERT INTO Event (name, description, date, location) VALUES(?, ?, ?, ?)\n"
			     + "[08:36:43.885]:[418]:org.hsqldb.jdbc.jdbcConnection:prepareStatement: Arg: INSERT INTO Event (name, description, date, location) VALUES(?, ?, ?, ?)\n"
			     + "[08:36:43.885]:[419]:org.hsqldb.jdbc.jdbcConnection:prepareStatement: Arg: INSERT INTO Event (name, description, date, location) VALUES(?, ?, ?, ?)\n"
			     + "[08:36:43.885]:[413]:java.lang.Thread:run: }:682\n"
			     + "[08:36:43.885]:[414]:java.lang.Thread:run: }:682\n"
			     + "[08:36:43.885]:[412]:java.lang.Thread:run: }:682\n"
			     + "[08:36:43.885]:[415]:java.lang.Thread:run: }:682\n"
			     + "[08:36:43.885]:[417]:java.lang.Thread:run: }:682\n"
			     + "[08:36:43.885]:[418]:java.lang.Thread:run: }:682\n"
			     + "[08:36:43.885]:[416]:java.lang.Thread:run: }:682\n"
			     + "[08:36:43.885]:[419]:java.lang.Thread:run: }:682\n";
				
		List<ITraceEvent> events = TestUtil.createEvents(EVENTS_FROM_MULTIPLE_THREADS_INTERLEAVED_8);
		IRequestSeparator requestSeparator = DefaultFactory.getFactory().getRequestSeparator();
		requestSeparator.setRequestCompletionEvent(m_requestCompletionEvent);//Whenever this particular event first, that marks the end of a thread/request.
		
		m_completedRequests.clear();
		ICompletedRequestCallback requestCallback = new ICompletedRequestCallback() {
			
			@Override
			public void requestCompleted(IRequest events) {
				m_completedRequests.add(events);
			}
		};
		requestSeparator.setCompletedRequestCallback(requestCallback);
		
		//Simulate events from the InTrace server agent
		for(ITraceEvent e : events) {
			requestSeparator.add(e);
		}
		//We're expecting one event per request.  Each event will look like this:
		String rawEventText = "[08:36:43.885]:[419]:org.hsqldb.jdbc.jdbcConnection:prepareStatement: Arg: INSERT INTO Event (name, description, date, location) VALUES(?, ?, ?, ?)\n";
		ITraceEvent testEvent = 
				DefaultFactory.getFactory().getEventParser().createEvent(rawEventText, 0);
		IncludeThisEventFilterExt myTestFilter = new IncludeThisEventFilterExt();
		myTestFilter.setFilterCriteria(testEvent);
		
		assertEquals("Added eight requests, each with one events, but didn't get eight", 8 /* events.size() */, m_completedRequests.size());
		for(IRequest request : m_completedRequests) {
			assertEquals("Expected one event for each request", 1, request.getEvents().size());
			
			assertTrue("Expected a particular event type, class name and package", 
					myTestFilter.matches(request.getEvents().get(0)));
		}
		
		assertEquals("Whoops.  The RequestSeparator should have evicted all the requests, but some were left",0,requestSeparator.size());
	}

}
