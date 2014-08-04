package org.headlessintrace.rcp.event;
import java.util.EventListener;
public interface IConnectionEventListener extends EventListener {
	void connectionEstablished(ConnectionEvent connectionEvent);

}
