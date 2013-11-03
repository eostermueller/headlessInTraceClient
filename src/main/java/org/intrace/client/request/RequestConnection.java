package org.intrace.client.request;

import java.util.Arrays;

import org.intrace.client.ITraceWriter;
import org.intrace.client.connection.ConnectionException;
import org.intrace.client.connection.DefaultCallback;
import org.intrace.client.connection.DefaultConnection;
import org.intrace.client.connection.ConnectionTimeout;
import org.intrace.client.connection.HostPort;
import org.intrace.client.connection.IConnection;
import org.intrace.client.connection.IConnectionStateCallback;
import org.intrace.client.connection.NetworkDataReceiverThread2;
import org.intrace.client.connection.command.ClassInstrumentationCommand;
import org.intrace.client.connection.command.IAgentCommand;
import org.intrace.client.filter.IncludeAnyOfTheseEventsFilterExt;
import org.intrace.client.model.ITraceEvent;
import org.intrace.client.test.level2.connection.lowLevel.ConnectionTestUtils;

import ca.odell.glazedlists.EventList;

/**
 * This is a layer written on top of "Connection" that
 * sorts the incoming trace events by thread.
 * Once an "exit" for the "thread completion method" event is received, the events for that particular thread are considered
 * complete and the CompletedListenerRequest fires its "requestCompleted" method.
 * This class only provides "request" support an inTrace agent in a single JVM -- rephrased, 
 * this class provides no support for "request" behavior with events from multiple inTrace agent connections.
 * @author erikostermueller
 *
 */
public class RequestConnection implements IConnection {
	private IConnection m_connection = null;
	private ITraceEvent m_requestCompletionEvent = null; 
	private IAgentCommand m_commandArray[] = null;
	public IAgentCommand[] getCommandArray() {
		return m_commandArray;
	}
	public void setCommandArray(IAgentCommand[] commandArray) {
		this.m_commandArray = commandArray;
	}
	public RequestConnection(int numRequestsToStore) {
		m_connection = new DefaultConnection();
		setTraceWriter( new RequestWriter(numRequestsToStore));

	}
	public boolean connect(String host, int port) throws ConnectionTimeout,
			ConnectionException, BadCompletedRequestListener {
		return this.connect(host, port,null);
	}
	public boolean connect(String host, int port, IAgentCommand[] startupCommandAry)
			throws ConnectionTimeout, ConnectionException, BadCompletedRequestListener {
		//m_commandArray = startupCommandAry;
		setCommandArray(startupCommandAry);
		enhanceCommandArray(startupCommandAry);
		return m_connection.connect(host, port, getCommandArray());
	}
	/**
	 * the headless inTrace client aims to allow the user to have method-level granularity,
	 * when deciding what events they want to see.
	 * This makes things complicated, because the InTrace server agent only supports class granularity.
	 * This method makes it easy for the consumer to place there preferences in one place 
	 * to specify their instrumentation preferences.
	 * NOTE:  currently there is no way for the consumer of this API to distinguish
	 * between two overloaded methods.  The InTrace server agent doesn't provide enough information.
	 * @param startupCommandAry
	 * @throws BadCompletedRequestListener
	 */
	private void enhanceCommandArray(IAgentCommand[] startupCommandAry) throws BadCompletedRequestListener {
		if (m_requestCompletionEvent.validate()==false) {
		   throw new BadCompletedRequestListener(m_requestCompletionEvent);
		}
		if (getTraceWriter().getTraceFilterExt() instanceof IncludeAnyOfTheseEventsFilterExt) {
			IncludeAnyOfTheseEventsFilterExt myFilter =  (IncludeAnyOfTheseEventsFilterExt) getTraceWriter().getTraceFilterExt();
			ClassInstrumentationCommand cic = new ClassInstrumentationCommand();
			cic.setIncludeClassRegEx(myFilter.getDelimitedListOfAllClasses() );
			
			if ( getCommandArray() == null) {
				this.setCommandArray(new IAgentCommand[]{});
			}
			//Expand by one 
			IAgentCommand[] tmp = Arrays.copyOf(getCommandArray(), getCommandArray().length+1);
			tmp[tmp.length-1] = cic;
			this.setCommandArray(tmp);
			
		}
	}
	public void disconnect() {
		m_connection.disconnect();
	}
	public void setCompletedRequestCallback(ICompletedRequestCallback callback) {
		RequestWriter requestWriter = (RequestWriter) m_connection.getTraceWriter();
		requestWriter.getRequestSeparator().setCompletedRequestCallback(callback);
	}
	/**
	 * The request api will consider a thread is completed processing when a particular
	 * method fires an exit event.  Which method?  The one indicated on the parameters of this method.
	 * As you'd expect, this needs to be called before events start firing.
	 * This the inTrace client will not be running in the same JVM as the system being monitored,
	 * it would not be possible to type these parameters with java.lang.Class and java.reflect.Method.
	 * @param string
	 * @param string2
	 */
	public void setRequestCompletionEvent(ITraceEvent requestCompletionEvent) {
		m_requestCompletionEvent = requestCompletionEvent;
		RequestWriter requestWriter = (RequestWriter) m_connection.getTraceWriter();
		requestWriter.getRequestSeparator().setRequestCompletionEvent(m_requestCompletionEvent);
		
		//m_threadCompletionMethodName = threadCompletionMethodName;
	}
	@Override
	public EventList<ITraceEvent> getTraceEvents() {
		return m_connection.getTraceEvents();
	}
	@Override
	public ITraceWriter getTraceWriter() {
		return m_connection.getTraceWriter();
	}
	@Override
	public void setTraceWriter(ITraceWriter traceWriter) {
		m_connection.setTraceWriter(traceWriter);
		
	}
	public IConnection getConnection() {
		return m_connection;
	}
	public boolean isConnected() {
		return getConnection().isConnected();
	}
	@Override
	public int removeConnectionStatusCallback(IConnectionStateCallback cb) {
		return getConnection().removeConnectionStatusCallback(cb);
	}
	@Override
	public boolean connect(HostPort hostPort, IAgentCommand[] startupCommandAry)
			throws ConnectionTimeout, ConnectionException {
		return getConnection().connect(hostPort, startupCommandAry);
	}
	@Override
	public void executeStartupCommands() {
		getConnection().executeStartupCommands();
	}
	@Override
	public HostPort getHostPort() {
		return getConnection().getHostPort();
	}
	@Override
	public NetworkDataReceiverThread2 getNetworkTraceThread2() {
		return getConnection().getNetworkTraceThread2();
	}
	public int getConnCallbackSize() {
		//return ((DefaultConnection) getConnection()).getConnectionDetail().getConnCallbacks().size();
		throw new RuntimeException("Not implemented");
	}
	public void addCallback(IConnectionStateCallback callback) {
		getConnection().addCallback(callback);
	}
	public void setHostPort(HostPort hostPort) {
		getConnection().setHostPort(hostPort);
	}
	public IConnectionStateCallback getMasterCallback() {
		return getConnection().getMasterCallback();
	}

}
