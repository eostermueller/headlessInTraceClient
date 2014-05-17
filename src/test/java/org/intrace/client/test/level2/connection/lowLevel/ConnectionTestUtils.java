package org.intrace.client.test.level2.connection.lowLevel;

import java.util.List;

import org.intrace.client.connection.ConnectState;

public class ConnectionTestUtils {
	public static final String DEFAULT_HOST_1 = "localhost";
	public static final String DEFAULT_PORT_1 = "9123";
	public static final int DEFAULT_PORT_1_INT = 9123;
	public static final String DEFAULT_HOST_2 = "localhost";
	public static final String DEFAULT_PORT_2 = "9124";
	public static final int WEB_REQUEST_INTRACE_PORT = 9125;
	public static final String TEST_CLASS_TO_INSTRUMENT_1 = "example.FirstTraceExample";
	public static final String TEST_WEB_CLASS_TO_INSTRUMENT = "org.hsqldb.jdbc.jdbcConnection";
	public static final String TEST_WEB_METHOD_TO_INSTRUMENT = "prepareStatement";
	
	/**
	 * org.intrace.test.webapp.servlet.HelloWorld.doGet
	 */
	public static final String TEST_WEB_REQUEST_COMPLETION_CLASS = "org.intrace.test.webapp.servlet.HelloWorld";
	public static final String TEST_WEB_REQUEST_COMPLETION_METHOD = "doGet";
	public static final long EVENT_COLLECTION_TIME_MS = 1000;
	
    public static boolean mostRecentMessageIsDisconnect(List<String> listMessages) {
    	String lastMessage = listMessages.get(listMessages.size()-1);
    	return lastMessage.equals(ConnectState.DISCONNECTED.toString());
    }
    public static boolean mostRecentMessageIsDisconnect2(List<ConnectState> listMessages) {
    	ConnectState lastMessage = listMessages.get(listMessages.size()-1);
    	return lastMessage.equals(ConnectState.DISCONNECTED);
    }
    public static boolean locateMessage(List<String> listMessages, String criteria) {
    	boolean ynRC = false; //assume we don'e find the specified text.
    	for(String msg : listMessages) {
    		//System.out.println("Comparing criteria [" + criteria + "] to [" + msg + "]");
    		if (msg.equalsIgnoreCase(criteria) ) {
    			ynRC = true;
    			break;
    		}
    	}
    	return ynRC;
    }
    public static boolean locateMessage2(List<ConnectState> listMessages, String criteria) {
    	boolean ynRC = false; //assume we don'e find the specified text.
    	for(ConnectState msg : listMessages) {
    		//System.out.println("Comparing criteria [" + criteria + "] to [" + msg.toString() + "]");
    		
    		if (msg.toString().contains(criteria) ) {
    			ynRC = true;
    			break;
    		}
    	}
    	return ynRC;
    }

}
