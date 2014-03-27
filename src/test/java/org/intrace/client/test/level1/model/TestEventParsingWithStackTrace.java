package org.intrace.client.test.level1.model;

import static org.junit.Assert.*;

import org.intrace.client.IntraceException;
import org.intrace.client.model.DefaultTraceEventParser;
import org.intrace.client.model.ITraceEvent;
import org.intrace.client.model.ITraceEventParser;
import org.intrace.client.model.ITraceEvent.EventType;
import org.intrace.client.model.DefaultTraceEvent;
import org.junit.Ignore;
import org.junit.Test;

/**  This tester will attempt to parse most of these InTrace agent events:
 * Each line consists of a text event sent back from the InTrace agent.
 * <PRE>
[00:02:32.255]:[17]:org.hsqldb.jdbc.jdbcConnection:getAutoCommit: }~org.hsqldb.jdbc.jdbcConnection.getAutoCommit(Unknown Source),org.apache.commons.dbcp.DelegatingConnection.getAutoCommit(DelegatingConnection.java:337),org.apache.commons.dbcp.PoolableConnectionFactory.passivateObject(PoolableConnectionFactory.java:688),org.apache.commons.pool.impl.GenericObjectPool.addObjectToPool(GenericObjectPool.java:1379),org.apache.commons.pool.impl.GenericObjectPool.returnObject(GenericObjectPool.java:1342),org.apache.commons.dbcp.PoolableConnection.close(PoolableConnection.java:90),org.apache.commons.dbcp.PoolingDataSource$PoolGuardConnectionWrapper.close(PoolingDataSource.java:191),org.springframework.jdbc.datasource.DataSourceUtils.doReleaseConnection(DataSourceUtils.java:333),org.springframework.jdbc.datasource.DataSourceUtils.releaseConnection(DataSourceUtils.java:294),org.springframework.jdbc.datasource.DataSourceTransactionManager.doCleanupAfterCompletion(DataSourceTransactionManager.java:324),org.springframework.transaction.support.AbstractPlatformTransactionManager.cleanupAfterCompletion(AbstractPlatformTransactionManager.java:1011),org.springframework.transaction.support.AbstractPlatformTransactionManager.processCommit(AbstractPlatformTransactionManager.java:804),org.springframework.transaction.support.AbstractPlatformTransactionManager.commit(AbstractPlatformTransactionManager.java:723),org.springframework.transaction.interceptor.TransactionAspectSupport.commitTransactionAfterReturning(TransactionAspectSupport.java:393),org.springframework.transaction.interceptor.TransactionInterceptor.invoke(TransactionInterceptor.java:120),org.springpringframework.jdbc.datasource.DataSourceTransactionManager.doCleanupAfterCompletion(DataSourceTransactionManager.java:324),org.springframework.transaction.support.AbstractPlatformTransactionManager.cleanupAfterCompletion(AbstractPlatformTransactionManager.java:1011),org.springframework.transaction.support.AbstractPlatformTransactionManager.processCommit(AbstractPlatformTransactionManager.java:804),org.springframework.transaction.support.AbstractPlatformTransactionManager.commit(AbstractPlatformTransactionManager.java:723),org.springframework.transaction.interceptor.TransactionAspectSupport.commitTransactionAfterReturning(TransactionAspectSupport.java:393),org.springframework.transaction.interceptor.TransactionInterceptor.invoke(TransactionInterceptor.java:120),org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:172),org.springframework.aop.framework.JdkDynamicAopProxy.invoke(JdkDynamicAopProxy.java:202),$Proxy6.save(Unknown Source),example.webapp.servlet.HelloWorld.doGet(HelloWorld.java:34),javax.servlet.http.HttpServlet.service(HttpServlet.java:668),javax.servlet.http.HttpServlet.service(HttpServlet.java:770),org.eclipse.jetty.servlet.ServletHolder.handle(ServletHolder.java:669),org.eclipse.jetty.servlet.ServletHandler.doHandle(ServletHandler.java:455),org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:137),org.eclipse.jetty.security.SecurityHandler.handle(SecurityHandler.java:560),org.eclipse.jetty.server.session.SessionHandler.doHandle(SessionHandler.java:231),org.eclipse.jetty.server.handler.ContextHandler.doHandle(ContextHandler.java:1072),org.eclipse.jetty.servlet.ServletHandler.doScope(ServletHandler.java:382),org.eclipse.jetty.server.session.SessionHandler.doScope(SessionHandler.java:193),org.eclipse.jetty.server.handler.ContextHandler.doScope(ContextHandler.java:1006),org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:135),org.eclipse.jetty.server.handler.HandlerWrapper.handle(HandlerWrapper.java:116),org.eclipse.jetty.server.Server.handle(Server.java:365),org.eclipse.jetty.server.AbstractHttpConnection.handleRequest(AbstractHttpConnection.java:485),org.eclipse.jetty.server.BlockingHttpConnection.handleRequest(BlockingHttpConnection.java:53),org.eclipse.jetty.server.AbstractHttpConnection.headerComplete(AbstractHttpConnection.java:926),org.eclipse.jetty.server.AbstractHttpConnection$RequestHandler.headerComplete(AbstractHttpConnection.java:988),org.eclipse.jetty.http.HttpParser.parseNext(HttpParser.java:635),org.eclipse.jetty.http.HttpParser.parseAvailable(HttpParser.java:235),org.eclipse.jetty.server.BlockingHttpConnection.handle(BlockingHttpConnection.java:72),org.eclipse.jetty.server.bio.SocketConnector$ConnectorEndPoint.run(SocketConnector.java:264),org.eclipse.jetty.util.thread.QueuedThreadPool.runJob(QueuedThreadPool.java:608),org.eclipse.jetty.util.thread.QueuedThreadPool$3.run(QueuedThreadPool.java:543)
 * </PRE>
 * @author erikostermueller
 *
 */
