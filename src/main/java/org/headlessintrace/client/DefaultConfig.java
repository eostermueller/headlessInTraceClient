package org.headlessintrace.client;

public class DefaultConfig implements Config {

	@Override
	public int getFixedMessageCount() {
		return 2000;
	}
	
	/**
	 * If the network doesn't respond in 5 seconds, there is a problem.
	 * Possible problems:
	 * 	The intrace agent isn't listening on the specified port
	 * The specified port is blocked by a firewall
	 * The host name is wrong
	 * The port number is wrong.
	 */
	@Override
	public long getConnectWaitMs() {
		return 60*1000;//60 seconds...enough to debug past the connection timeout
	}

}
