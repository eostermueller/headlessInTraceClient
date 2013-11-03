package org.intrace.rcp;

public class ClientStrings {
	  public static final String DEFAULT_HOST_AND_PORT = "localhost:9123";
	  public static final String TRACE_CREATION_ERROR = "Failed to setup network trace";
	public static final String ALREADY_CONNECTED = "Already connected to this host:port";
	public static final String CACHED_CONNECTION_IS_DEAD = "A cached connection as found in ConnectionList but the state was not CONNECTED";
	public static final String HARD_DISSCONNECT_NO_LONGER_SUPPORTED = "public disconnect() is no longer supported.  Please use ClientList.disconnect()";
	public static final String CODING_ERROR = "Was not expecting this to happen -- probably a coding error";
	public static final String TAB_NAME_CONFIG_1 = "Config-1"; 
	public static final String TAB_NAME_TEXT_TRACE = "TextTrace";
	/**
	 * Column header for trace events.  The timestamp that the JVM Agent created the event.  Contrast this to the time that the InTrace UI received the event (Receipt Time).
	 */
	public static final String COL_NAME_EVENT_TS = "Event Time";
	/**
	 * Column header for trace events.  Computer name or IP that generated the event.
	 */
	public static final String COL_NAME_AGENT_NAME = "Agent Name";  
	/**
	 * Column header for trace events.  TCP/IP Port of Agent that generated the event.
	 */
	public static final String COL_NAME_AGENT_PORT = "Agent Port";  
	/**
	 * Column header for trace events.  TCP/IP Port of Agent that generated the event.
	 */
	public static final String COL_NAME_THREAD_ID = "Thread ID";  
	/**
	 * Column header for trace events.  Time the InTrace UI received the event.
	 */
	public static final String COL_NAME_RECEIPT_TS = "Receipt Time";  
	/**
	 * Column header for trace events.  Time the InTrace UI received the event.
	 */
	//public static final String COL_NAME_RAW_EVENT_DATA = "Event Data";  
	public static final String COL_NAME_RAW_EVENT_DATA = "rawEventData";
	/**
	 * Column header for trace events.  Event type.
	 */
	public static final String COL_NAME_RAW_EVENT_TYPE = "Event Type";  
	  
	
	
}
