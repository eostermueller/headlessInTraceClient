package org.intrace.client.model;

import java.util.ArrayList;
import java.util.List;

import org.intrace.client.DefaultFactory;
import org.intrace.client.IntraceException;
import org.intrace.client.model.ITraceEvent.EventType;


public class DefaultTraceEventParser implements ITraceEventParser {

	private static final int UNINIT = -1;
	private static final char TOKEN_COLON = ':';
	private static final char TOKEN_TILDE = '~';
	private static final String STACK_TRACE_ELE_DELIMETER = ",";
	private static final int UNKNOWN = -1;
	public static final int UNKNOWN_SOURCE = -52;
	public static final String UNKNOWN_SOURCE_STR = "Unknown Source";
	ITraceEventParser m_entryExitParser = new EntryAndExitTraceEventParser();
	ITraceEventParser m_otherParser = new OtherTraceEventParser();
	
	@Override
	public StackTraceElement createStackTraceElement(String singleStackTraceElement) {
		StackTraceElement ste = null;
		if (singleStackTraceElement.trim().length() > 0) {
			singleStackTraceElement = singleStackTraceElement.trim();
			String parts[] = singleStackTraceElement.split("[\\(:\\)]");
			if (parts.length>=2) {
				String packageAndClassAndMethod = parts[0];
	            String fileName = parts[1];
	            String lineNumber = "";
	            if (parts.length >= 3) {
	            	lineNumber = parts[2];
	            }
				int intLineNumber = UNKNOWN;

				int lastPeriod = packageAndClassAndMethod.lastIndexOf('.');
				if (lastPeriod > 1) {
					String declaringClass = packageAndClassAndMethod.substring(0, lastPeriod);
					String methodName = packageAndClassAndMethod.substring(lastPeriod+1);
					
					if (UNKNOWN_SOURCE_STR.equals(lineNumber.trim())) {
						intLineNumber = UNKNOWN_SOURCE;
					} else {
						try {
							intLineNumber = Integer.parseInt(lineNumber);
						} catch(NumberFormatException nfe) {
							//don't have a good place to stick this error/data yet.
						}
					}
					ste = new StackTraceElement(declaringClass, methodName, fileName, intLineNumber);
				} else {
					throw new RuntimeException("error parsing stacktrace. Expecting to find a period that separated the class name and method, but instead found this data [" + singleStackTraceElement + "]");
				}
			} else {
				throw new RuntimeException("Error parsing stack trace element [" + singleStackTraceElement + "]  Expecting 3 split parts, but only found [" + parts.length + "]");
			}
		}
		return ste;
	}
	/**
	 * 
	 * @param string in this format:  18:07:53.681
	 * @return number of milliseconds since midnight.
	 * @throws IntraceException 
	 */
	static public long convertInTraceAgentTimeFmtToLong(String string) throws IntraceException {
		
		//18:07:53.681
		String delimsRegEx = "[:.]";
		String[] timeParts = string.split(delimsRegEx);
		if (timeParts.length!=4)
			throw new IntraceException("Received invalid date [" + string + "].  Was expecting 4 pices 'split' by regex [" + delimsRegEx + "]");

		int militaryHours = UNINIT;
		int minutes = UNINIT;
		int seconds = UNINIT;
		int millis = UNINIT;

		try {
			militaryHours = Integer.parseInt(timeParts[0]);
			minutes = Integer.parseInt(timeParts[1]);
			seconds = Integer.parseInt(timeParts[2]);
			millis = Integer.parseInt(timeParts[3]);
			
		} catch (NumberFormatException nfe) {
			throw new IntraceException(nfe,"Unparseable InTrace data [" + string + "]");
		}
		
		militaryHours *= 60*60*1000;
		minutes *= 60*1000;
		seconds *= 1000;
		
		return militaryHours+minutes+seconds+millis;
	}

	/* 
	 * For performance reasons, trying to create as few objects as possible in this class.
	 * Also for performance reasons, this code should also choose char searches over String searches when possible.
	 */
	@Override
	public ITraceEvent createEvent(String rawEventText, int sourceLineNumber) throws IntraceException {
		
		if (rawEventText==null){
			return null;
		}
		String parts[] = rawEventText.split("~");
		rawEventText = parts[0];
		StackTraceElement[] stackTrace = null;
		if (parts.length==2) {
			stackTrace = parseStackTrace(parts[1]);
		}

		int indexOfLastColon = rawEventText.lastIndexOf(TOKEN_COLON);
		if (indexOfLastColon==-1) {
			throw new RuntimeException("1000 Major exception.  To separate parts of a date, there should be multiple colons in this event text, but found zero [" + rawEventText + "]");
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
		
		event.setStackTrace(stackTrace);
		return event;
	}

	/**
	 * 
	 * @param comma delimited string, where each token represents a single element in the stack.
	 * @return
	 */
	@Override
	public StackTraceElement[] parseStackTrace(String stackTrace) {
		String stackTraceElements[] = stackTrace.split(STACK_TRACE_ELE_DELIMETER);
		
		List<StackTraceElement> elements = new ArrayList<StackTraceElement>();
		for(String singleElement : stackTraceElements) {
			StackTraceElement ste = createStackTraceElement(singleElement);
			elements.add(ste);
		}
		
		return elements.toArray(new StackTraceElement[elements.size()]);
	}

}
class OtherTraceEventParser implements ITraceEventParser {

	private static final String DEBUG_EVENT_INDICATOR = ":DEBUG:";

	@Override
	public ITraceEvent createEvent(String rawEventText, int sourceLineNumber) throws IntraceException {
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
			throw new RuntimeException("Expecting 5 parts (but got [" + pieces.length + "]) when parsing square brackets in raw text [" + rawEventText + "]");
		}
		event.setAgentTimeMillis(DefaultTraceEventParser.convertInTraceAgentTimeFmtToLong(pieces[1]));
		event.setThreadId(pieces[3]);
		int indexOfDebug = rawEventText.indexOf(DEBUG_EVENT_INDICATOR);
		
		if (indexOfDebug > 0) {
			event.setValue(  rawEventText.substring(indexOfDebug+DEBUG_EVENT_INDICATOR.length()+1) );
			event.setEventType(EventType.DEBUG);
			event.setRawEventData(rawEventText);
		} else {
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
			if (indexOfLastPeriod==-1) {
				throw new IntraceException("Trouble parsing event [" + rawEventText + "] colonSeparatedPieces.length [" + colonSeparatedPieces.length + "] colonSeparatedPieces[1] [" + colonSeparatedPieces[1] + "]");
			}
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
			
		}
		
		return event;
	}


	public StackTraceElement createStackTraceElement(String singleStackTraceElement) {
		throw new UnsupportedOperationException();
	}

	@Override
	public StackTraceElement[] parseStackTrace(String stackTrace) {
		throw new UnsupportedOperationException();
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
	public ITraceEvent createEvent(String rawEventText, int sourceLineNumber) throws IntraceException {
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
		
		event.setAgentTimeMillis( DefaultTraceEventParser.convertInTraceAgentTimeFmtToLong(pieces[1]));
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

	public StackTraceElement createStackTraceElement(String singleStackTraceElement) {
		throw new UnsupportedOperationException();
	}

	@Override
	public StackTraceElement[] parseStackTrace(String stackTrace) {
		throw new UnsupportedOperationException();
	}
	
}
