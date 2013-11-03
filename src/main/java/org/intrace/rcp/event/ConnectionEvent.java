package org.intrace.rcp.event;

import java.util.EventObject;

import org.intrace.client.connection.ConnectionDetail;

public class ConnectionEvent extends EventObject {
	boolean m_successful = false;
	public ConnectionEvent(ConnectionDetail source, boolean success) {
		super(source);
		this.m_successful = success;
		// TODO Auto-generated constructor stub
	}
	public void connectionEstablished() {
		
	}
	public ConnectionDetail getConnection() {
		return (ConnectionDetail)getSource();
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public boolean isSuccessful() {
		return m_successful;
	}
	public void setSuccessful(boolean successful) {
		this.m_successful = successful;
	}

}
