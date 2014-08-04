package org.headlessintrace.client.filter;

public interface ITraceFilter {
	boolean matches(String traceData);
}
