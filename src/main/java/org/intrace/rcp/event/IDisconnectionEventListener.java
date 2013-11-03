package org.intrace.rcp.event;
import java.util.EventListener;
public interface IDisconnectionEventListener extends EventListener {
	void connectionBroken(DisconnectionEvent disconnectionEvent);

}
