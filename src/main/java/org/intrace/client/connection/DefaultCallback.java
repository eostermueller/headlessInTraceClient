package org.intrace.client.connection;

import java.util.List;
import java.util.Map;

import org.intrace.client.DefaultFactory;
import org.intrace.client.IFactory;
import org.intrace.client.model.FixedLengthQueue;
import org.intrace.client.IFactory;

public class DefaultCallback implements IConnectionStateCallback {
	
	/**
	 * Keep a history of recent status messages...but it is a limited history.
	 */
	FixedLengthQueue<String> m_connectionStatusMessages = 
			new FixedLengthQueue<String>(
					DefaultFactory.getFactory().getConfig().getFixedMessageCount() );

	/**
	 * Keep a history of recent connections states...but it is a limited history.
	 */
	FixedLengthQueue<ConnectState> m_connectStates = 
			new FixedLengthQueue<ConnectState>(
					DefaultFactory.getFactory().getConfig().getFixedMessageCount() );
	
	public ConnectState getConnectState() {
		return m_connectStates.getLast();
	}
	
	public List<ConnectState> getConnectStates() {
		return m_connectStates;
	}
	@Override
	public void setConnectionStatusMsg(String msg) {
		m_connectionStatusMessages.add(msg);
	}
	public List getConnectionStatusMsgs() {
		return m_connectionStatusMessages;
	}

	@Override
	public void setConnectState(ConnectState state) {
		m_connectStates.add(state);
	}

	/**
	 * Need to learn how to use this.  Is it even useful?
	 */
	@Override
	public void setProgress(Map<String, String> progress) {
		// TODO Auto-generated method stub

	}

	/**
	 * Need to learn how to use this.  Is it even useful?
	 */
	@Override
	public void setStatus(Map<String, String> progress) {
		// TODO Auto-generated method stub

	}
	/**
	 * Need to learn how to use this.  Is it even useful?
	 */
	@Override
	public void setConfig(Map<String, String> progress) {
		// TODO Auto-generated method stub

	}
	
}
