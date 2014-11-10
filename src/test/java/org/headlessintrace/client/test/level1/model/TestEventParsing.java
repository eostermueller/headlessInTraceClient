package org.headlessintrace.client.test.level1.model;

import static org.junit.Assert.*;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.headlessintrace.client.IntraceException;
import org.headlessintrace.client.model.DefaultTraceEventParser;
import org.headlessintrace.client.model.ITraceEvent;
import org.headlessintrace.client.model.ITraceEventParser;
import org.headlessintrace.client.model.ITraceEvent.EventType;
import org.headlessintrace.client.model.DefaultTraceEvent;
import org.junit.Ignore;
import org.junit.Test;

/**  This tester will attempt to parse most of these InTrace agent events:
 * Each line consists of a text event sent back from the InTrace agent.
 * <PRE>
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
 * </PRE>
 * @author erikostermueller
 *
 */
public class TestEventParsing {
	//private static final long EXPECTED_TIMESTAMP = 46090792L;/*"12:48:10.792"*/
	private static final long EXPECTED_TIMESTAMP = 1415299690792L;/*"12:48:10.792"*/
	
	//eto private static final long EXPECTED_TIMESTAMP_2 = 65273681;/*"18:07:53.681"*/
	private static final long EXPECTED_TIMESTAMP_2 = 1415318873681l;/*"18:07:53.681"*/

	ITraceEventParser m_eventParser = new DefaultTraceEventParser();


	private void validateSomeEventValues(ITraceEvent expectedEvent, ITraceEvent actualEvent) {
		assertEquals("Could not find package name", expectedEvent.getPackageName().toLowerCase(), actualEvent.getPackageName().toLowerCase());
		assertEquals("Could not find class name", expectedEvent.getClassName().toLowerCase(), actualEvent.getClassName().toLowerCase());
		assertEquals("Could not find agentTime", expectedEvent.getAgentTimeMillis(), actualEvent.getAgentTimeMillis());
		assertEquals("Could not find method name", expectedEvent.getMethodName().toLowerCase(), actualEvent.getMethodName().toLowerCase());
		assertEquals("Could not find event type", expectedEvent.getEventType(), actualEvent.getEventType());
		assertEquals("Could not find arg or return value", expectedEvent.getValue(), actualEvent.getValue());
		assertEquals("Could not find constructor indicator", expectedEvent.isConstructor(), actualEvent.isConstructor());
		assertEquals("Could not find thread id", expectedEvent.getThreadId(), actualEvent.getThreadId());
		assertEquals("Could not find Argument name", expectedEvent.getArgName(), actualEvent.getArgName());
		
	}
	@Test
	public void canParseTraceEvent_ENTRY() throws IntraceException {
		String rawEventText = "[18:07:53.681]:[67]:org.hsqldb.jdbc.jdbcConnection:prepareStatement: {";
		ITraceEvent expectedEvent = new DefaultTraceEvent(
				rawEventText,						//raw event text
				"org.hsqldb.jdbc",					//package
				"jdbcConnection",					//class
				setCurrDate(EXPECTED_TIMESTAMP_2)/*"18:07:53.681"*/,						//time in millis
				"prepareStatement",					//method
				EventType.ENTRY,					//trace event type
				null,								//arg or return value
				false,								//isConstructor
				"67",								//threadId
				-1,								//Source Code line number (default).
				null);							//Name of the argument
		
		ITraceEvent actualEvent = m_eventParser.createEvent(rawEventText,0);
		validateSomeEventValues(expectedEvent, actualEvent);
		
	}
	@Test 
	public void canParseTraceEvent_ARG() throws IntraceException {
		String rawEventText = "[18:07:53.681]:[67] :org.hsqldb.jdbc.jdbcConnection:prepareStatement: Arg: INSERT INTO Event (name, description, date, location) VALUES(?, ?, ?, ?)";
		ITraceEvent expectedEvent = new DefaultTraceEvent(
				rawEventText,						//raw event text
				"org.hsqldb.jdbc",					//package
				"jdbcConnection",					//class
				setCurrDate(EXPECTED_TIMESTAMP_2)/*"18:07:53.681"*/,						//time in millis
				//setCurrDate(1415318873681l),
				"prepareStatement",					//method
				EventType.ARG,						//trace event type
				"INSERT INTO Event (name, description, date, location) VALUES(?, ?, ?, ?)",								//arg or return value
				false,								//isConstructor
				"67",								//threadId
				-1,								//Source Code line number (default).
				null);							//Name of the argument
		ITraceEvent actualEvent = m_eventParser.createEvent(rawEventText,0);
		validateSomeEventValues(expectedEvent, actualEvent);
	}

