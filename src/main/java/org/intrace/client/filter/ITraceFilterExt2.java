package org.intrace.client.filter;

import org.intrace.client.model.ITraceEvent;

public interface ITraceFilterExt2 extends ITraceFilterExt {
	boolean matchesPrevious(ITraceEvent traceData);

}
