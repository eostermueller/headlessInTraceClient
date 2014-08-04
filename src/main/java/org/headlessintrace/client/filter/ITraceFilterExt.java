package org.headlessintrace.client.filter;

import org.headlessintrace.client.model.ITraceEvent;

public interface ITraceFilterExt {
	boolean matches(ITraceEvent traceData);

}
