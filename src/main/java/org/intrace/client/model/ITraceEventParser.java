package org.intrace.client.model;

import java.util.regex.Pattern;

import org.intrace.client.IntraceException;


public interface ITraceEventParser {
	public static final String SQUARE_BRACKET_DELIMITERS = "[\\[\\]]";
	public static final String PARENTHESIS_DELIMITERS = "[" + Pattern.quote("(") + Pattern.quote(")") + "]";
	public static final char ENTRY_MARKER = '{';
	public static final char EXIT_MARKER = '}';
	public static final String CONSTRUCTOR_METHOD_MARKER = "<init>";
	public static final String METHOD_PARAMETER_MARKER_UNNAMED = "Arg";
	public static final String METHOD_PARAMETER_MARKER_NAMED = "Arg (";
	public static final String METHOD_RETURN_VALUE_MARKER = "Return";

	public ITraceEvent createEvent(String rawEventText, int sourceLineNumber) throws IntraceException;

	public abstract StackTraceElement createStackTraceElement(String singleStackTraceElement);

	StackTraceElement[] parseStackTrace(String stackTrace);

}