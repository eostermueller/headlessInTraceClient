package org.intrace.client.request;

import java.util.Queue;

import org.apache.log4j.Logger;
import org.intrace.client.DefaultFactory;
import org.intrace.client.ITraceWriter;
import org.intrace.client.connection.HostPort;
import org.intrace.client.connection.NetworkDataReceiverThread2;
import org.intrace.client.filter.ITraceFilter;
import org.intrace.client.filter.ITraceFilterExt;
import org.intrace.client.model.FixedLengthQueue;
import org.intrace.client.model.ITraceEvent;
import org.intrace.client.model.ITraceEventParser;
import org.junit.Before;

import ca.odell.glazedlists.EventList;

public class RequestWriter implements ITraceWriter, ICompletedRequestCallback {
    private static final Logger LOG = Logger.getLogger( RequestWriter.class.getName() );
	Queue<IRequestEvents> m_requestQueue = null;
	private ITraceEvent m_requestCompletionEvent = null;

	
	public Queue<IRequestEvents> getCompletedRequestQueue() {
		return m_requestQueue;
	}
	/**
	 * This class is a highly-configurable beast.
	 * Most of the configuration takes place in this ctor.
	 * @param numberOfRequestsToRetain
	 */
	public RequestWriter(int numberOfRequestsToRetain) {
		
		this.setRequestSeparator(DefaultFactory.getFactory().getRequestSeparator());
		//The requests, with their events, are stored in memory here.   no support for persistence yet.
		m_requestQueue = new FixedLengthQueue<IRequestEvents>(numberOfRequestsToRetain);
		
		//Indicates where the completed requests should be sent to.
		getRequestSeparator().setCompletedRequestCallback(this);
		
		setEventParser(DefaultFactory.getFactory().getEventParser());
		
		//For now, assume that everything matches...TODO: code a filter that takes into account InTrace-class-includes/excludes and InTrace-filter-includes/excludes
		setTraceFilter( new ITraceFilter(){
			@Override
			public boolean matches(String traceData) {
				return true;
			}});
		setTraceFilterExt( new ITraceFilterExt(){
			@Override
			public boolean matches(ITraceEvent traceData) {
				return true;
			}});
	}
	IRequestSeparator m_requestSeparator = null;
	private ITraceEventParser m_eventParser = null;
	private ITraceFilter m_traceFilter;
	private ITraceFilterExt m_traceFilterExt;;
	
	private ITraceEventParser getEventParser() {
		return m_eventParser;
	}
	private void setEventParser(ITraceEventParser eventParser) {
		this.m_eventParser = eventParser;
	}
	public IRequestSeparator getRequestSeparator() {
		return m_requestSeparator;
	}

	private void setRequestSeparator(IRequestSeparator requestSeparator) {
		this.m_requestSeparator = requestSeparator;
	}

	@Override
	public void writeTraceEvent(String traceLine, HostPort hostPort) {
		if (getTraceFilter().matches(traceLine)) {
			  try {
				  ITraceEvent te = getEventParser().createEvent(traceLine,0);
				  te.setAgentHostName(hostPort.hostNameOrIpAddress);
				  te.setAgentPort((short) hostPort.port);
				  if (getTraceFilterExt().matches(te)) {
					  LOG.debug("From raw data [" + traceLine + "] adding parsed event to request separator [" + te.toString() + "]");
					  getRequestSeparator().add(te);
				  }
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	@Override
	public String getName() {
		throw new RuntimeException("Whoops...thought no on was using this.");	
	}

	@Override
	public void setName(String name) {
		throw new RuntimeException("Whoops...thought no on was using this.");
	}

	@Override
	public void setTraceEvents(EventList<ITraceEvent> traceEvents) {
		// TODO Auto-generated method stub
		Exception e = new Exception();
		e.printStackTrace();
		//throw new RuntimeException("Whoops...thought no on was using this.");
	}

	@Override
	public EventList<ITraceEvent> getTraceEvents() {
		Exception e = new Exception();
		e.printStackTrace();
		//throw new RuntimeException("Whoops...thought no on was using this.");
		return null;
	}

	@Override
	public void setTraceFilter(ITraceFilter filter) {
		m_traceFilter = filter;
	}

	@Override
	public ITraceFilter getTraceFilter() {
		return m_traceFilter;
	}

	@Override
	public void setTraceFilterExt(ITraceFilterExt filter) {
		m_traceFilterExt = filter;

	}

	@Override
	public ITraceFilterExt getTraceFilterExt() {
		return m_traceFilterExt;
	}

	@Override
	public void requestCompleted(IRequestEvents events) {
		getCompletedRequestQueue().add(events);
	}

}
