package org.headlessintrace.jdbc;


public class MySqlProvider implements IJdbcProvider {

	@Override
	public String[] getStatementPackageAndClass() {
		String[] a = {"com.mysql.jdbc.Statement"}; 
		return a;
	}

	@Override
	public String[] getConnectionPackageAndClass() {
		String[] a = {"com.mysql.jdbc.Connection"}; 
		return a;
	}

	@Override
	public String getVersion() {
		return "mysql-connector-java-5.1.31-bin.jar";
	}

}
