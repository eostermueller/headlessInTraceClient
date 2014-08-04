package org.headlessintrace.client;

public interface Config {
	
	/**
	 * Storing too many messages might cause a memory problem.
	 * Storing too few messages might not be enough information from recent history to understand connectivity situation.
	 * @return
	 */
	int getFixedMessageCount();
	
	long getConnectWaitMs();
}
