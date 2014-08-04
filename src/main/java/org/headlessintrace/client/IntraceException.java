package org.headlessintrace.client;

public class IntraceException extends Throwable {

	Throwable m_trigger = null;
	public IntraceException(String string) {
		super(string);
	}
	public IntraceException() {}

	public Throwable getTrigger() {
		return m_trigger;
	}
	public IntraceException(NumberFormatException nfe, String string) {
		m_trigger = nfe;
	}

}
