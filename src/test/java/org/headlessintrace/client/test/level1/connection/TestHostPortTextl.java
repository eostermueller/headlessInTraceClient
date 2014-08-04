package org.headlessintrace.client.test.level1.connection;

import org.headlessintrace.client.connection.HostPort;

import static org.junit.Assert.*;

import java.util.List;

import junit.framework.Assert;

import org.headlessintrace.client.connection.HostPort;
import org.junit.Test;


/**
 * @author e0018740
 *
 */
public class TestHostPortTextl {
	private static String HOST_1 = "localhost";
	private static int PORT_1_INT = 9123;
	private static String PORT_1_STR = "9123";

	private static String HOST_2 = "yourhost";
	private static int PORT_2_INT = 9123;
	private static String PORT_2_STR = "9123";

	@Test 
	public void canDistinguishIpAddressFromHostName() {

		assertFalse("Whoops.  just identifed 'foo' as an ip address", HostPort.isIpv4("foo"));
		
		assertFalse("Whoops.  just identifed '...' as an ip address", HostPort.isIpv4("..."));

		assertFalse("Whoops.  just identifed 'f...' as an ip address", HostPort.isIpv4("f..."));

		assertFalse("Whoops.  just identifed '0...' as an ip address", HostPort.isIpv4("0..."));

		assertFalse("Whoops.  just identifed '0.0..' as an ip address", HostPort.isIpv4("0.0.."));

		assertFalse("Whoops.  just identifed '0.0.0.' as an ip address", HostPort.isIpv4("0.0.0."));

		assertTrue("Whoops.  unable to identify '0.0.0.0' as an ip address", HostPort.isIpv4("0.0.0.0"));
		
		assertTrue("Whoops.  just identifed '10.10.10.10' as an ip address", HostPort.isIpv4("10.10.10.10"));
	}
	@Test
	public void parseSimpleSingleHostAndPort() {
		HostPort hostPort = new HostPort(HOST_1+":"+PORT_1_STR);
		Assert.assertEquals("could not find simple host name", HOST_1, hostPort.hostNameOrIpAddress);
		Assert.assertEquals("could not find simple port number", PORT_1_INT, hostPort.port);
	}
	@Test
	public void parseSingleHostAndPortList() {
		List<HostPort> hostsAndPorts = HostPort.parseList(HOST_1+":"+PORT_1_STR);
		Assert.assertEquals("provided a single host:port for input, but list size does not reflect that.", 1,hostsAndPorts.size());
		HostPort hostPort = hostsAndPorts.get(0);
		Assert.assertEquals("could not find simple host name in list", HOST_1, hostPort.hostNameOrIpAddress);
		Assert.assertEquals("could not find simple port number in list", PORT_1_INT, hostPort.port);
	}
	@Test
	public void parseCommaDelimetedHostsAndPorts() {
		List<HostPort> hostsAndPorts = HostPort.parseList("localhost:9123, yourhost:9123");
		Assert.assertEquals("provided a single host:port for input, but list size does not reflect that.", 2,hostsAndPorts.size());
		Assert.assertEquals("wrong list size.  provided a comma delimieted list of two host:port for input, but list size does not reflect that.", 2,hostsAndPorts.size());

		HostPort hostPort = hostsAndPorts.get(0);
		Assert.assertEquals("could not find first host name", HOST_1, hostPort.hostNameOrIpAddress);
		Assert.assertEquals("could not find first port number", PORT_1_INT, hostPort.port);

		HostPort hostPort2 = hostsAndPorts.get(1);
		Assert.assertEquals("could not find second host name", HOST_2, hostPort2.hostNameOrIpAddress);
		Assert.assertEquals("could not find second port number", PORT_2_INT, hostPort2.port);
	
	}

}
