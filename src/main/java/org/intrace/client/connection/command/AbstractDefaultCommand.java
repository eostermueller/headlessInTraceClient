package org.intrace.client.connection.command;

import org.intrace.client.gui.helper.ControlConnectionThread;

public abstract class AbstractDefaultCommand implements IAgentCommand {

	private ControlConnectionThread m_controlConnectionThread = null;
	
	@Override
	public void setControlConnectionThread(ControlConnectionThread cct) {
		m_controlConnectionThread = cct;
	}

	@Override
	public ControlConnectionThread getControlConnectionThread() {
		return m_controlConnectionThread;
	}
	
	public abstract String getMessage();
	
	public void send() {
		this.getControlConnectionThread().sendMessage(this.getMessage());
	}

}