	@Test 
	public void canParseTraceEvent_ENTRY2() throws IntraceException {
		String rawEventText = "[18:07:53.681]:[67]:org.hsqldb.jdbc.jdbcConnection:checkClosed: {";
		ITraceEvent expectedEvent = new DefaultTraceEvent(
				rawEventText,						//raw event text
				"org.hsqldb.jdbc",	//package
				"jdbcConnection",					//class
				setCurrDate(EXPECTED_TIMESTAMP_2)/*"18:07:53.681"*/,									//time in millis
				"checkClosed",					//method
				EventType.ENTRY,					//trace event type
				null,								//arg or return value
				false,								//isConstructor
				"67",								//threadId
				-1,								//Source Code line number (default).
				null);							//Name of the argument		
		ITraceEvent actualEvent = m_eventParser.createEvent(rawEventText,0);
		validateSomeEventValues(expectedEvent, actualEvent);
	}
	@Test 
	public void canParseTraceEvent_EXIT() throws IntraceException {
		String rawEventText = "[18:07:53.681]:[67]:org.hsqldb.jdbc.jdbcConnection:checkClosed: }";
		ITraceEvent expectedEvent = new DefaultTraceEvent(
				rawEventText,						//raw event text
				"org.hsqldb.jdbc",	//package
				"jdbcConnection",					//class
				setCurrDate(EXPECTED_TIMESTAMP_2)/*"18:07:53.681"*/,									//time in millis
				"checkClosed",					//method
				EventType.EXIT,					//trace event type
				null,								//arg or return value
				false,								//isConstructor
				"67",								//threadId
				-1,								//Source Code line number (default).
				null);							//Name of the argument
		
		ITraceEvent actualEvent = m_eventParser.createEvent(rawEventText,0);
		validateSomeEventValues(expectedEvent, actualEvent);
	}
	@Test 
	public void canParseTraceEvent_CONSTRUCTOR_ENTRY() throws IntraceException {
		String rawEventText = "[18:07:53.681]:[67]:org.hsqldb.jdbc.jdbcStatement:<init>: {";
		ITraceEvent expectedEvent = new DefaultTraceEvent(
				rawEventText,						//raw event text
				"org.hsqldb.jdbc",	//package
				"jdbcStatement",					//class
				setCurrDate(EXPECTED_TIMESTAMP_2)/*"18:07:53.681"*/,									//time in millis
				"<init>",							//method
				EventType.ENTRY,					//trace event type
				null,								//arg or return value
				true,								//isConstructor
				"67",								//threadId
				-1,								//Source Code line number (default).
				null);							//Name of the argument		
		ITraceEvent actualEvent = m_eventParser.createEvent(rawEventText,0);
		validateSomeEventValues(expectedEvent, actualEvent);
	}

