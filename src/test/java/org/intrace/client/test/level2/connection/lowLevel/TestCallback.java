package org.intrace.client.test.level2.connection.lowLevel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.intrace.client.connection.AbstractNoOpConnectionCallback;
import org.intrace.client.connection.ConnectState;
import org.intrace.client.connection.DefaultCallback;

/**
 * This class is designed for just this test.
 * It collects a list of all status messages. CONNECTED, DISCONNECTED, etc...
 * I could imagine keeping a timestamped non-test list of connections for history, to show the end user what has been happening recently.
 * @author e0018740
 *
 */
public class TestCallback extends DefaultCallback {
	private List<ConnectState> m_connectStates = new CopyOnWriteArrayList<ConnectState>();
	private List<String> m_listMessages = new ArrayList<String>();
	public List<String> getMessages() {
		return m_listMessages;
	}
	@Override
	public void setConnectionStatusMsg(String msg) {
		getMessages().add(msg);
		
	}
	
	@Override
	public void setConnectState(ConnectState state) {
		getConnectStates().add(state);
	}
	public List<ConnectState> getConnectStates() {
		return m_connectStates;
	}
	@Override
	public ConnectState getConnectState() {
		return m_connectStates.get(m_connectStates.size()-1);
	}
}

