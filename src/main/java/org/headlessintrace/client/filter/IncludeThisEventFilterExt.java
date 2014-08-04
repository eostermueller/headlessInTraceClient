package org.headlessintrace.client.filter;


import org.headlessintrace.client.model.ITraceEvent;

/**
 * An "Include" style filter, as opposed to an "exclude" style filter.
 * @author erikostermueller
 *
 */
public class IncludeThisEventFilterExt implements ITraceFilterExt {
	ITraceEvent m_criteria = null;
	public IncludeThisEventFilterExt() {
		
	}
	public IncludeThisEventFilterExt(ITraceEvent val) {
		setFilterCriteria(val);
	}
	//@Override
	public void setFilterCriteria(ITraceEvent criteria) {
		m_criteria = criteria;
	}
	@Override
	public boolean matches(ITraceEvent traceData) {
		return matches(m_criteria,traceData);
	}
	protected boolean matches(ITraceEvent a, ITraceEvent b) {
		if (a==null || b==null)
			return false;
		
		if (a.getEventType()==b.getEventType()
				&& a.getClassName().equals(b.getClassName())
				&& a.getMethodName().equals(b.getMethodName())
				) {
					return true;
				}
			return false;
		
	}
}
