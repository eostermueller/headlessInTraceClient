package org.headlessintrace.client.connection;

import java.util.Map;
/**
 * Designed to be used for testing.
 * provides "no-op" implementations of all methods in ConnectionPartTwo.IConnectionStateCallback
 * @author e0018740
 *
 */
public abstract class AbstractNoOpConnectionCallback implements IConnectionStateCallback {

	@Override
	public void setConnectionStatusMsg(String msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setConnectState(ConnectState state) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setProgress(Map<String, String> progress) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setStatus(Map<String, String> progress) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setConfig(Map<String, String> progress) {
		// TODO Auto-generated method stub
		
	}

}
