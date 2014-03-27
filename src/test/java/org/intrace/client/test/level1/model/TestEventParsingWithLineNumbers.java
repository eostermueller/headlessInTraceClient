package org.intrace.client.test.level1.model;

import static org.junit.Assert.*;

import org.intrace.client.IntraceException;
import org.intrace.client.model.DefaultTraceEventParser;
import org.intrace.client.model.ITraceEvent;
import org.intrace.client.model.ITraceEventParser;
import org.intrace.client.model.ITraceEvent.EventType;
import org.intrace.client.model.DefaultTraceEvent;
import org.junit.Ignore;
import org.junit.Test;

/**  This tester will attempt to parse most of these InTrace agent events:
 * Each line consists of a text event sent back from the InTrace agent.
 * <PRE>
	[13:47:21.068]:[1]:example.FirstTraceExample:intArrayMethod: }:70
 * </PRE>
 * @author erikostermueller
 *
 */
public class TestEventParsingWithLineNumbers {
	private static final long EXPECTED_TIMESTAMP_2 = 65273681;/*"18:07:53.681"*/
	ITraceEventParser m_eventParser = new DefaultTraceEventParser();


	private void validateSomeEventValues(ITraceEvent expectedEvent, ITraceEvent actualEvent) {
		assertEquals("Could not find package name", expectedEvent.getPackageName().toLowerCase(), actualEvent.getPackageName().toLowerCase());
		assertEquals("Could not find class name", expectedEvent.getClassName().toLowerCase(), actualEvent.getClassName().toLowerCase());
		//assertEquals("Could not find agentTime", expectedEvent.getAgentTimeMillis(), actualEvent.getAgentTimeMillis());
		assertEquals("Could not find method name", expectedEvent.getMethodName().toLowerCase(), actualEvent.getMethodName().toLowerCase());
		assertEquals("Could not find event type", expectedEvent.getEventType(), actualEvent.getEventType());
		assertEquals("Could not find arg or return value", expectedEvent.getValue(), actualEvent.getValue());
		assertEquals("Could not find constructor indicator", expectedEvent.isConstructor(), actualEvent.isConstructor());
		assertEquals("Could not find thread id", expectedEvent.getThreadId(), actualEvent.getThreadId());
		assertEquals("Could not find source code line number", expectedEvent.getSourceLineNumber(), actualEvent.getSourceLineNumber());
		
	}
	@Test
	public void canParseTraceEventWithLineNumber_ENTRY() throws IntraceException {
		String rawEventText = "[18:07:53.681]:[1]:example.FirstTraceExample:intArrayMethod: }:70";
		ITraceEvent expectedEvent = new DefaultTraceEvent(
				rawEventText,						//raw event text
				"example",					//package
				"FirstTraceExample",					//class
				EXPECTED_TIMESTAMP_2,									//time in millis
				"intArrayMethod",					//method
				EventType.EXIT,					//trace event type
				null,								//arg or return value
				false,								//isConstructor
				"1",								//threadId
				70,								//Source Code line number (default).
				null);							//Name of the argument
		ITraceEvent actualEvent = m_eventParser.createEvent(rawEventText,0);
		validateSomeEventValues(expectedEvent, actualEvent);
		
	}

}
