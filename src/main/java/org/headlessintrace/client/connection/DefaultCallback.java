package org.headlessintrace.client.connection;

import java.util.List;
import java.util.Map;

import org.headlessintrace.client.DefaultFactory;
import org.headlessintrace.client.IFactory;
import org.headlessintrace.client.model.FixedLengthQueue;
import org.headlessintrace.client.IFactory;

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
	 * org/intrace/agent/ClassTransformer.java (part of agent code) drives the traffic to this method.
	 * If you place this line of code in the method:
	 * <pre>
		System.out.println("DefaultCallback.setProgress() [" + progress.toString() + "]");
		<pre>
		...then you'll get output that looks like this:
		<pre>
	   	DefaultCallback.setProgress() [{NUM_PROGRESS_COUNT=2700, NUM_PROGRESS_ID=NUM_PROGRESS_ID, NUM_PROGRESS_TOTAL=2740}]
	  	DefaultCallback.setProgress() [{NUM_PROGRESS_COUNT=2710, NUM_PROGRESS_ID=NUM_PROGRESS_ID, NUM_PROGRESS_TOTAL=2740}]
	  	DefaultCallback.setProgress() [{NUM_PROGRESS_COUNT=2720, NUM_PROGRESS_ID=NUM_PROGRESS_ID, NUM_PROGRESS_TOTAL=2740}]
	  	DefaultCallback.setProgress() [{NUM_PROGRESS_COUNT=2730, NUM_PROGRESS_ID=NUM_PROGRESS_ID, NUM_PROGRESS_TOTAL=2740}]
	  	DefaultCallback.setProgress() [{NUM_PROGRESS_DONE=true, NUM_PROGRESS_ID=NUM_PROGRESS_ID, NUM_PROGRESS_TOTAL=2740, NUM_PROGRESS_COUNT=2740}]
	   </pre>
	   
	 */
	@Override
	public void setProgress(Map<String, String> progress) {
		
	}

	/**
	 * Need to learn how to use this.  Is it even useful?
	 */
	@Override
	public void setStatus(Map<String, String> progress) {
		

	}
	/**
	 * Need to learn how to use this.  Is it even useful?
	 */
	@Override
	public void setConfig(Map<String, String> progress) {
		

	}
	
}