	@Test 
	public void canParseTraceEvent_CONSTRUCTOR_ARG() throws IntraceException {
		String rawEventText = "[18:07:53.681]:[67]:org.hsqldb.jdbc.jdbcStatement:<init>: Arg: org.hsqldb.jdbc.jdbcConnection@682f8c99";
		ITraceEvent expectedEvent = new DefaultTraceEvent(
				rawEventText,						//raw event text
				"org.hsqldb.jdbc",	//package
				"jdbcStatement",					//class
				setCurrDate(EXPECTED_TIMESTAMP_2)/*"18:07:53.681"*/,									//time in millis
				"<init>",							//method
				EventType.ARG,					//trace event type
				"org.hsqldb.jdbc.jdbcConnection@682f8c99",								//arg or return value
				true,								//isConstructor
				"67",								//threadId
				-1,								//Source Code line number (default).
				null);							//Name of the argument		
		ITraceEvent actualEvent = m_eventParser.createEvent(rawEventText,0);
		validateSomeEventValues(expectedEvent, actualEvent);
	}
//[18:07:53.681]:[67]:org.hsqldb.jdbc.jdbcStatement:<init>: Arg: 1003
	@Test 
	public void canParseTraceEvent_CONSTRUCTOR_ARG2() throws IntraceException {
		String rawEventText = "[18:07:53.681]:[67]:org.hsqldb.jdbc.jdbcStatement:<init>: Arg: 1003";
		ITraceEvent expectedEvent = new DefaultTraceEvent(
				rawEventText,						//raw event text
				"org.hsqldb.jdbc",	//package
				"jdbcStatement",					//class
				setCurrDate(EXPECTED_TIMESTAMP_2)/*"18:07:53.681"*/,									//time in millis
				"<init>",							//method
				EventType.ARG,					//trace event type
				"1003",								//arg or return value
				true,								//isConstructor
				"67",								//threadId
				-1, 							//Line number
				null);							//Arg name
		ITraceEvent actualEvent = m_eventParser.createEvent(rawEventText,0);
		validateSomeEventValues(expectedEvent, actualEvent);
	}
/** [18:07:53.681]:[67]:org.hsqldb.jdbc.jdbcStatement:<init>: }
 * @throws IntraceException 
 * 
 */
	@Test 
	public void canParseTraceEvent_CONSTRUCTOR_EXIT() throws IntraceException {
		String rawEventText = "[18:07:53.681]:[67]:org.hsqldb.jdbc.jdbcStatement:<init>: }";
		ITraceEvent expectedEvent = new DefaultTraceEvent(
				rawEventText,						//raw event text
				"org.hsqldb.jdbc",	//package
				"jdbcStatement",					//class
				setCurrDate(EXPECTED_TIMESTAMP_2)/*"18:07:53.681"*/,									//time in millis
				"<init>",							//method
				EventType.EXIT,					//trace event type
				null,								//arg or return value
				true,								//isConstructor
				"67",								//threadId
				-1,								//Source Code line number (default).
				null);							//Name of the argument
				
		ITraceEvent actualEvent = m_eventParser.createEvent(rawEventText,0);
		validateSomeEventValues(expectedEvent, actualEvent);
	}

	/**
	 * [18:07:53.682]:[67]:org.hsqldb.jdbc.jdbcConnection:nativeSQL: Return: INSERT INTO Event (name, description, date, location) VALUES(?, ?, ?, ?)
	 * @throws IntraceException 

	 */
	@Test 
	public void canParseTraceEvent_RETURN() throws IntraceException {
		String rawEventText = "[18:07:53.681]:[67]:org.hsqldb.jdbc.jdbcConnection:nativeSQL: Return: INSERT INTO Event (name, description, date, location) VALUES(?, ?, ?, ?)";
		ITraceEvent expectedEvent = new DefaultTraceEvent(
				rawEventText,						//raw event text
				"org.hsqldb.jdbc",	//package
				"jdbcConnection",					//class
				setCurrDate(EXPECTED_TIMESTAMP_2)/*"18:07:53.681"*/,									//time in millis
				"nativeSQL",							//method
				EventType.RETURN,					//trace event type
				"INSERT INTO Event (name, description, date, location) VALUES(?, ?, ?, ?)",								//arg or return value
				false,								//isConstructor
				"67",								//threadId
				-1,								//Source Code line number (default).
				null);							//Name of the argument				

		ITraceEvent actualEvent = m_eventParser.createEvent(rawEventText,0);
		validateSomeEventValues(expectedEvent, actualEvent);
	}
	/**
	 * This is an important test, because square brackets at the beginning of the raw trace event:  [18:07:53.683]:[67]
	 * are factored _out_ of the processing.  But in this particular example, there are several square brackets
	 * at the end of the raw trace event that must NOT be "factored out" -- instead the must be included in the event.setValue().
	 * This test confirms that, as desired, the first brackets are gone and the last ones are all present.  Wooohoo!
	 * @throws IntraceException 
	 */
	@Test
	public void canParseTraceEvent_RETURN_WithExtraSquareBrackets() throws IntraceException {
		String rawEventText = "[18:07:53.683]:[67]:org.hsqldb.jdbc.jdbcConnection:prepareStatement: Return: org.hsqldb.jdbc.jdbcPreparedStatement@4b8efa2f[sql=[INSERT INTO Event (name, description, date, location) VALUES(?, ?, ?, ?)], parameters=[[null], [null], [null], [null]]]";
		ITraceEvent expectedEvent = new DefaultTraceEvent(
				rawEventText,						//raw event text
				"org.hsqldb.jdbc",	//package
				"jdbcConnection",					//class
				//setCurrDate(65273683)/*"18:07:53.683"*/,									//time in millis
				setCurrDate(1415318873683l),
				"prepareStatement",							//method
				EventType.RETURN,					//trace event type
				"org.hsqldb.jdbc.jdbcPreparedStatement@4b8efa2f[sql=[INSERT INTO Event (name, description, date, location) VALUES(?, ?, ?, ?)], parameters=[[null], [null], [null], [null]]]",								//arg or return value
				false,								//isConstructor
				"67",								//threadId
				-1,								//Source Code line number (default).
				null);							//Name of the argument
		
		ITraceEvent actualEvent = m_eventParser.createEvent(rawEventText,0);
		validateSomeEventValues(expectedEvent, actualEvent);
	}
	