public class TestEventParsingWithStackTrace {
	private static final long EXPECTED_TIMESTAMP = 46090792L;/*"12:48:10.792"*/
	ITraceEventParser m_eventParser = new DefaultTraceEventParser();


	
	@Test public void canParseMinimalStackTraceElement() {
		String rawStackTraceElement = "$Proxy6.save(Unknown Source)";
		StackTraceElement ste = m_eventParser.createStackTraceElement(rawStackTraceElement);
		assertEquals("Expecting ","$Proxy6", ste.getClassName() );
		assertEquals("Couldn't find the source file name", DefaultTraceEventParser.UNKNOWN_SOURCE_STR, ste.getFileName());
		assertTrue("Couldn't parse the line number form a stack trace", ste.getLineNumber() < 0);
	}
	
	@Test public void canParseStackTraceElement() {
		String rawStackTraceElement = "org.eclipse.jetty.util.thread.QueuedThreadPool$3.run(QueuedThreadPool.java:543)";
		StackTraceElement ste = m_eventParser.createStackTraceElement(rawStackTraceElement);
		assertEquals("Expecting ","org.eclipse.jetty.util.thread.QueuedThreadPool$3", ste.getClassName() );
		assertEquals("Couldn't find method name parsed from stack trace element", "run", ste.getMethodName());
		assertEquals("Couldn't find the source file name", "QueuedThreadPool.java", ste.getFileName());
		assertEquals("Couldn't parse the line number form a stack trace", 543, ste.getLineNumber());
	}
	
