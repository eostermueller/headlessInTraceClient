package org.intrace.client.connection;

public class DisconnectionException extends RuntimeException {

	private String m_message;

	public DisconnectionException(String disconnectionMessage) {
		m_message = disconnectionMessage;
	}
	@Override public String getMessage() {
		return m_message;
	}

}
