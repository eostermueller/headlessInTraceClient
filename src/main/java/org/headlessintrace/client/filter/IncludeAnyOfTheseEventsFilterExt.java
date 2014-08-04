package org.headlessintrace.client.filter;

import java.util.List;

import org.headlessintrace.client.model.ITraceEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IncludeAnyOfTheseEventsFilterExt implements ITraceFilterExt {
    private static final Logger LOG = LoggerFactory.getLogger( IncludeAnyOfTheseEventsFilterExt.class.getName() );
	private static final Object CLASS_DELIMITER = "|";
	List<ITraceEvent> m_criteriaList = null;
	public void setFilterCriteria(List<ITraceEvent> criteria) {
		m_criteriaList = criteria;
	}
	/**
	 * The InTrace UI supports a nice format for specifying multiple classes
	 * in a single string of text -- convenient for copy-n-paste.
	 * If you want com.ibm.MyClass and com.hp.YourClass, then delimit the multiple classes with a bar (|), like this:
	 * com.ibm.MyClass|com.hp.YourClass
	 * in the "classes" instrumentation dialog (http://mchr3k.github.io/org.intrace/ui.html).
	 * This syntax also works when requesting classes to be instrumented via code and the server agent.
	 * @return
	 */
	public String getDelimitedListOfAllClasses() {
		int count=0;
		StringBuilder sb = new StringBuilder();
		for(ITraceEvent event : m_criteriaList) {
			if (count++>0) sb.append(CLASS_DELIMITER);
			sb.append(event.getPackageAndClass());
		}
		return sb.toString();
	}
	/**
	 * If _any_ of the given criteria class/method pairs match the given event, allow the event to fire.
	 */
	@Override
	public boolean matches(ITraceEvent traceData) {
		boolean ynMatches = false;
		for( ITraceEvent criteria : m_criteriaList) {
			if (LOG.isDebugEnabled())  LOG.debug("Comparing [" + traceData.getRawEventData() + "] to [" + m_criteriaList.size() + "] criteria [" + criteria.toString() +  "]");

//			if (criteria.getEventType()==traceData.getEventType()
////					&& criteria.getClassName().equals(traceData.getClassName())
//					&& criteria.getMethodName().equals(traceData.getMethodName())
//					) 
			
			if (criteria.getMethodName().equals(traceData.getMethodName() )) {
						if (LOG.isDebugEnabled())  LOG.debug("Found match for [" + traceData.getRawEventData() + "] to [" + m_criteriaList.size() + "] criteria ");
						return true;
					} 
		}
		return ynMatches;
	}
}
