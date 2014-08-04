package org.intrace.client.filter;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.intrace.client.model.ITraceEvent;
import org.intrace.client.model.ITraceEvent.EventType;
import org.intrace.client.request.ICompletedRequestCallback;
import org.intrace.client.request.IRequest;

public class ContiguousEventFilter implements ICompletedRequestCallback {

	private static final int UNINIT = -1;
	private static final int INDEX_ERROR = -2;
	private ICompletedRequestCallback m_downStreamCallback;
	private Map<String,String> m_keepMethods  = new Hashtable<String,String>();
	public void keepMethod(String val) {
		m_keepMethods.put(val, val);
	}
	private boolean isMethodToKeep(String val) {
		return m_keepMethods.containsKey(val);
	}
	
	public ContiguousEventFilter(ICompletedRequestCallback testCallback) {
		m_downStreamCallback = testCallback;
	}
	public ICompletedRequestCallback getDownStreamCallback() {
		return m_downStreamCallback;
	}
	public void setDownStreamCallback(ICompletedRequestCallback val) {
		this.m_downStreamCallback = val;
	}
	@Override
	public void requestCompleted(IRequest events)  {
			removeNonContiguousEvents( events.getEvents() );
			getDownStreamCallback().requestCompleted(events);
	}
	
	/**
	 * Given 'currentIndex' below, this method prunes out all the a and x events, 
	 * which are events from jdbc 'wrapped' methods.
	 * Given this set of events (1=entry, 2=exit, others may be inbetween 1 & 2):
	 * <pre>
	 * a1
	 * b1
	 * b2
	 * a2
	 * x1
	 * y1
	 * y2
	 * x2
	 * </pre>
	 * Eliminate the 'wrapper' events (the a's and the x's).
	 * Leaving this:
	 * <pre>
	 * b1
	 * b2
	 * y1
	 * y2
	 * </pre>
	 * 
	 * approach:
	 * step 1:  walking forward from 0, locate first exit (b2).
	 * step 2: identify the preceding events that belong to the b2 EXIT, by walking backward to the previous ENTRY.
	 * step 3: DELETE everything up to but not including the first b event.
	 * repeat, with the NEXT_ITERATION_START being immediately after the b events.
	 * <pre>
	 * a1 - DELETE
	 * b1 - ENTRY
	 * b2 - EXIT
	 * a2 - NEXT_ITERATION_START
	 * x1
	 * y1
	 * y2
	 * x2
	 * </pre>
	 * @param events
	 * @throws WuqispankException 
	 */
	private void removeNonContiguousEvents(List<ITraceEvent> events) {
		int firstEntry = INDEX_ERROR;
		int firstExit = INDEX_ERROR;
		int endOfWindow = INDEX_ERROR;
		int current = 0;
		
		int countDeletesBefore = UNINIT;
		int countDeletesAfter = UNINIT;
		int countOfEventsToKeep = UNINIT;
		try {
			while (current < events.size()) {
				firstExit = getIndexOfNext(EventType.EXIT,events,current);
				if (firstExit!=INDEX_ERROR) {
					firstEntry = getIndexOfPrevious(EventType.ENTRY,events,firstExit);
					if (firstEntry!=INDEX_ERROR) {
						
						//Delete 'wrapped' events _before_ the unwrapped method invocation
						countDeletesBefore = firstEntry-current;
						for(int j = 0; j < countDeletesBefore; j++)
							events.remove(current);
						
						//Reset index to account for above deletion.
						firstExit -= countDeletesBefore;

						endOfWindow = getIndexOfNext(EventType.ENTRY,events,firstExit);
						if (endOfWindow==INDEX_ERROR)
							countDeletesAfter = events.size() - (firstExit+1);
						else
							countDeletesAfter = (endOfWindow-firstExit)-1;
						
						/**
						 * Delete 'wrapped' events _after_ the unwrapped method invocation
						 */
						for(int j = 0; j < countDeletesAfter; j++)
							events.remove(firstExit+1);
						
						current = firstExit + 1;
					}
				}
			}
		} catch(Exception ex) {
			ex.printStackTrace();
			StringBuilder sb = new StringBuilder();
			sb.append("\n"  + ex.getMessage() + "\n");
			sb.append("firstEntry [" + firstEntry + "]\n");
			sb.append("firstExit [" + firstExit + "]\n");
			sb.append("endOfWindow [" + endOfWindow + "]\n");
			sb.append("current [" + current + "]\n");
			sb.append("countDeletesBefore [" + countDeletesBefore + "]\n");
			sb.append("countDeletesAfter [" + countDeletesAfter + "]\n");
			sb.append("countOfEventsToKeep [" + countOfEventsToKeep + "]\n");
			throw new RuntimeException("InTrace Event Order error.  [" + sb.toString() + "]");
		}
		
		
	}
	private int getIndexOfNext(EventType eventType, List<ITraceEvent> events, int current) {
		int rc = INDEX_ERROR;
		for(int i = current; i < events.size();i++) {
			if (events.get(i).getEventType()==eventType && isMethodToKeep(events.get(i).getMethodName()))  {
				rc = i;
				break;
			}
		}
		return rc;
	}
	private int getIndexOfPrevious(EventType eventType, List<ITraceEvent> events, int current) {
		int rc = INDEX_ERROR;
		for(int i = current-1; i >=0;i--) {
			if (events.get(i).getEventType()==eventType && isMethodToKeep(events.get(i).getMethodName()))  {
				rc = i;
				break;
			}
		}
		return rc;
	}

}
