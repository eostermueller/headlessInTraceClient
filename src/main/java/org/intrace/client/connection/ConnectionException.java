package org.intrace.client.connection;

//private List<ConnectionPartTwo> m_connections = null;
public class ConnectionException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3894940496892281257L;
	private HostPort m_hostPort = null;
	private String m_msg = null;
	public ConnectionException(HostPort hostPort, String msg) {
		m_hostPort = hostPort;
		m_msg = msg;
	}
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(m_hostPort.key());
		sb.append(" ").append(m_msg);
		
		return sb.toString();
	}
}