package org.headlessintrace.client.connection;


import org.headlessintrace.client.DefaultFactory;


import org.headlessintrace.client.ITraceWriter;
import org.headlessintrace.client.IntraceException;
import org.headlessintrace.client.model.ITraceEvent;
import org.headlessintrace.client.model.ITraceEventParser;
import org.headlessintrace.client.model.ITraceEvent.EventType;

import ca.odell.glazedlists.EventList;

import org.headlessintrace.client.filter.ITraceFilterExt;
import org.headlessintrace.client.filter.ITraceFilter;
//import org.headlessintrace.client.filter.IncludeThisEventFilterExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultTraceEventWriterImpl implements ITraceWriter {
	private static final Logger LOG = LoggerFactory.getLogger(DefaultTraceEventWriterImpl.class);
	ITraceEventParser m_eventParser = null;
	public DefaultTraceEventWriterImpl() {
		m_eventParser = DefaultFactory.getFactory().getEventParser();
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
			}

			});
	}
	private ITraceFilter m_traceFilter = null;
	private ITraceFilterExt m_traceFilterExt = null;
	  public ITraceFilterExt getTraceFilterExt() {
		return m_traceFilterExt;
	}
	public void setTraceFilterExt(ITraceFilterExt traceFilterExt) {
		this.m_traceFilterExt = traceFilterExt;
	}
	private EventList<ITraceEvent> m_traceEvents = null;
	private String m_name;
	  public void setTraceEvents(EventList<ITraceEvent> traceEvents) {
		  this.m_traceEvents = traceEvents;
	  }
	  public EventList<ITraceEvent> getTraceEvents() {
		  if (LOG.isDebugEnabled()) LOG.debug("@#%332 DefaultTraceEventWriterImpl traceEvents [" + m_traceEvents.hashCode() + "]");
		  return m_traceEvents;
	  }

	public void writeTraceEvent(String traceLine, HostPort hostPort) {
		//traceLine = this.getName() + "-" + traceLine;
		if (getTraceFilter().matches(traceLine)) {
			  try {
				  //BeanTraceEventImpl te = TraceFactory.createTraceEvent(traceLine);
				  getTraceEvents().getReadWriteLock().writeLock().lock();
				  ITraceEvent te = m_eventParser.createEvent(traceLine,0);
				  if (te.getEventType()==EventType.DEBUG)
					  System.out.println("AGENT DEBUG: " + te.getRawEventData() );
				  else {
					  te.setAgentHostName(hostPort.hostNameOrIpAddress);
					  te.setAgentPort((short) hostPort.port);
					  if (getTraceFilterExt().matches(te)) {
						  getTraceEvents().add(te);
					  }
					  
				  }
			} catch (IntraceException e) {
				e.printStackTrace();
			} finally {
				getTraceEvents().getReadWriteLock().writeLock().unlock();
			}
		}

	}
	public ITraceFilter getTraceFilter() {
		return m_traceFilter;
	}
	public void setTraceFilter(ITraceFilter traceFilter) {
		this.m_traceFilter = traceFilter;
	}
	public String getName() {
		return m_name;
	}
	/**
	 * This is the window name.
	 */
	public void setName(String name) {
		m_name = name;
	}

}
