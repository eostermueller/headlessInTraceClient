package org.headlessintrace.client.model;

import java.util.List;

import org.headlessintrace.client.model.ITraceEvent.EventType;

/**
 *  This is a very unsophisticated API.
 *  Simply put, there is one ITraceEvent for each event from the InTrace server agent.
 *  Below are some examples of InTrace events.
 *  Note the following "features"
 *  <ul>
 *  	<li>It takes multiple events to include all the information (entry time, exit time, method parameters, return value, etc...) for a single method call.</li>
 *		<li>The method name will be repeated for all events related to a single method invocation.</li>
 *  	<li>"Entry" and "Exit" methods for a particular method are bookends for all activity (calls to other methods) that happens in that method.  
 *  		For example, if the foo() method calls bar(), then the foo() Entry event will come first, followed by all bar() events, followed by the foo() Exit event.</li>
 *		<li>getValue() is set only for TraceType of ARG and RETURN</li>
 *   </ul>
 *   This API is not capable of the following:
 *   <ul>
 *  	<li>Because the Agent API doesn't support it, only the values of intrinsic types are stored for Arg and Return types.  Otherwise, just the object id (which includes the data type) is stored.</li>
 *  	<li>directly knowing how "nested" a particular method call is.</li>
 *  	<li></li>
 *  </ul>
 *  
 *  <PRE>
[18:07:53.681]:[67]:org.hsqldb.jdbc.jdbcConnection:prepareStatement: {
[18:07:53.681]:[67]:org.hsqldb.jdbc.jdbcConnection:prepareStatement: Arg: INSERT INTO Event (name, description, date, location) VALUES(?, ?, ?, ?)
[18:07:53.681]:[67]:org.hsqldb.jdbc.jdbcConnection:checkClosed: {
[18:07:53.681]:[67]:org.hsqldb.jdbc.jdbcConnection:checkClosed: }
[18:07:53.681]:[67]:org.hsqldb.jdbc.jdbcStatement:<init>: {
[18:07:53.681]:[67]:org.hsqldb.jdbc.jdbcStatement:<init>: Arg: org.hsqldb.jdbc.jdbcConnection@682f8c99
[18:07:53.681]:[67]:org.hsqldb.jdbc.jdbcStatement:<init>: Arg: 1003
[18:07:53.681]:[67]:org.hsqldb.jdbc.jdbcStatement:<init>: }
[18:07:53.682]:[67]:org.hsqldb.jdbc.jdbcConnection:nativeSQL: {
[18:07:53.682]:[67]:org.hsqldb.jdbc.jdbcConnection:nativeSQL: Arg: INSERT INTO Event (name, description, date, location) VALUES(?, ?, ?, ?)
[18:07:53.682]:[67]:org.hsqldb.jdbc.jdbcConnection:checkClosed: {
[18:07:53.682]:[67]:org.hsqldb.jdbc.jdbcConnection:checkClosed: }
[18:07:53.682]:[67]:org.hsqldb.jdbc.jdbcConnection:nativeSQL: Return: INSERT INTO Event (name, description, date, location) VALUES(?, ?, ?, ?)
[18:07:53.682]:[67]:org.hsqldb.jdbc.jdbcConnection:nativeSQL: }
[18:07:53.683]:[67]:org.hsqldb.jdbc.jdbcPreparedStatement:<init>: {
[18:07:53.683]:[67]:org.hsqldb.jdbc.jdbcPreparedStatement:<init>: Arg: org.hsqldb.jdbc.jdbcConnection@682f8c99
[18:07:53.683]:[67]:org.hsqldb.jdbc.jdbcPreparedStatement:<init>: Arg: INSERT INTO Event (name, description, date, location) VALUES(?, ?, ?, ?)
[18:07:53.683]:[67]:org.hsqldb.jdbc.jdbcPreparedStatement:<init>: Arg: 1003
[18:07:53.683]:[67]:org.hsqldb.jdbc.jdbcPreparedStatement:<init>: }
[18:07:53.683]:[67]:org.hsqldb.jdbc.jdbcConnection:prepareStatement: Return: org.hsqldb.jdbc.jdbcPreparedStatement@4b8efa2f[sql=[INSERT INTO Event (name, description, date, location) VALUES(?, ?, ?, ?)], parameters=[[null], [null], [null], [null]]]
[18:07:53.683]:[67]:org.hsqldb.jdbc.jdbcConnection:prepareStatement: }
[18:07:53.683]:[67]:org.hsqldb.jdbc.jdbcPreparedStatement:setString: {
[18:07:53.684]:[67]:org.hsqldb.jdbc.jdbcPreparedStatement:setString: Arg: 1
[18:07:53.684]:[67]:org.hsqldb.jdbc.jdbcPreparedStatement:setString: Arg: event name
[18:07:53.684]:[67]:org.hsqldb.jdbc.jdbcPreparedStatement:setParameter: {
[18:07:53.684]:[67]:org.hsqldb.jdbc.jdbcPreparedStatement:setParameter: Arg: 1
[18:07:53.684]:[67]:org.hsqldb.jdbc.jdbcPreparedStatement:setParameter: Arg: event name
[18:07:53.684]:[67]:org.hsqldb.jdbc.jdbcPreparedStatement:checkSetParameterIndex: {
[18:07:53.684]:[67]:org.hsqldb.jdbc.jdbcPreparedStatement:checkSetParameterIndex: Arg: 1
 *  </PRE>
 * 
 * @author erikostermueller
 *
 */
public interface ITraceEvent {

	public enum EventType {
			ENTRY, EXIT, ARG, RETURN, DEBUG
	}

	public String getAgentName();

	public void setAgentHostName(String agentName);

	public short getAgentPort();

	public void setAgentPort(short agentPort);

	public String getThreadId();

	public void setThreadId(String threadId);

	public long getAgentTimeMillis();

	public void setAgentTimeMillis(long agentTimeMillis);

	public long getClientDateTimeMillis();
	
	public void setClientDateTimeMillis(long receiptTimeMillis);

	public String getRawEventData();

	public void setRawEventData(String rawEventData);

	public EventType getEventType();

	public String getPackageName();

	public void setPackageName(String packageName);

	public String getClassName();

	public void setClassName(String className);
	
	public String getMethodName();
	
	public void setMethodName(String method);
		
	public boolean isConstructor();
	
	public void setConstructor(boolean val);
	
	public String getValue();
	
	public void setValue(String val);

	public void setEventType(EventType eventType);
	
	/**
	 * Say for a particular class, you have two 'get' methods.
	 * One takes an int parameter, the other takes a string.
	 * InTrace server agent events don't include the parameters. So these two methods will be indistinguishable, with the exception
	 * of this attribute, the source code line number.
	 * @return
	 */
	public int getSourceLineNumber();
	
	public void setSourceLineNumber(int val);

	public abstract boolean validate();

	public abstract String getPackageAndClass();

	public abstract String getArgName();

	public abstract void setArgName(String argName);

	public void setStackTrace(StackTraceElement[] stackTrace);

	public StackTraceElement[] getStackTrace();
}