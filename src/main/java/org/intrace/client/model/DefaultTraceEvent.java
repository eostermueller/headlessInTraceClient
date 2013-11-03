package org.intrace.client.model;

import org.intrace.client.model.ITraceEvent.EventType;

/**
 * A pojo "Value Object"  -- not much code here, just data.
 * @author erikostermueller
 *
 */
public class DefaultTraceEvent implements ITraceEvent {

	private static final String DELIMIT = ":";

	public DefaultTraceEvent() {}
	public DefaultTraceEvent(
			String rawEventData,
			String packageName,
			String className,
			String agentTimeMillisString,
			String methodName,
			EventType eventType,
			String value,
			boolean isConstructor,
			String threadId,
			int sourceLineNumber,
			String argName) {
		setRawEventData(rawEventData);
		setPackageName(packageName);
		setClassName(className);
		setAgentTimeMillisString(agentTimeMillisString);
		setMethodName(methodName);
		setEventType(eventType);
		setValue(value);
		setConstructor(isConstructor);
		setThreadId(threadId);
		setSourceLineNumber(sourceLineNumber);
		setArgName(argName);
	}
	private String m_argName = null;
	@Override
	public void setArgName(String argName) {
		m_argName  = argName;
	}
	@Override
	public String getArgName() {
		return m_argName;
	}
	public void setValue(String value) {
		m_value = value;
	}
	
	@Override
	public String getPackageAndClass() {
		return getPackageName() + "." + getClassName();
	}
	@Override
	public boolean validate() {
		boolean ynRC = true;
		if(getClassName() == null 
			|| getClassName().trim().equals("")
			|| getMethodName() == null
			|| getMethodName().trim().equals("") ) {
			ynRC = false;
		}
		if (getEventType()==null) {
			ynRC = false;
		}
		return ynRC;
	}
	
	private String m_agentName = null;
	private short m_agentPort = -1;
	private String m_threadId = null;
	private String m_agentTimeMillisString = null;
	private String m_packageName = null;
	private String m_className = null;
	private String m_methodName = null;
	private EventType m_eventType = null;
	private long m_clientTimeMillis = -1;
	private String m_rawEventData = null;
	private boolean m_isConstructor;
	private String m_value;
	private int m_sourceLineNumber = -1;
	
	@Override
	public String getAgentName() {
		return m_agentName;
	}

	@Override
	public void setAgentHostName(String agentName) {
		m_agentName = agentName;

	}

	@Override
	public short getAgentPort() {
		return m_agentPort;
	}

	@Override
	public void setAgentPort(short agentPort) {
		m_agentPort = agentPort;
	}

	@Override
	public String getThreadId() {
		return m_threadId;
	}

	@Override
	public void setThreadId(String threadId) {
		m_threadId = threadId;
	}

	@Override
	public long getAgentTimeMillis() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getAgentTimeMillisString() {
		return m_agentTimeMillisString;
	}
	@Override
	public void setAgentTimeMillis(long agentTimeMillis) {
		// TODO Auto-generated method stub

	}

	@Override
	public long getClientTimeMillis() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setClientTimeMillis(long receiptTimeMillis) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getRawEventData() {
		return m_rawEventData;
	}

	@Override
	public void setRawEventData(String rawEventData) {
		m_rawEventData = rawEventData;
	}

	@Override
	public EventType getEventType() {
		return m_eventType;
	}

	@Override
	public void setEventType(EventType eventType) {
		m_eventType = eventType;
	}

	@Override
	public String getPackageName() {
		return m_packageName;
	}

	@Override
	public void setPackageName(String packageName) {
		m_packageName = packageName;
	}

	@Override
	public String getClassName() {
		return m_className;
	}

	@Override
	public void setClassName(String className) {
		m_className = className;
	}

	@Override
	public String getMethodName() {
		return m_methodName;
	}

	@Override
	public void setMethodName(String method) {
		m_methodName = method;
		if (m_methodName!=null) {
			if (DefaultTraceEventParser.CONSTRUCTOR_METHOD_MARKER.equals(m_methodName)) {
				setConstructor(true);
			}
		}
	}

	@Override
	public boolean isConstructor() {
		return m_isConstructor;
	}

	@Override
	public String getValue() {
		return m_value;
	}
	@Override
	public void setAgentTimeMillisString(String val) {
		m_agentTimeMillisString = val;
	}
	@Override
	public void setConstructor(boolean val) {
		m_isConstructor = val;
	}
	@Override
	public int getSourceLineNumber() {
		return m_sourceLineNumber;
	}
	@Override
	public void setSourceLineNumber(int val) {
		m_sourceLineNumber = val;
		
	}
	@Override
	public String toString() {
		
		if (getRawEventData()!=null) {
			return getRawEventData();
		} else {
			return getPackageAndClass() + DELIMIT + getEventType();
		}
	}


}
