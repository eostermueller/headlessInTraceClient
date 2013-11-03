package org.intrace.client.filter;


import org.intrace.client.model.ITraceEvent;

/**
 * An "Include" style filter, as opposed to an "exclude" style filter.
 * @author erikostermueller
 *
 */
public class IncludeThisEventFilterExt implements ITraceFilterExt {
	ITraceEvent m_criteria = null;
	public void setFilterCriteria(ITraceEvent criteria) {
		m_criteria = criteria;
	}
	@Override
	public boolean matches(ITraceEvent traceData) {
		if (m_criteria.getEventType()==traceData.getEventType()
			&& m_criteria.getClassName().equals(traceData.getClassName())
			&& m_criteria.getMethodName().equals(traceData.getMethodName())
			) {
				return true;
			}
		return false;
	}
}
