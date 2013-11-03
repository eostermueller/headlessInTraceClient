package org.intrace.client.model;

import org.intrace.client.DefaultFactory;
import org.intrace.client.model.ITraceEvent.EventType;


public class DefaultTraceEventParser implements ITraceEventParser {
	private static final char TOKEN_COLON = ':';
	ITraceEventParser m_entryExitParser = new EntryAndExitTraceEventParser();
	ITraceEventParser m_otherParser = new OtherTraceEventParser();

	/* 
	 * For performance reasons, trying to create as few objects as possible in this class.
	 * Also for performance reasons, this code should also choose char searches over String searches when possible.
	 */
	@Override
	public ITraceEvent createEvent(String rawEventText, int sourceLineNumber) {

		int indexOfLastColon = rawEventText.lastIndexOf(TOKEN_COLON);
		if (indexOfLastColon==-1) {
			throw new RuntimeException("1000 Major exception.  There should be multiple colons in this event text, but found zero [" + rawEventText + "]");
		}
		sourceLineNumber = -1; //Input sourceLineNumber is ignored.  -1 is default used if trace event doesn't have line number, which probably means agent wasn't configured to provide it.
		
		//If the character to the left of the last colon is a curly, 
		//then the text to the right of the last colon is the source line number.
		char isCurlyBrace = rawEventText.charAt(indexOfLastColon-1);
		if (isCurlyBrace==ENTRY_MARKER || isCurlyBrace==EXIT_MARKER) {
			sourceLineNumber = Integer.parseInt(rawEventText.substring(indexOfLastColon+1));	
		} else { // then there is no line number at the end of the line, so see if there is a curly at the end of the line.
			isCurlyBrace = rawEventText.charAt(rawEventText.length()-1);
		}
		
		ITraceEvent event = null;
		if (isCurlyBrace==ENTRY_MARKER) {
			event = m_entryExitParser.createEvent(rawEventText, sourceLineNumber);
			event.setEventType(EventType.ENTRY );
		} else if (isCurlyBrace==EXIT_MARKER) {
			event = m_entryExitParser.createEvent(rawEventText, sourceLineNumber);
			event.setEventType(EventType.EXIT );
		} else {
			event = m_otherParser.createEvent(rawEventText, sourceLineNumber);
		}
		return event;
	}

}
class OtherTraceEventParser implements ITraceEventParser {

	@Override
	public ITraceEvent createEvent(String rawEventText, int sourceLineNumber) {
		ITraceEvent event = DefaultFactory.getFactory().getTraceEvent();
		event.setSourceLineNumber(sourceLineNumber);
		String[] pieces = rawEventText.split(SQUARE_BRACKET_DELIMITERS,5);
		/*
			[0]	"" (id=890)	
			[1]	"18:07:53.681" (id=880)	
			[2]	":" (id=893)	
			[3]	"67" (id=885)	
			[4]	":org.hsqldb.jdbc.jdbcStatement:<init>: Arg: 1003" (id=894)	

		 * 
		 */
		if (pieces.length != 5) {
			throw new RuntimeException("Expecting 3 parts (but got [" + pieces.length + "]) when parsing square brackets in raw text [" + rawEventText + "]");
		}
		event.setAgentTimeMillisString(pieces[1]);
		event.setThreadId(pieces[3]);
		
		String[] colonSeparatedPieces = pieces[4].split(":",5);
		/*
				[0]	"" (id=64)	
				[1]	"org.hsqldb.jdbc.jdbcStatement" (id=65)	
				[2]	"<init>" (id=66)	
				[3]	" Arg" (id=67)	
				[4]	" 1003" (id=68)	
		 * 
		 */
		int indexOfLastPeriod = colonSeparatedPieces[1].lastIndexOf(".");
		event.setPackageName(colonSeparatedPieces[1].substring(0, indexOfLastPeriod));
		event.setClassName(colonSeparatedPieces[1].substring(indexOfLastPeriod+1));
		event.setMethodName(colonSeparatedPieces[2]);
		event.setRawEventData(rawEventText);
		
		if (colonSeparatedPieces[3].trim().startsWith(METHOD_PARAMETER_MARKER_NAMED)) {
			event.setEventType(EventType.ARG);
			// Expecting colonSeparatedPieces[3] to look something like this: Arg (request)
			String[] morePieces = colonSeparatedPieces[3].split(PARENTHESIS_DELIMITERS);
			if (morePieces.length <2) {
				throw new RuntimeException("Couldn't find the argument name in this parameter.  Expecting to find one open and one close parenthesis: [" + colonSeparatedPieces[3] + "]");
			}
			
			if ( !morePieces[0].trim().equals(METHOD_PARAMETER_MARKER_UNNAMED)) {
				throw new RuntimeException("Had trouble parsing [" + colonSeparatedPieces[3] + "]");
			} else {
				event.setArgName(morePieces[1]);
			}
		} else if (METHOD_PARAMETER_MARKER_UNNAMED.equals(colonSeparatedPieces[3].trim())) {
			event.setEventType(EventType.ARG);
		} else if (METHOD_RETURN_VALUE_MARKER.equals(colonSeparatedPieces[3].trim())) {
			event.setEventType(EventType.RETURN);
		} else {
			throw new RuntimeException("Unable to determine trace event type for raw input [" + rawEventText + "]");
		}
		event.setValue(colonSeparatedPieces[4].trim());
		
		return event;
	}
}
/**
 * Here is an example of an ENTRY and an EXIT trace event:
 * <PRE>
[18:07:53.681]:[67]:org.hsqldb.jdbc.jdbcConnection:checkClosed: {
[18:07:53.681]:[67]:org.hsqldb.jdbc.jdbcConnection:checkClosed: }
	</PRE>
 * @author erikostermueller
 *
 */
class EntryAndExitTraceEventParser implements ITraceEventParser {

	@Override
	public ITraceEvent createEvent(String rawEventText, int sourceLineNumber) {
		ITraceEvent event = DefaultFactory.getFactory().getTraceEvent();
		event.setSourceLineNumber(sourceLineNumber);
		String[] pieces = rawEventText.split(SQUARE_BRACKET_DELIMITERS);
		if (pieces.length != 5) {
			throw new RuntimeException("Expecting 5 parts (but got [" + pieces.length + "]) when parsing square brackets in raw text [" + rawEventText + "]");
		}
		/**	Input:  "[18:07:53.681]:[67]:org.hsqldb.jdbc.jdbcStatement:<init>: }"
				[0]	"" (id=78)	
				[1]	"18:07:53.681" (id=79)	
				[2]	":" (id=80)	
				[3]	"67" (id=81)	
				[4]	":org.hsqldb.jdbc.jdbcStatement:<init>: }" (id=82)	
		 */
		
		event.setAgentTimeMillisString(pieces[1]);
		event.setThreadId(pieces[3]);
		
		String[] colonSeparatedPieces = pieces[4].split(":");
			/**  Input:  ":org.hsqldb.jdbc.jdbcStatement:<init>: }
				[0]	"" (id=84)	
				[1]	"org.hsqldb.jdbc.jdbcStatement" (id=85)	
				[2]	"<init>" (id=86)	
				[3]	" }" (id=87)	
			 * 
			 */
		int indexOfLastPeriod = colonSeparatedPieces[1].lastIndexOf(".");
		event.setPackageName(colonSeparatedPieces[1].substring(0, indexOfLastPeriod));
		event.setClassName(colonSeparatedPieces[1].substring(indexOfLastPeriod+1));
		event.setMethodName(colonSeparatedPieces[2]);
		event.setRawEventData(rawEventText);
		return event;
	}
	
}
