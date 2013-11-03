package org.intrace.client.test.level2.connection;

import static org.junit.Assert.*;

import org.intrace.client.DefaultFactory;
import org.intrace.client.connection.ConnectionException;
import org.intrace.client.connection.ConnectionTimeout;
import org.intrace.client.connection.DefaultConnectionList;
import org.intrace.client.connection.HostPort;
import org.intrace.client.connection.IConnection;
import org.intrace.client.connection.command.ClassInstrumentationCommand;
import org.intrace.client.connection.command.IAgentCommand;
import org.intrace.client.filter.IncludeThisEventFilterExt;
import org.intrace.client.model.ITraceEvent;
import org.intrace.client.model.ITraceEvent.EventType;
import org.intrace.client.request.BadCompletedRequestListener;
import org.intrace.client.test.level2.connection.lowLevel.ConnectionTestUtils;
import org.intrace.client.test.level2.connection.lowLevel.TestCallback;
import org.intrace.shared.TraceConfigConstants;
import org.junit.Test;

public class TestEventFilterExt {

	@Test
	public void canCollectUnParsedEventsFromExternalAgent() throws BadCompletedRequestListener, InterruptedException {
		ClassInstrumentationCommand cic = new ClassInstrumentationCommand();
		cic.setIncludeClassRegEx(ConnectionTestUtils.TEST_CLASS_TO_INSTRUMENT_1);
		IAgentCommand commandArray[] = { cic };
		
		IncludeThisEventFilterExt filterExt = new IncludeThisEventFilterExt();
		ITraceEvent criteria = DefaultFactory.getFactory().getTraceEvent();
		//example.FirstTraceExample:intArrayMethod
		criteria.setClassName("FirstTraceExample");
		criteria.setMethodName("intArrayMethod");
		criteria.setEventType(EventType.EXIT);
		filterExt.setFilterCriteria(criteria);
		HostPort hostPort = new HostPort(ConnectionTestUtils.DEFAULT_HOST_1,ConnectionTestUtils.DEFAULT_PORT_1_INT);
		try {
			/**
			 *  C O N N E C T
			 */
			IConnection c = DefaultConnectionList.getSingleton().connect(null, hostPort, commandArray, filterExt);
			assertNotNull("Is the test program started?  Didn't get a connection. [" + DefaultFactory.getFactory().getMessages().getTestSetupInstructions() + "]");
			assertEquals("Didn't not connect successfully. [" + DefaultFactory.getFactory().getMessages().getTestSetupInstructions() + "]", true, c.isConnected());
			/**
			 * Pause to collect some trace events from the Agent running on 9123.
			 */
			Thread.sleep(2000/*ConnectionTestUtils.EVENT_COLLECTION_TIME_MS */);			
			
			long traceEventCount = c.getTraceEvents().size();
			assertTrue("Trace count should be > 0", (traceEventCount>0));
			//System.out.println("Found [" + traceEventCount + "] events.");
			
			int countOfMatches = 0;
			int countOfMisses = 0;
			for ( ITraceEvent te : c.getTraceEvents() ) {
				if (te.getEventType().equals(EventType.EXIT)
						&& "FirstTraceExample".equals(te.getClassName())
						&& "intArrayMethod".equals(te.getMethodName()) )
					countOfMatches++;
				else
					countOfMisses++;
			}
			assertTrue("Whoops.  Found at least one event that didn't match the filter",countOfMisses==0);
			assertTrue("Whoops.  Perhaps too much got filtered out.  Found zero events matching the filter",countOfMatches>0);
			
			c.disconnect();
			
		} catch (ConnectionTimeout e) {
			e.printStackTrace();
			fail("Received a connection timeout.  Is the test program example.FirstTraceExample running? [" + e.getMessage() + "]");
		} catch (ConnectionException e) {
			e.printStackTrace();
			fail("Unknown connection error [" + e.getMessage() + "]");
		}
	
	
	}

}