package org.headlessintrace.client.model;


import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * A pojo "Value Object"  -- not much code here, just data.
 * @author erikostermueller
 *
 */
public class DefaultTraceEvent implements ITraceEvent, java.io.Serializable {
	private static final String DELIMIT = ":";
	private static final long LONG_UNINIT = -1L;

	public DefaultTraceEvent() {
		super();
		this.setClientDateTimeMillis(System.currentTimeMillis());
	}
	public DefaultTraceEvent(
			String rawEventData,
			String packageName,
			String className,
			long agentTimeMillis,
			String methodName,
			EventType eventType,
			String value,
			boolean isConstructor,
			String threadId,
			int sourceLineNumber,
			String argName) {
		this();
		setRawEventData(rawEventData);
		setPackageName(packageName);
		setClassName(className);
		
		Date d = new Date();
		
		setAgentTimeMillis(agentTimeMillis);
		setMethodName(methodName);
		setEventType(eventType);
		setValue(value);
		setConstructor(isConstructor);
		setThreadId(threadId);
		setSourceLineNumber(sourceLineNumber);
		setArgName(argName);
	}
	private long m_agentTimeMillis = LONG_UNINIT;
	private StackTraceElement[] m_stackTrace;
	@Override
	public StackTraceElement[] getStackTrace() {
		return m_stackTrace;
	}
//	@Override 
//	public String getAgentDateTimeString() {
//		DateTime dt = new DateTime();
//	    DateTimeFormatter fmt = DateTimeFormat.forPattern("MM/dd.YYYY");
//	    String str = fmt.print(dt);		
//	}
	@Override
	public void setStackTrace(StackTraceElement[] stackTrace) {
		this.m_stackTrace = stackTrace;
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
	private String m_packageName = null;
	private String m_className = null;
	private String m_methodName = null;
	private EventType m_eventType = null;
	private long m_clientDateTimeMillis = -1;
	private String m_rawEventData = null;
	private boolean m_isConstructor;
	private String m_value;
	private int m_sourceLineNumber = -1;
	//private StackTraceElement[] m_stackTrace = null;
	
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
		return m_agentTimeMillis;
	}

	@Override
	public void setAgentTimeMillis(long val) {
		m_agentTimeMillis = val;
 	}

	@Override
	public long getClientDateTimeMillis() {
		return m_clientDateTimeMillis;
	}

	@Override
	public void setClientDateTimeMillis(long receiptTimeMillis) {
		m_clientDateTimeMillis = receiptTimeMillis;
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
			if (ITraceEventParser.CONSTRUCTOR_METHOD_MARKER.equals(m_methodName)) {
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
    private static final String SIMPLE_DATE_FORMAT_PATTERN = "yyyy-MM-dd kk:mm:ss.SSS";
	private SimpleDateFormat dateFormat = new SimpleDateFormat(SIMPLE_DATE_FORMAT_PATTERN);
    public synchronized String format(Date date) {
        return dateFormat.format(date);
    }
	@Override
	public String getAgentDateTimeString() {
		Date d = new Date(this.getAgentTimeMillis());
		
		return format(d);
	}


}
