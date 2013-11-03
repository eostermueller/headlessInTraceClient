package org.intrace.client.test.level1.request;

import static org.junit.Assert.*;

import java.io.PrintStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.intrace.client.DefaultFactory;
import org.intrace.client.filter.IncludeThisEventFilterExt;
import org.intrace.client.model.ITraceEvent;
import org.intrace.client.request.ICompletedRequestCallback;
import org.intrace.client.request.IRequestEvents;
import org.intrace.client.request.IRequestSeparator;
import org.intrace.client.test.TestUtil;
import org.junit.Before;
import org.junit.Test;

public class TestMultiThreadedRequestSeparator {
	public static AtomicInteger atomicRequestCount = new AtomicInteger();
	public static AtomicInteger atomicEventCount = new AtomicInteger();
	/**
	 * Would like to use a closure instead of this, but closures don't exist yet.
	 */
	List<IRequestEvents> m_completedRequests = new CopyOnWriteArrayList<IRequestEvents>();
	private static final String THREAD_COMPLETION_EVENT = "[08:36:43.885]:[412]:java.lang.Thread:run: }:682";

	private ITraceEvent m_requestCompletionEvent = null;

	@Before
	public void setup() {
		m_requestCompletionEvent = DefaultFactory.getFactory().getEventParser().createEvent(THREAD_COMPLETION_EVENT, 0);
		atomicRequestCount.set(0);
	}
	

	
	@Test
	public void canSeparteMultipleRequests_almostTrivial() {
		/**
		 * These events represent the same two events getting fired repeatedly, but synchronously, where one thread doesn't start until the previous one ends.
		 */
		//List<ITraceEvent> events = TestUtil.createEvents(EVENTS_FROM_MULTIPLE_REQUESTS_PRISTINE);
		IRequestSeparator requestSeparator = DefaultFactory.getFactory().getRequestSeparator();
		requestSeparator.setRequestCompletionEvent(m_requestCompletionEvent);//Whenever this particular event first, that marks the end of a thread/request.
		
		m_completedRequests.clear();
		ICompletedRequestCallback requestCallback = new ICompletedRequestCallback() {
			
			@Override
			public void requestCompleted(IRequestEvents events) {
				m_completedRequests.add(events);
				
			}
		};
		requestSeparator.setCompletedRequestCallback(requestCallback);
		
		int numThreads = 25;
		int iterations = 10;		
		ExecutorService execService = Executors.newFixedThreadPool(numThreads);
		for (int i = 0; i < numThreads; i++) {
			execService.execute( new Thread(new EventGenerator(requestSeparator,iterations)));
		}
		execService.shutdown();
		try {
			execService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException ie) {
			throw new RuntimeException("interrupted!!!");
		}

		assertEquals("Not even the sanity check worked", numThreads*iterations*EventGenerator.getEventCount(), atomicRequestCount.get());
		assertEquals("Added a fixed number of java.lang.Thread.run EXIT events using multiple threads.", numThreads*iterations*EventGenerator.getEventCount(), m_completedRequests.size());
//		for(IRequestEvents request : m_completedRequests) {
//			assertEquals("Expected zero events for each request", 0, request.getRequestEvents().size());
//		}
		assertEquals("Whoops.  The RequestSeparator should have evicted all the requests, but some were left",0,requestSeparator.size());
		
	}
	@Test
	public void canSeparteMultipleMultiThreadedRequests_threeEach() {
		IRequestSeparator requestSeparator = DefaultFactory.getFactory().getRequestSeparator();
		requestSeparator.setRequestCompletionEvent(m_requestCompletionEvent);//Whenever this particular event first, that marks the end of a thread/request.
		
		m_completedRequests.clear();
		ICompletedRequestCallback requestCallback = new ICompletedRequestCallback() {
			@Override
			public void requestCompleted(IRequestEvents events) {
				m_completedRequests.add(events);
			}
		};
		requestSeparator.setCompletedRequestCallback(requestCallback);
		
		//Try some larger and arbitrary numbers
		int numThreads = 51;
		int iterations = 1013;		
	     CountDownLatch startSignal = new CountDownLatch(1);
	     CountDownLatch doneSignal = new CountDownLatch(numThreads);
		for (int i = 0; i < numThreads; i++) {
		       new Thread(new EventAndThreadIdGenerator(startSignal, doneSignal, requestSeparator,iterations, System.out)).start();
		}
	     startSignal.countDown();      // let all threads proceed
	     try {
	    	 //System.out.println("Waiting to finish");
			doneSignal.await();
			//System.out.println("Finished.");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}           // wait for all to finish

		assertEquals("Whoops, the test infrastructure didn't produce the right # of requests", numThreads*iterations, atomicRequestCount.get());
		assertEquals("Whoops, the test infrastructure didn't produce the right number of events.", numThreads*iterations*EventAndThreadIdGenerator.getEventCount(), atomicEventCount.get());

		assertEquals("Added a fixed number of java.lang.Thread.run EXIT events using multiple threads.", numThreads*iterations, m_completedRequests.size());
		assertEquals("Whoops.  The RequestSeparator should have evicted all the requests, but some were left",0,requestSeparator.size());
		//System.out.println("collected events [" + atomicEventCount.get() + "] ");
		ConcurrentHashMap<String, IRequestEvents> mapInFlight = requestSeparator.getInFlightRequests();
		for(IRequestEvents request : m_completedRequests) {
			if (request==null) {
				fail("Shouldn't have any nulls here.");
			} else {
				//System.out.println("#%#% Found non-null item in hash map");
				List<ITraceEvent> endEvents = request.getRequestEvents();
//				for(ITraceEvent x : endEvents) {
//					System.out.println("parent thread [" + request.getThreadId() + "] simulated thread id [" + x.getThreadId() + "] Event [" + x.getValue() + "]");
//				}
				assertEquals("Expected 2 events for each request", 3, endEvents.size());
			}
		}
		
		assertEquals("All in-flight requests should have been complted",0, mapInFlight.size());
		//System.out.println("InFlight stuff###################################");
		Enumeration<IRequestEvents> inFlightEnum =  mapInFlight.elements();
		while(inFlightEnum.hasMoreElements()) {
			IRequestEvents req = inFlightEnum.nextElement();
			for(ITraceEvent e : req.getRequestEvents()) {
				System.out.println("Parent [" + req.getThreadId() + "] child [" + e.getThreadId() + "][" + e.getAgentTimeMillisString() + "]");
			}
		}
	}
	

}
class EventGenerator implements Runnable {
	
