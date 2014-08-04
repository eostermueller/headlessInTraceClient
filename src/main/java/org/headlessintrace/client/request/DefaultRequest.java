package org.headlessintrace.client.request;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import org.headlessintrace.client.model.ITraceEvent;

public class DefaultRequest implements IRequest {
	private AtomicBoolean m_initialized = new AtomicBoolean(false);
	private List<ITraceEvent> m_events = new CopyOnWriteArrayList<ITraceEvent>();
	private String m_uniqueId = null;
	
	public DefaultRequest() {
		setUniqueId(UUID.randomUUID().toString());
	}

	@Override
	public String getUniqueId() {
		return m_uniqueId.toString();
	}

	@Override
	public void setUniqueId(String uniqueId) {
		this.m_uniqueId = uniqueId;
	}
	private String m_threadId = null;
	private String m_url;
	private String m_httpRespCode;
	@Override
	public List<ITraceEvent> getEvents() {
		return m_events;
	}

	@Override
	public void setEvents(List<ITraceEvent> events) {
		m_events = events;
	}

	@Override
	public void setThreadId(String threadId) {
		m_threadId = threadId;
	}
	@Override
	public String getThreadId(){ 
		return m_threadId;
	}

	@Override
	public String getUrl() {
		return m_url;
	}

	/**  Souce data looks like this:
HttpServlet raw [[16:19:06.380]:[19]:javax.servlet.http.HttpServlet:service: {:762]
HttpServlet raw [[16:19:06.380]:[19]:javax.servlet.http.HttpServlet:service: Arg (req): (GET /test/helloExecuteQuery)@376028835 org.eclipse.jetty.server.Request@1669bea3]
HttpServlet raw [[16:19:06.380]:[19]:javax.servlet.http.HttpServlet:service: Arg (res): HTTP/1.1 200 
]	 * 
	 */
	@Override
	public void setUrl(String val) {
		m_url = val;
		
	}

	@Override
	public String getHttpResponseCode() {
		return m_httpRespCode;
	}

	/**  Souce data looks like this:
HttpServlet raw [[16:19:06.380]:[19]:javax.servlet.http.HttpServlet:service: {:762]
HttpServlet raw [[16:19:06.380]:[19]:javax.servlet.http.HttpServlet:service: Arg (req): (GET /test/helloExecuteQuery)@376028835 org.eclipse.jetty.server.Request@1669bea3]
HttpServlet raw [[16:19:06.380]:[19]:javax.servlet.http.HttpServlet:service: Arg (res): HTTP/1.1 200 
]	 * 
	 */
	@Override
	public void setHttpResponseCode(String val) {
		m_httpRespCode = val;
	}

	@Override
	public boolean isInitialized() {
		return this.m_initialized.get();
	}

	@Override
	public void setInitialized(boolean val) {
		m_initialized.set(val);
		
	}

}
