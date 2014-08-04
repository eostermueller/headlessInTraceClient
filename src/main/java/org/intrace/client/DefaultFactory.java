package org.intrace.client;


import org.intrace.client.connection.DefaultCallback;
import org.intrace.client.connection.DefaultConnection;
import org.intrace.client.connection.DefaultTraceEventWriterImpl;
import org.intrace.client.connection.IConnection;
import org.intrace.client.connection.IConnectionStateCallback;
import org.intrace.client.model.DefaultTraceEventParser;
import org.intrace.client.model.ITraceEvent;
import org.intrace.client.model.ITraceEventParser;
import org.intrace.client.model.DefaultTraceEvent;
import org.intrace.client.request.DefaultRequest;
import org.intrace.client.request.DefaultRequestSeparator;
import org.intrace.client.request.IRequest;
import org.intrace.client.request.IRequestSeparator;
import org.intrace.jdbc.HsqldbProvider;
import org.intrace.jdbc.IJdbcProvider;

/**
 * This is my low-complexity approach to dependency injection.
 * This class decides on default implementations.
 * If, application wide, you want to override a default, you do this:
 * 1) At application startup time, instantiate your own implementation of IFactory (probably extending DefaultFactory).
 * 2) Pass an instance of your new factory into DefaultFactory.setFactory(), as detailed here:
   <PRE>
   
   IFactory myCustomFactory = new DefaultFactory() {
		@Override
		public ITraceEventParser getEventParser() {
			return new MyCustomTraceEventParser();
		}
   };
   DefaultFactory.setFactory(myCustomFactory);
   </PRE>
 * Then, subsequent calls to DefaultFactory.getFactory() will return myCustomFactory.
 * USE CASE:  the headless InTrace client relies on the text of an event to be formatted in a very specific way.
 * Let's say that multiple text formats must be supported.  If so, just create the necessary implementation of ITraceEventParser
 * and configure the right one (as described above) at startup.
 * @author erikostermueller
 *
 */
public class DefaultFactory implements IFactory {
	private static IFactory INSTANCE = null;
	private HumanReadableMessages messages = new AmericanEnglishMessages();
	@Override
	public HumanReadableMessages getMessages() {
		return messages;
	}

	@Override
	public void setMessages(HumanReadableMessages val) {
		messages = val;
	}
	
	private Config config = new DefaultConfig();
	@Override
	public Config getConfig() {
		return config;
	}
	@Override
	public void setConfig(Config val) {
		config = val;
	}
	/**
	 * This method was designed to be called just one time at application startup.
	 * This could be done in a single static block of a single class, that perhaps
	 * grabbed the name of the Factory impl from a -D parameter or similar.
	 * If this method detects that its already been called, it throws a runtime exception.
	 * I did that so the developer will know whether the "worst case" has happened (ie, when multiple code locations are trying to set the factory).
	 * @param myFactory
	 */
	public static void setFactory(IFactory myFactory) {
		if (INSTANCE==null) {
			INSTANCE = myFactory;
		} else {
			throw new RuntimeException("Factory has already been set to value [" + INSTANCE.getClass().getName() + "]");
		}
	}
	/**
	 * 
	 * 
	 * @return
	 */
	public static IFactory getFactory() {
		if( INSTANCE==null) {
			return new DefaultFactory();
		}
		return INSTANCE;
	}
	

	@Override
	public ITraceEventParser getEventParser() {
		return new DefaultTraceEventParser();
	}

	@Override
	public ITraceWriter getTraceWriter() {
		return new DefaultTraceEventWriterImpl();
	}

	@Override
	public IConnection getDormantConnection() {
		DefaultConnection d = new DefaultConnection();
		System.out.println(" $$$  in factory #################################");
		System.out.println(d.toString());
		return new DefaultConnection();
	}

	@Override
	public ITraceEvent getTraceEvent() {
		return new DefaultTraceEvent();
	}

	@Override
	public IRequestSeparator getRequestSeparator() {
		return new DefaultRequestSeparator();
	}

	@Override
	public IRequest getRequest() {
		return new DefaultRequest();
	}
	@Override
	public IConnectionStateCallback getCallback() {
		return new DefaultCallback();
	}
	@Override 
	public IJdbcProvider getJdbcProvider() {
		return new HsqldbProvider();
	}
//	public static void log(String msg)  {
//		FileWriter fw = null;
//		try {
//			File output = new File("/tmp/InTrace.txt");
//			fw = new FileWriter(output,true);
//			fw.append(msg);
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//		} finally {
//			if (fw!=null)
//				try {
//					fw.flush();
//					fw.close();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//		}
//		
//		
//	}
	

}
