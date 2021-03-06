package org.headlessintrace.client.test.level2.connection;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.headlessintrace.client.DefaultFactory;
import org.headlessintrace.client.connection.ConnectionException;
import org.headlessintrace.client.connection.ConnectionTimeout;
import org.headlessintrace.client.connection.DefaultCallback;
import org.headlessintrace.client.connection.DefaultConnectionList;
import org.headlessintrace.client.connection.HostPort;
import org.headlessintrace.client.connection.IConnection;
import org.headlessintrace.client.connection.command.ClassInstrumentationCommand;
import org.headlessintrace.client.connection.command.IAgentCommand;
import org.headlessintrace.client.model.ITraceEvent;
import org.headlessintrace.client.request.BadCompletedRequestListener;
import org.headlessintrace.client.test.level2.connection.lowLevel.ConnectionTestUtils;
import org.junit.Test;

import ca.odell.glazedlists.EventList;

public class JdbcClassTest {

	private CountDownLatch latch = null;
	private boolean m_waitForInstrumentation = false;
	
	@Test
	public void canDetectImplementor() {
		
		ClassInstrumentationCommand cic = new ClassInstrumentationCommand();
		//cic.setIncludeClassRegEx("org.hsqldb.jdbc.jdbcStatement");
		cic.setIncludeClassRegEx(ConnectionTestUtils.TEST_CLASS_TO_INSTRUMENT_1);
		IAgentCommand commandArray[] = { cic };
		HostPort hostPort = new HostPort(ConnectionTestUtils.DEFAULT_HOST_1,ConnectionTestUtils.DEFAULT_PORT_1_INT);
		
		//Test will fail unless we wait for instrumentation to complete.
		IConnection c = null;
		try {
			latch = new CountDownLatch(1);
			DefaultCallback callback = new DefaultCallback() {
				@Override
				public void setProgress(Map<String, String> progress) {
					m_waitForInstrumentation = true;
					String result = progress.get("NUM_PROGRESS_DONE");
					System.out.print("."); // kinda a live status update, filling screen with dots.
					if (result!=null && result.equals("true"))
						latch.countDown();
				}
			};
			/**
			 *  C O N N E C T
			 */
			c = DefaultConnectionList.getSingleton().connect(callback, hostPort, commandArray);
			//c = DefaultConnectionList.getSingleton().connect(null, hostPort, null); // may 15 2014
			System.out.println("x1 ####@after connect");
			System.out.println("x2 Connection [" + c.toString() + "]\n");
			
			assertNotNull("Is the test program started?  Didn't get a connection. [" + DefaultFactory.getFactory().getMessages().getTestSetupInstructions() + "]");
			assertEquals("Didn't not connect successfully. [" + DefaultFactory.getFactory().getMessages().getTestSetupInstructions() + "]", true, c.isConnected());
			
			//Thread.sleep(120000);
			if (m_waitForInstrumentation) {
				System.out.println("x4 Waiting for agent to finish instrumentation");
				latch.await();
			}
			System.out.println("x5 ####%instrumentation finished Connection [" + c.toString() + "]\n");
			/**
			 *  V A L I D A T E
			 *  T R A C E
			 *  E V E N T S
			 */
			//dispEvents(c.getTraceEvents());
			Thread.sleep(5000);//collect some events
			int traceEventCount = c.getTraceEvents().size();
			System.out.println("Found [" + traceEventCount + "] events.");
			//assertEquals("Didn't find exepected number of java.sql.PreparedStatement events", 71, traceEventCount);
			//ITraceEvent event = c.getTraceEvents().get(0);
			
			c.disconnect();

		} catch (ConnectionTimeout e) {
			e.printStackTrace();
			fail("Received a connection timeout.  Is the test program example.FirstTraceExample running? [" + e.getMessage() + "]");
		} catch (ConnectionException e) {
			e.printStackTrace();
			fail("Unknown connection error [" + e.getMessage() + "]");
		} catch (BadCompletedRequestListener e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} finally {
			latch.countDown();
			if (c!=null) {
				c.disconnect();
				System.out.println(" Connection list [" + DefaultConnectionList.getSingleton().size() + "]");
				System.out.println("@@ 1 @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
				System.out.println(" Connection [" + c.toString() + "]\n");
				System.out.println("** 2 *********************************************");
			}
		}
		
	
	}
	private void dispEvents(EventList<ITraceEvent> traceEvents) {
		for( ITraceEvent t : traceEvents) {
			System.out.println(" Event [" + t.getPackageAndClass()  + "#" + t.getMethodName() + "] type [" + t.getEventType() + "]");
		}
		
	}

}