	public static int getEventCount() {
		return 3;//number of events in below string
	}
	/**
	 * The number of events in this string must e
	 */
	static final String EVENTS_FROM_MULTIPLE_REQUESTS_PRISTINE = 
			  "[08:36:43.885]:[412]:java.lang.Thread:run: }:682\n"
			+ "[08:36:43.885]:[413]:java.lang.Thread:run: }:682\n"
			+ "[08:36:43.885]:[414]:java.lang.Thread:run: }:682\n";
	private int m_iterations = -1;
	private IRequestSeparator m_requestSeparator = null;
	public EventGenerator(IRequestSeparator irs, int iterations) {
		m_iterations = iterations;
		m_requestSeparator = irs;
	}
    public void run() {
		
		for(int i = 0; i < m_iterations; i++) {
			//Simulate events from the InTrace server agent
			List<ITraceEvent> events = TestUtil.createEvents(EventGenerator.EVENTS_FROM_MULTIPLE_REQUESTS_PRISTINE);
			for(ITraceEvent e : events) {
				m_requestSeparator.add(e);
				TestMultiThreadedRequestSeparator.atomicRequestCount.incrementAndGet();
			}
		}
   	
    }

}

class EventAndThreadIdGenerator implements Runnable {
	public static int getEventCount() {
		return 4;//number of lines in the below string with prepareStatement
	}
	/**
	 * These events represent the same two events getting fired repeatedly, but synchronously, where one thread doesn't start until the previous one ends.
	 */
	static final String EVENTS_FROM_MULTIPLE_REQUESTS_PRISTINE = 
		      "[08:36:43.881]:[89]:org.hsqldb.jdbc.jdbcConnection:prepareStatement: Arg: INSERT INTO Event (name, description, date, location) VALUES(?, ?, ?, ?)\n"
		    + "[08:36:43.882]:[90]:org.hsqldb.jdbc.jdbcConnection:prepareStatement: Arg: INSERT INTO Event (name, description, date, location) VALUES(?, ?, ?, ?)\n"
		    + "[08:36:43.883]:[90]:org.hsqldb.jdbc.jdbcConnection:prepareStatement: Arg: INSERT INTO Event (name, description, date, location) VALUES(?, ?, ?, ?)\n"
			+ "[08:36:43.884]:[91]:java.lang.Thread:run: }:682\n";
	List<ITraceEvent> events = TestUtil.createEvents(EVENTS_FROM_MULTIPLE_REQUESTS_PRISTINE);
   private final CountDownLatch m_startSignal;
   private final CountDownLatch m_doneSignal;
	private int m_iterations = -1;
	private IRequestSeparator m_requestSeparator = null;
	PrintStream m_out = null;
	public EventAndThreadIdGenerator(CountDownLatch startSignal, CountDownLatch doneSignal, IRequestSeparator irs, int iterations, PrintStream out) {

		m_iterations = iterations;
		m_requestSeparator = irs;
		this.m_startSignal = startSignal;
		this.m_doneSignal = doneSignal;
		this.m_out = out;
	}
    public void run() {
        try {
        	//m_out.println("Waiting to start [" + Thread.currentThread().getName() + "]");
			m_startSignal.await();
        	//m_out.println("starting [" + Thread.currentThread().getName() + "]");
		
			for(int i = 0; i < m_iterations; i++) {
				//Simulate events from the InTrace server agent
				int simulatedRequestId = TestMultiThreadedRequestSeparator.atomicRequestCount.incrementAndGet();
				List<ITraceEvent> events = TestUtil.createEvents(EVENTS_FROM_MULTIPLE_REQUESTS_PRISTINE);
				for(ITraceEvent e : events) {
					//Debug info
					int eventSeq = 	TestMultiThreadedRequestSeparator.atomicEventCount.incrementAndGet();
					e.setThreadId(""+simulatedRequestId);
					String strSimulatedThreadId = Thread.currentThread().getName()+"~"+simulatedRequestId+"!"+eventSeq;
					e.setValue(strSimulatedThreadId);
					e.setSourceLineNumber(eventSeq);
					m_requestSeparator.add(e);
				}
			}
	        m_doneSignal.countDown();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
   	
    }

}
