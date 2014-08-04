package org.headlessintrace.rcp.event;

import java.util.EventObject;

import org.headlessintrace.client.connection.ConnectionDetail;

public class DisconnectionEvent extends EventObject {

	public DisconnectionEvent(ConnectionDetail source) {
		super(source);
		// TODO Auto-generated constructor stub
	}
	public ConnectionDetail getConnection() {
		return (ConnectionDetail)getSource();
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
