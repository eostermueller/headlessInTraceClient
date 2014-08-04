package org.intrace.client.filter;

import java.util.List;

import org.intrace.client.model.ITraceEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IncludeAnyOfTheseMethodsFilterExt extends IncludeAnyOfTheseEventsFilterExt {
private static final Logger LOG = LoggerFactory.getLogger( IncludeAnyOfTheseMethodsFilterExt.class.getName() );

	/**
	 * just checks for event type and method name, _not_ class or package name.
	 */
	@Override
	public boolean matches(ITraceEvent traceData) {
		boolean ynMatches = false;
		for( ITraceEvent criteria : m_criteriaList) {
			if (LOG.isDebugEnabled())  LOG.debug("Comparing [" + traceData.getRawEventData() + "] to [" + m_criteriaList.size() + "] criteria [" + criteria.toString() +  "]");

			if (criteria.getEventType()==traceData.getEventType()
					&& criteria.getMethodName().equals(traceData.getMethodName())
					) {
						if (LOG.isDebugEnabled())  LOG.debug("Found match for [" + traceData.getRawEventData() + "] to [" + m_criteriaList.size() + "] criteria ");
						return true;
					} 
		}
		return ynMatches;
	}
}
