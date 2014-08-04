package org.headlessintrace.client.connection;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.headlessintrace.client.DefaultFactory;

/**
 * This class is used as a simple value object and as place for text formatting utilities,
 * that deal with whitespace and delimeter processing.
 * @author e0018740
 *
 */
public class HostPort {

	public HostPort(String hostName, String port) {
		this(hostName,Integer.parseInt(port));
	}
	
	/**
	 * Return true of the string is an IP address instead of a hostname.
	 * No, this is not a perfect algorithm, but is is close enough.
	 * If no number
	 * @param hostName
	 * @return
	 */
	public static boolean isIpv4(String hostName) {
		int periodCount = 0;
		int numberCount = 0;
		int lowerCount = 0;
		int upperCount = 0;
		int otherCount = 0;
		
		for(int i = 0; i < hostName.length(); i++ ) {
			char myChar = hostName.charAt(i);
			if (myChar=='.') {
				periodCount++;
			} else if (myChar >= '0' && myChar <= '9') {
				numberCount++;
			} else if (myChar >= 'a' & myChar <= 'z') {
				lowerCount++;
			} else if (myChar >= 'A' & myChar <= 'Z') {
				upperCount++;
			} else {
				otherCount++;
			}
		}
		
		if (periodCount==3 && numberCount >= 4 && lowerCount==0 && upperCount==0 && otherCount==0) {
			return true;
		} else {
			return false;
		}
	}
	public HostPort(String hostNameOrIpAddress, int port) {

		this.setHostNameOrIp(hostNameOrIpAddress);
		this.port = port;
	}
	public String toString3() {
		StringBuilder sb = new StringBuilder();
		sb.append("hostNameOrIpAddress: [" + hostNameOrIpAddress + "]");
		sb.append(" ip: [" + ip + "]");
		sb.append(" port: [" + port + "]");
		return sb.toString();
	}
	public boolean equals(HostPort criteria) {
		boolean ynRC = false;
		if (
				(criteria.hostNameOrIpAddress.equals(this.ip)) ||
				(criteria.hostNameOrIpAddress.equals(this.hostNameOrIpAddress)) ||
				(criteria.ip.equals(this.ip)) 
				) {
				if (criteria.port == this.port)
					ynRC = true;
		}
		return ynRC;
	}

	public void setHostNameOrIp(String val) {
		if (isIpv4(val)) {
			setIp(val);
		} else {
			this.hostNameOrIpAddress = val;
		}
	}
	/**
	 * Expects host:port format
	 * @param hostColonPort
	 */
	public HostPort(String hostColonPort) {
		StringTokenizer st = new StringTokenizer(hostColonPort);
		
		String tmpHostName = (String)st.nextToken(HOST_PORT_DELIM).trim();
		
		setHostNameOrIp(tmpHostName.trim());
		String port = (String)st.nextToken();
		this.port = Integer.valueOf(port);
	}
	public HostPort() {
		// TODO Auto-generated constructor stub
	}
	private static String HOST_PORT_DELIM = ":";
	public static int UNINITIALIZED_PORT=-2;
	public String hostNameOrIpAddress = null;
	/**
	 * Kept private so this class can retrieve the IP, instead of relying on end user.
	 * Connections are stored in a map in ConnectionList, therefore we need a single way (IP) address
	 * to identify a host, instead of having multiple ways (IP, DNS name).
	 */
	private String ip = null;
	
	public int port = UNINITIALIZED_PORT;
	public String key() {
		String hostName = ip == null ? this.hostNameOrIpAddress : this.ip;
		return (hostName+HOST_PORT_DELIM+String.valueOf(this.port) ).trim().toLowerCase();
	}
	public String getHostNameKey() {
		String rc = null;
		if (hostNameOrIpAddress!=null) {
			rc = hostNameOrIpAddress+HOST_PORT_DELIM + (""+this.port).trim().toLowerCase();
		}
		return rc;
	}
	public String getIpKeyName() {
		String rc = null;
		if (ip!=null) {
			rc = ip+HOST_PORT_DELIM + (""+this.port).trim();
		}
		return rc;
	}
	public String toString() {
		return key();
	}
	public static List<HostPort> parseList(String listOfMultipleHostPorts) {
		List<HostPort> hostsAndPorts = new ArrayList<HostPort>();
		StringTokenizer st = new StringTokenizer(listOfMultipleHostPorts,",");
		String currentLine = null;
		while(st.hasMoreTokens()) {
			currentLine = (String)st.nextToken();
			HostPort hostPort = new HostPort(currentLine);
			hostsAndPorts.add(hostPort);
		}
		return hostsAndPorts;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}
	
	public List<String> validate() {
		List<String> errorMessages = new ArrayList<String>();
	    if (hostNameOrIpAddress.length() == 0) {
	    	errorMessages.add(DefaultFactory.getFactory().getMessages().getInvalidHostMessage());
	    }
	    if (port <= 0) {
	    	errorMessages.add(DefaultFactory.getFactory().getMessages().getInvalidPortMessage());
	    }
		return errorMessages;
	}
	
}