	//TODO @Test
	/**
	 * Need to capture a trace of this method and validate that we can parse it correctly.
	  <PRE>
	  	private String getStackTraceElementAsString(StackTraceElement[] stackTrace) {
		StringBuilder sb = new StringBuilder();
		for(StackTraceElement ele : stackTrace) {
			sb.append( ele.toString());
			sb.append("\n");
		}
		return sb.toString();
	}

	  </PRE>
	 * @throws IntraceException 
	 */
	public void canParseEventWith_array_return() throws IntraceException {
		String rawEventText = "[12:22:16.819]:[138]:org.headlessintrace.test.webapp.servlet.HelloWorld:getStackTraceElementAsString: Arg (arr$): Len:20 [java.lang.Thread.getStackTrace(Thread.java:1567),���org.headlessintrace.test.webapp.servlet.HelloWorld.doGe";
		ITraceEvent expectedEvent = new DefaultTraceEvent(
				rawEventText,						//raw event text
				"org.headlessintrace.test.webapp.servlet",	//package
				"HelloWorld",					//class
				setCurrDate(44536792)/*"12:22:16.819"*/,									//time in millis
				"getStackTraceElementAsString",							//method
				EventType.ARG,					//trace event type
				"org.hsqldb.jdbc.jdbcPreparedStatement@4b8efa2f[sql=[INSERT INTO Event (name, description, date, location) VALUES(?, ?, ?, ?)], parameters=[[null], [null], [null], [null]]]",								//arg or return value
				false,								//isConstructor
				"138",								//threadId
				-1,								//Source Code line number (default).
				null);							//Name of the argument
				
		ITraceEvent actualEvent = m_eventParser.createEvent(rawEventText,0);
		validateSomeEventValues(expectedEvent, actualEvent);
	}
	
	@Test
	public void canParseEventWithNamedArg() throws IntraceException {
		String rawEventText = "[12:48:10.792]:[59]:org.headlessintrace.test.webapp.servlet.HelloWorld:doGet: Arg (request): org.apache.catalina.connector.RequestFacade@c2aa254";
		ITraceEvent expectedEvent = new DefaultTraceEvent(
				rawEventText,						//raw event text
				"org.headlessintrace.test.webapp.servlet",	//package
				"HelloWorld",					//class
				setCurrDate(EXPECTED_TIMESTAMP)/*"12:48:10.792"*/,					//time in millis
				"doGet",							//method
				EventType.ARG,					//trace event type
				"org.apache.catalina.connector.RequestFacade@c2aa254",								//arg or return value
				false,								//isConstructor
				"59",								//threadId
				-1,								//Source Code line number (default).
				"request");							//Name of the argument				
		ITraceEvent actualEvent = m_eventParser.createEvent(rawEventText,0);
		validateSomeEventValues(expectedEvent, actualEvent);
	}
	@Test
	public void canParseTimestamp() throws IntraceException {
//		String rawEventText = "[12:48:10.792]:[59]:org.headlessintrace.test.webapp.servlet.HelloWorld:doGet: Arg (request): org.apache.catalina.connector.RequestFacade@c2aa254";
		String rawEventText = "[12:48:10.792]:[59]:org.headlessintrace.test.webapp.servlet.HelloWorld:doGet: Arg (request): org.apache.catalina.connector.RequestFacade@c2aa254";
		ITraceEvent actualEvent = m_eventParser.createEvent(rawEventText,0);
		
		assertEquals("Can't find string-version of timestamp.", setCurrDate(EXPECTED_TIMESTAMP) /*"12:48:10.792"*/, actualEvent.getAgentTimeMillis());
		
		//792			=     792
		//10*1000		=   10000
		//48*1000*60	= 2880000
		//12*1000*60*60	=43200000
		//Grand total:   46090792
		
//		assertEquals("Can't parse timestamp (ms)", setCurrDate(46090792), actualEvent.getAgentTimeMillis());
	}
	@Test
	public void canParseDebugEvent() throws IntraceException {
		String debugMessage = "Ignoring class not matching the active include regex: java.util.concurrent.locks.LockSupport";
		String rawEventText = "[12:48:10.792]:[12]:DEBUG: " + debugMessage;
		
		ITraceEvent expectedEvent = new DefaultTraceEvent(
				rawEventText,						//raw event text
				null,	//package
				null,					//class
				setCurrDate(EXPECTED_TIMESTAMP)/*"12:48:10.792"*/,					//time in millis
				null,							//method
				EventType.DEBUG,					//trace event type
				null,								//arg or return value
				false,								//isConstructor
				"12",								//threadId
				-1,								//Source Code line number (default).
				null);							//Name of the argument				

		ITraceEvent actualEvent = m_eventParser.createEvent(rawEventText,0);

		assertEquals("Could not find debug message", debugMessage, actualEvent.getValue());
		assertEquals("Could not find event type", expectedEvent.getEventType(), actualEvent.getEventType());

		assertEquals("Could not find agentTime", expectedEvent.getAgentTimeMillis(), actualEvent.getAgentTimeMillis());
		assertEquals("Could not find constructor indicator", expectedEvent.isConstructor(), actualEvent.isConstructor());
		assertEquals("Could not find thread id", expectedEvent.getThreadId(), actualEvent.getThreadId());
		assertEquals("Could not find Argument name", expectedEvent.getArgName(), actualEvent.getArgName());
	}
	