	@Test public void canParseStackTraceElementUnkSource() {
		String rawStackTraceElement = "org.hsqldb.jdbc.jdbcConnection.getAutoCommit(Unknown Source)";
		StackTraceElement ste = m_eventParser.createStackTraceElement(rawStackTraceElement);
		assertEquals("Expecting to have parsed a particular class name","org.hsqldb.jdbc.jdbcConnection", ste.getClassName() );
		assertEquals("Couldn't find the source file name", DefaultTraceEventParser.UNKNOWN_SOURCE_STR, ste.getFileName());
		assertTrue("Couldn't parse the line number form a stack trace", ste.getLineNumber() < 0);
	}
	@Test
	public void canParseTraceEvent() throws IntraceException {
		String rawEventText = "[12:48:10.792]:[17]:org.hsqldb.jdbc.jdbcConnection:getAutoCommit: }~org.hsqldb.jdbc.jdbcConnection.getAutoCommit(Unknown Source),org.apache.commons.dbcp.DelegatingConnection.getAutoCommit(DelegatingConnection.java:337),org.apache.commons.dbcp.PoolableConnectionFactory.passivateObject(PoolableConnectionFactory.java:688),org.apache.commons.pool.impl.GenericObjectPool.addObjectToPool(GenericObjectPool.java:1379),org.apache.commons.pool.impl.GenericObjectPool.returnObject(GenericObjectPool.java:1342),org.apache.commons.dbcp.PoolableConnection.close(PoolableConnection.java:90),org.apache.commons.dbcp.PoolingDataSource$PoolGuardConnectionWrapper.close(PoolingDataSource.java:191),org.springframework.jdbc.datasource.DataSourceUtils.doReleaseConnection(DataSourceUtils.java:333),org.springframework.jdbc.datasource.DataSourceUtils.releaseConnection(DataSourceUtils.java:294),org.springframework.jdbc.datasource.DataSourceTransactionManager.doCleanupAfterCompletion(DataSourceTransactionManager.java:324),org.springframework.transaction.support.AbstractPlatformTransactionManager.cleanupAfterCompletion(AbstractPlatformTransactionManager.java:1011),org.springframework.transaction.support.AbstractPlatformTransactionManager.processCommit(AbstractPlatformTransactionManager.java:804),org.springframework.transaction.support.AbstractPlatformTransactionManager.commit(AbstractPlatformTransactionManager.java:723),org.springframework.transaction.interceptor.TransactionAspectSupport.commitTransactionAfterReturning(TransactionAspectSupport.java:393),org.springframework.transaction.interceptor.TransactionInterceptor.invoke(TransactionInterceptor.java:120),org.springpringframework.jdbc.datasource.DataSourceTransactionManager.doCleanupAfterCompletion(DataSourceTransactionManager.java:324),org.springframework.transaction.support.AbstractPlatformTransactionManager.cleanupAfterCompletion(AbstractPlatformTransactionManager.java:1011),org.springframework.transaction.support.AbstractPlatformTransactionManager.processCommit(AbstractPlatformTransactionManager.java:804),org.springframework.transaction.support.AbstractPlatformTransactionManager.commit(AbstractPlatformTransactionManager.java:723),org.springframework.transaction.interceptor.TransactionAspectSupport.commitTransactionAfterReturning(TransactionAspectSupport.java:393),org.springframework.transaction.interceptor.TransactionInterceptor.invoke(TransactionInterceptor.java:120),org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:172),org.springframework.aop.framework.JdkDynamicAopProxy.invoke(JdkDynamicAopProxy.java:202),$Proxy6.save(Unknown Source),example.webapp.servlet.HelloWorld.doGet(HelloWorld.java:34),javax.servlet.http.HttpServlet.service(HttpServlet.java:668),javax.servlet.http.HttpServlet.service(HttpServlet.java:770),org.eclipse.jetty.servlet.ServletHolder.handle(ServletHolder.java:669),org.eclipse.jetty.servlet.ServletHandler.doHandle(ServletHandler.java:455),org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:137),org.eclipse.jetty.security.SecurityHandler.handle(SecurityHandler.java:560),org.eclipse.jetty.server.session.SessionHandler.doHandle(SessionHandler.java:231),org.eclipse.jetty.server.handler.ContextHandler.doHandle(ContextHandler.java:1072),org.eclipse.jetty.servlet.ServletHandler.doScope(ServletHandler.java:382),org.eclipse.jetty.server.session.SessionHandler.doScope(SessionHandler.java:193),org.eclipse.jetty.server.handler.ContextHandler.doScope(ContextHandler.java:1006),org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:135),org.eclipse.jetty.server.handler.HandlerWrapper.handle(HandlerWrapper.java:116),org.eclipse.jetty.server.Server.handle(Server.java:365),org.eclipse.jetty.server.AbstractHttpConnection.handleRequest(AbstractHttpConnection.java:485),org.eclipse.jetty.server.BlockingHttpConnection.handleRequest(BlockingHttpConnection.java:53),org.eclipse.jetty.server.AbstractHttpConnection.headerComplete(AbstractHttpConnection.java:926),org.eclipse.jetty.server.AbstractHttpConnection$RequestHandler.headerComplete(AbstractHttpConnection.java:988),org.eclipse.jetty.http.HttpParser.parseNext(HttpParser.java:635),org.eclipse.jetty.http.HttpParser.parseAvailable(HttpParser.java:235),org.eclipse.jetty.server.BlockingHttpConnection.handle(BlockingHttpConnection.java:72),org.eclipse.jetty.server.bio.SocketConnector$ConnectorEndPoint.run(SocketConnector.java:264),org.eclipse.jetty.util.thread.QueuedThreadPool.runJob(QueuedThreadPool.java:608),org.eclipse.jetty.util.thread.QueuedThreadPool$3.run(QueuedThreadPool.java:543)";
		ITraceEvent expectedEvent = new DefaultTraceEvent(
				rawEventText,						//raw event text
				"org.hsqldb.jdbc",					//package
				"jdbcConnection",					//class
				EXPECTED_TIMESTAMP,						//time in millis
				"getAutoCommit",					//method
				EventType.EXIT,					//trace event type
				null,								//arg or return value
				false,								//isConstructor
				"17",								//threadId
				-1,								//Source Code line number (default).
				null);							//Name of the argument
		
		ITraceEvent actualEvent = m_eventParser.createEvent(rawEventText,0);
		validateSomeEventValues(expectedEvent, actualEvent);
		
		assertEquals("Couldn't find the right number of stack trace elements", m_expectedStackTraceElements.length, actualEvent.getStackTrace().length);
		validateStackTrace( actualEvent, m_expectedStackTraceElements);
		
	}
	private void validateStackTrace(ITraceEvent actualEvent,
			String[] m_stackTraceElements2) {
		
		StackTraceElement[] actualStackTrace = actualEvent.getStackTrace();
		assertTrue("Found zero stack trace elements...smthg wrong here",actualStackTrace.length>0);
		
		assertEquals("Did not find right count of stack trace elements",m_expectedStackTraceElements.length, actualStackTrace.length );
		int count = 0;
		for(String expectedStackTraceElement : m_expectedStackTraceElements) {
			assertEquals("Missing expected element in stack trace index [" + count + "]", 
					expectedStackTraceElement,
					actualStackTrace[count++].toString() );
					
		}
	}
	private void validateSomeEventValues(ITraceEvent expectedEvent, ITraceEvent actualEvent) {
		assertEquals("Could not find package name", expectedEvent.getPackageName().toLowerCase(), actualEvent.getPackageName().toLowerCase());
		assertEquals("Could not find class name", expectedEvent.getClassName().toLowerCase(), actualEvent.getClassName().toLowerCase());
		//assertEquals("Could not find agentTime", expectedEvent.getAgentTimeMillis(), actualEvent.getAgentTimeMillis());
		assertEquals("Could not find method name", expectedEvent.getMethodName().toLowerCase(), actualEvent.getMethodName().toLowerCase());
		assertEquals("Could not find event type", expectedEvent.getEventType(), actualEvent.getEventType());
		assertEquals("Could not find arg or return value", expectedEvent.getValue(), actualEvent.getValue());
		assertEquals("Could not find constructor indicator", expectedEvent.isConstructor(), actualEvent.isConstructor());
		assertEquals("Could not find thread id", expectedEvent.getThreadId(), actualEvent.getThreadId());
		assertEquals("Could not find Argument name", expectedEvent.getArgName(), actualEvent.getArgName());
	}
	private String[] m_expectedStackTraceElements = {
			"org.hsqldb.jdbc.jdbcConnection.getAutoCommit(Unknown Source)",
			"org.apache.commons.dbcp.DelegatingConnection.getAutoCommit(DelegatingConnection.java:337)",
			"org.apache.commons.dbcp.PoolableConnectionFactory.passivateObject(PoolableConnectionFactory.java:688)",
			"org.apache.commons.pool.impl.GenericObjectPool.addObjectToPool(GenericObjectPool.java:1379)",
			"org.apache.commons.pool.impl.GenericObjectPool.returnObject(GenericObjectPool.java:1342)",
			"org.apache.commons.dbcp.PoolableConnection.close(PoolableConnection.java:90)",
			"org.apache.commons.dbcp.PoolingDataSource$PoolGuardConnectionWrapper.close(PoolingDataSource.java:191)",
			"org.springframework.jdbc.datasource.DataSourceUtils.doReleaseConnection(DataSourceUtils.java:333)",
			"org.springframework.jdbc.datasource.DataSourceUtils.releaseConnection(DataSourceUtils.java:294)",
			"org.springframework.jdbc.datasource.DataSourceTransactionManager.doCleanupAfterCompletion(DataSourceTransactionManager.java:324)",
			"org.springframework.transaction.support.AbstractPlatformTransactionManager.cleanupAfterCompletion(AbstractPlatformTransactionManager.java:1011)",
			"org.springframework.transaction.support.AbstractPlatformTransactionManager.processCommit(AbstractPlatformTransactionManager.java:804)",
			"org.springframework.transaction.support.AbstractPlatformTransactionManager.commit(AbstractPlatformTransactionManager.java:723)",
			"org.springframework.transaction.interceptor.TransactionAspectSupport.commitTransactionAfterReturning(TransactionAspectSupport.java:393)",
			"org.springframework.transaction.interceptor.TransactionInterceptor.invoke(TransactionInterceptor.java:120)",
			"org.springpringframework.jdbc.datasource.DataSourceTransactionManager.doCleanupAfterCompletion(DataSourceTransactionManager.java:324)",
			"org.springframework.transaction.support.AbstractPlatformTransactionManager.cleanupAfterCompletion(AbstractPlatformTransactionManager.java:1011)",
			"org.springframework.transaction.support.AbstractPlatformTransactionManager.processCommit(AbstractPlatformTransactionManager.java:804)",
			"org.springframework.transaction.support.AbstractPlatformTransactionManager.commit(AbstractPlatformTransactionManager.java:723)",
			"org.springframework.transaction.interceptor.TransactionAspectSupport.commitTransactionAfterReturning(TransactionAspectSupport.java:393)",
			"org.springframework.transaction.interceptor.TransactionInterceptor.invoke(TransactionInterceptor.java:120)",
			"org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:172)",
			"org.springframework.aop.framework.JdkDynamicAopProxy.invoke(JdkDynamicAopProxy.java:202)",
			"$Proxy6.save(Unknown Source)",
			"example.webapp.servlet.HelloWorld.doGet(HelloWorld.java:34)",
			"javax.servlet.http.HttpServlet.service(HttpServlet.java:668)",
			"javax.servlet.http.HttpServlet.service(HttpServlet.java:770)",
			"org.eclipse.jetty.servlet.ServletHolder.handle(ServletHolder.java:669)",
			"org.eclipse.jetty.servlet.ServletHandler.doHandle(ServletHandler.java:455)",
			"org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:137)",
			"org.eclipse.jetty.security.SecurityHandler.handle(SecurityHandler.java:560)",
			"org.eclipse.jetty.server.session.SessionHandler.doHandle(SessionHandler.java:231)",
			"org.eclipse.jetty.server.handler.ContextHandler.doHandle(ContextHandler.java:1072)",
			"org.eclipse.jetty.servlet.ServletHandler.doScope(ServletHandler.java:382)",
			"org.eclipse.jetty.server.session.SessionHandler.doScope(SessionHandler.java:193)",
			"org.eclipse.jetty.server.handler.ContextHandler.doScope(ContextHandler.java:1006)",
			"org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:135)",
			"org.eclipse.jetty.server.handler.HandlerWrapper.handle(HandlerWrapper.java:116)",
			"org.eclipse.jetty.server.Server.handle(Server.java:365)",
			"org.eclipse.jetty.server.AbstractHttpConnection.handleRequest(AbstractHttpConnection.java:485)",
			"org.eclipse.jetty.server.BlockingHttpConnection.handleRequest(BlockingHttpConnection.java:53)",
			"org.eclipse.jetty.server.AbstractHttpConnection.headerComplete(AbstractHttpConnection.java:926)",
			"org.eclipse.jetty.server.AbstractHttpConnection$RequestHandler.headerComplete(AbstractHttpConnection.java:988)",
			"org.eclipse.jetty.http.HttpParser.parseNext(HttpParser.java:635)",
			"org.eclipse.jetty.http.HttpParser.parseAvailable(HttpParser.java:235)",
			"org.eclipse.jetty.server.BlockingHttpConnection.handle(BlockingHttpConnection.java:72)",
			"org.eclipse.jetty.server.bio.SocketConnector$ConnectorEndPoint.run(SocketConnector.java:264)",
			"org.eclipse.jetty.util.thread.QueuedThreadPool.runJob(QueuedThreadPool.java:608)",
			"org.eclipse.jetty.util.thread.QueuedThreadPool$3.run(QueuedThreadPool.java:543)"			
	};

}
