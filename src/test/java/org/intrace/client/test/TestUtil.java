package org.intrace.client.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.intrace.client.DefaultFactory;
import org.intrace.client.IntraceException;
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
	 * @throws IntraceException 
	 * 
	 * @Return an array of parsed events.
	 * 
	 */
	public static List<ITraceEvent> createEvents(String textTraceEvents) throws IntraceException {
		String[] aryEvents = textTraceEvents.split("\n");
		List<ITraceEvent> events = new ArrayList<ITraceEvent>();
		ITraceEventParser parser = DefaultFactory.getFactory().getEventParser();
		for(String oneEvent : aryEvents) {
			events.add( parser.createEvent(oneEvent, 0));
		}
		return events;
	}
	/**
	 * Taken from here:  http://computing.dcu.ie/~humphrys/Notes/Networks/java.html
	 * @param url
	 * @throws IOException
	 */
	public static void sendHttpGet(String url) throws IOException {
		 Socket s = new Socket("localhost", 8080);

        OutputStream out = s.getOutputStream();
        PrintWriter outw = new PrintWriter(out, false);
        outw.print("GET " + url + " HTTP/1.0\r\n");
        outw.print("Accept: text/plain, text/html, text/*\r\n");
        outw.print("\r\n");
        outw.flush();		
        InputStream in = s.getInputStream();
        InputStreamReader inr = new InputStreamReader(in);
        BufferedReader br = new BufferedReader(inr);
        String line;
        while ((line = br.readLine()) != null) 
        {
                System.out.println(line);
        }
        
        s.close();
	}

}
