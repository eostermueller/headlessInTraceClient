package org.intrace.client.filter;

public interface ITraceFilter {
	boolean matches(String traceData);
}
