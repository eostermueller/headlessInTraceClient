package org.intrace.client.connection.command;

import org.intrace.client.gui.helper.ControlConnectionThread;
/**
 * Multiple implementors of this interface can be batched up to send
 * multiple unrelated messages in a single round trip.
 * Example:
 *  <PRE>
 *  getControlConnectionThread().sendMessage( one.getMessage()+two.getMessage()+three.getMessage() );
 *  </PRE>
 *  ...where "one", "two" and "three" are different instances/implementations of IAgentControlProxy 
 * @author e0018740
 *
 */
public interface IAgentCommand {
	void setControlConnectionThread(ControlConnectionThread cct);
	ControlConnectionThread getControlConnectionThread();
	void send();
	public abstract String getMessage();	
}
