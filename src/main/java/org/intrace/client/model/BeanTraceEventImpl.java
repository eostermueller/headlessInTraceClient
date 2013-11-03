package org.intrace.client.model;
/*
	*	ClientStrings.COL_NAME_AGENT_NAME,
	*	ClientStrings.COL_NAME_AGENT_PORT,
	*	ClientStrings.COL_NAME_THREAD_ID,
	*	ClientStrings.COL_NAME_EVENT_TS,
	*	ClientStrings.COL_NAME_RECEIPT_TS,
	*	ClientStrings.COL_NAME_RAW_EVENT_DATA,
	*	ClientStrings.COL_NAME_RAW_EVENT_TYPE
 * 
 */
public class BeanTraceEventImpl {
	public static final long UNINITIALIZED_TS = -1; 
	public static final short UNINITIALIZED_PORT = -2; 
	private String rawEventData = null;
	private Exception exception = null;

	/*
	 * ("<threadId>","<packageName>", "<className>",traceLine, "TRACE");
	 */
	public BeanTraceEventImpl(String rawEventData, String eventType, Exception ex) {
		this.rawEventData=rawEventData;
		this.exception = ex;
		
		// TODO Auto-generated constructor stub
	}
	
	/*
	 * ("<threadId>","<packageName>", "<className>",traceLine, "TRACE");
	 */
	public BeanTraceEventImpl(String rawEventData, String eventType) {
		this.rawEventData = rawEventData;
	}		
	/* (non-Javadoc)
	 * @see org.intrace.client.model.ITraceEvent#getRawEventData()
	 */
	
	public String getRawEventData() {
		return rawEventData;
	}
	
//	public Exception getException() {
//		// TODO Auto-generated method stub
//		return exception;
//	}
	
	
}
