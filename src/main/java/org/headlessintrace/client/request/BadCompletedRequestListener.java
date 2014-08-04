package org.headlessintrace.client.request;

import org.headlessintrace.client.model.ITraceEvent;

public class BadCompletedRequestListener extends Exception {

	ITraceEvent m_event;
	public BadCompletedRequestListener(ITraceEvent event) {
		m_event = event;
	}
	@Override
	public String getMessage() {
		return "Before calling RequestConnection.connect(), you call RequestConnection(ctor) or RequestConnection.setCompletedRequestMethod() with class/method name found in InTrace Agent JVM." +
	"Instead, received [" + m_event.getClassName() + "] and [" + m_event.getMethodName() + "] for the class and method name.  The class name must use dot notation instead of class notation.";
	}
}
