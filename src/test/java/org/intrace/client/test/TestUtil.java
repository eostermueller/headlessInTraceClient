package org.intrace.client.test;

import java.io.BufferedReader;
import java.io.File;
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
	 * To create the following base64 string, I zipped up example/MyFirstTestImplementor.class and example/MyTestInterface.class
	 * into a jar file.  Then I used a free "base64 encode this file" to produce this text.
	 * http://webcodertools.com/imagetobase64converter

	* Will want to use these instructions to create the temporary folder/file:
	* http://garygregory.wordpress.com/2010/01/20/junit-tip-use-rules-to-manage-temporary-files-and-folders/
	* http://fahdshariff.blogspot.com/2013/03/junit-creating-temporary-files-using.html
	 */
	public static final String BASE_64_JAR_FILE = "UEsDBBQACAgIANYCx0QAAAAAAAAAAAAAAAAkAAQAZXhhbXBsZS9NeUZpcnN0VGVzdEltcGxlbWVudG9yLmNsYXNz/soAAH1RTU8TQRh+hm67ZV208lUVheIJTbDhDOFiIIFUPNBwn25f7JTt7mZ2SvAXeeYixIM/wB9leGZt0pAYLu8z887z8c7Mn7+/fgPYw/sIDbQjvMLrEG8i1LAR4m2IdyE2FRoHJjPuUKG28+FCIficD0XhRc9kcjadDMT29SBlZ7mXJzq90Nb4/awZuJEpFTo9udGTIpXul+/HxpauL6U78Y2JZC63+3S/zHPyd059iBKvGetr3U119q17kjmxdlo4GR7dJFI4k2fU1As9LUlVpwrReT61iRwbH7vx/5hP3jFGE4shtmJ00FLYfDolxjYWyXp6foXW3ObrYCyJU2jPNRXdu1/qRB6R+yMresirlKlIwXctrMncudPJVd+SzPg6f0ch5FctEDk8a8Rdl6iI9Y93ULfV8TPWRtWsIWaN/xGwhOdEuqM1EydkB8SlrXss/EDzbPcngtuKHjCrNjNbpxjMDGgVMrnJbkwzb94hy5+8xDJXtJrF+NUKVqvp1qqx1h8AUEsHCDQ1Ty6CAQAAawIAAFBLAwQUAAgICADVAsdEAAAAAAAAAAAAAAAAHQAAAGV4YW1wbGUvTXlUZXN0SW50ZXJmYWNlLmNsYXNzO/Vv1z4GBgZDBk52BnZ2Bg5GBua0/HxGBhYNL80wRgau4PzSouRUt8ycVEYGEd/KkNTiEs+8ktSitMTkVL2sxLJERgbx1IrE3IKcVH00aUYGAZAC/ZzEvHR9/6Ss1OQSNkYGRgYmBhBgZAFaxcACYjGwAkkmBjYAUEsHCMjcAnl4AAAAjAAAAFBLAQIUABQACAgIANYCx0Q0NU8uggEAAGsCAAAkAAQAAAAAAAAAAAAAAAAAAABleGFtcGxlL015Rmlyc3RUZXN0SW1wbGVtZW50b3IuY2xhc3P+ygAAUEsBAhQAFAAICAgA1QLHRMjcAnl4AAAAjAAAAB0AAAAAAAAAAAAAAAAA2AEAAGV4YW1wbGUvTXlUZXN0SW50ZXJmYWNlLmNsYXNzUEsFBgAAAAACAAIAoQAAAJsCAAAAAA==";
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
	public static void createTempJarFile(File unCreatedTempFile, String base64EncodedText) {
		
		
	}

}
