package org.intrace.client.filter;

import org.intrace.client.model.ITraceEvent;

public interface ITraceFilterExt {
	boolean matches(ITraceEvent traceData);

}
