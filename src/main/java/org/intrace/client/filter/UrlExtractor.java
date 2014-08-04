package org.intrace.client.filter;

import org.intrace.client.model.ITraceEvent;
import org.intrace.client.model.ITraceEvent.EventType;
import org.intrace.client.request.ICompletedRequestCallback;
import org.intrace.client.request.IRequest;

/**  This shows the raw events processed by this class.
 Event [[11:24:14.248]:[14]:javax.servlet.http.HttpServlet:service: Arg (req): (GET /test/hello)@1925384448 org.eclipse.jetty.server.Request@72c30900]
 Event [[11:24:14.248]:[14]:javax.servlet.http.HttpServlet:service: Arg (res): HTTP/1.1 200 
 Event [[11:24:14.249]:[14]:javax.servlet.http.HttpServlet:service: Arg (lastModified): (GET /test/hello)@1925384448 org.eclipse.jetty.server.Request@72c30900]
 Event [[11:24:14.249]:[14]:javax.servlet.http.HttpServlet:service: Arg (lastModified): HTTP/1.1 200 
 * 
 * 
 * @author erikostermueller
 *
 */
public class UrlExtractor implements ICompletedRequestCallback {

	ICompletedRequestCallback m_callback = null;
	public UrlExtractor(ICompletedRequestCallback val) {
		m_callback = val;
	}
	@Override
	public void requestCompleted(IRequest events) {
		extractUrl(events);
		m_callback.requestCompleted(events);
	}
	private void extractUrl(IRequest events) {
		for(ITraceEvent e : events.getEvents()) {
			if ("HttpServlet".equals(e.getClassName() )
				&& "javax.servlet.http".equals(e.getPackageName() ) ) {
				System.out.println("HttpServlet raw [" + e.getRawEventData() + "]");
				
				events.getEvents().remove(e);
				
				if (e.getEventType().equals(EventType.ARG) ) {
					
					if (e.getValue().contains("POST")
							|| e.getValue().contains("GET"))
						events.setUrl(e.getRawEventData());
					else if (e.getValue().contains("HTTP")) 
						events.setUrl(e.getValue());
				} 
			}
		}
		
	}


}
