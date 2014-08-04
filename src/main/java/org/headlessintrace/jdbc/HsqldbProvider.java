package org.headlessintrace.jdbc;


public class HsqldbProvider implements IJdbcProvider {

	@Override
	public String[] getStatementPackageAndClass() {
		String[] a = {"org.hsqldb.jdbc.JDBCStatement"}; 
		return a;
	}

	@Override
	public String[] getConnectionPackageAndClass() {
		String[] a = {"org.hsqldb.jdbc.jdbcConnection"}; 
		return a;
	}

	@Override
	public String getVersion() {
		return "unk";
	}

}