	/**
	 * 
	 * @param time
	 * @return given time with the current date.
	 * @throws IntraceException
	 */
	static public long setCurrDate(long time) throws IntraceException {
		Date currentDate = new Date();
		Date objTime = new Date(time);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy MMM dd HH:mm:ss.SSS");	
		
//		System.out.println("In Test util 1, starting with date: [" + sdf.format(currentDate) + "]");
//		System.out.println("In Test util 2, starting with time: [" + sdf.format(objTime) + "]");
		
		Calendar calTime = new GregorianCalendar();
		calTime.setTime(objTime);
		Calendar calDate = new GregorianCalendar();
		calDate.setTime(currentDate);
		
		Calendar calDateTime = new GregorianCalendar();
		
		calDateTime.set(Calendar.YEAR,			calDate.get(Calendar.YEAR) );
		calDateTime.set(Calendar.MONTH,			calDate.get(Calendar.MONTH) );
		calDateTime.set(Calendar.DAY_OF_MONTH,	calDate.get(Calendar.DAY_OF_MONTH) );

//		System.out.println("hourOfDay \t: " + calTime.get(Calendar.HOUR_OF_DAY));
//		System.out.println("minute \t\t: " + calTime.get(Calendar.MINUTE));
//		System.out.println("second \t\t: " + calTime.get(Calendar.SECOND));
//		System.out.println("millisecond \t: " + calTime.get(Calendar.MILLISECOND));		
		
		calDateTime.set(Calendar.HOUR_OF_DAY,	calTime.get(Calendar.HOUR_OF_DAY)); // 24 hour clock
		calDateTime.set(Calendar.MINUTE,		calTime.get(Calendar.MINUTE));
		calDateTime.set(Calendar.SECOND,		calTime.get(Calendar.SECOND));
		calDateTime.set(Calendar.MILLISECOND,	calTime.get(Calendar.MILLISECOND));
		
//		System.out.println("In Test util 3, starting with time: [" + sdf.format(calDateTime.getTime()) + "]");
		
		return calDateTime.getTime().getTime();
	}
	static public long addCurrDate_ORIG(long time) throws IntraceException {
		Date currentDate = new Date();
		Date objTime = new Date(time);
		
		String delimsRegEx = "[:.]";

		Calendar calTime = new GregorianCalendar();
		calTime.setTime(objTime);
		
		
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(currentDate);
		
		calendar.set(Calendar.HOUR_OF_DAY,calTime.get(Calendar.HOUR_OF_DAY)); // 24 hour clock
		calendar.set(Calendar.MINUTE,calTime.get(Calendar.MINUTE));
		calendar.set(Calendar.SECOND,calTime.get(Calendar.SECOND));
		calendar.set(Calendar.MILLISECOND,calTime.get(Calendar.MILLISECOND));
		
		return calendar.getTime().getTime();
	}

}
