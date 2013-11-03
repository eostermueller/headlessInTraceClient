package org.intrace.client.test;

import java.util.ArrayList;
import java.util.List;

import org.intrace.client.DefaultFactory;
import org.intrace.client.model.ITraceEvent;
import org.intrace.client.model.ITraceEventParser;

public class TestUtil {
	/**
	 * Given a \n delimited string of test events like this:
	 * 
[08:36:43.885]:[412]:org.hsqldb.jdbc.jdbcConnection:prepareStatement: Arg: INSERT INTO Event (name, description, date, location) VALUES(?, ?, ?, ?)
[08:36:43.885]:[412]:java.lang.Thread:run: }:682
[08:36:43.885]:[413]:org.hsqldb.jdbc.jdbcConnection:prepareStatement: Arg: INSERT INTO Event (name, description, date, location) VALUES(?, ?, ?, ?)
[08:36:43.885]:[413]:java.lang.Thread:run: }:682
[08:36:43.885]:[414]:org.hsqldb.jdbc.jdbcConnection:prepareStatement: Arg: INSERT INTO Event (name, description, date, location) VALUES(?, ?, ?, ?)
[08:36:43.885]:[414]:java.lang.Thread:run: }:682
[08:36:43.885]:[415]:org.hsqldb.jdbc.jdbcConnection:prepareStatement: Arg: INSERT INTO Event (name, description, date, location) VALUES(?, ?, ?, ?)
[08:36:43.885]:[415]:java.lang.Thread:run: }:682
	 * 
	 * @Return an array of parsed events.
	 * 
	 */
	public static List<ITraceEvent> createEvents(String textTraceEvents) {
		String[] aryEvents = textTraceEvents.split("\n");
		List<ITraceEvent> events = new ArrayList<ITraceEvent>();
		ITraceEventParser parser = DefaultFactory.getFactory().getEventParser();
		for(String oneEvent : aryEvents) {
			events.add( parser.createEvent(oneEvent, 0));
		}
		return events;
	}

}
