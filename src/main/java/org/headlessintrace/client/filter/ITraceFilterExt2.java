package org.headlessintrace.client.filter;

import org.headlessintrace.client.model.ITraceEvent;

public interface ITraceFilterExt2 extends ITraceFilterExt {
	boolean matchesPrevious(ITraceEvent traceData);

}
