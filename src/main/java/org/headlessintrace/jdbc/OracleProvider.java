package org.headlessintrace.jdbc;


public class OracleProvider implements IJdbcProvider {

	@Override
	public String[] getStatementPackageAndClass() {
		String[] a = {"oracle.jdbc.driver.OracleStatement"}; 
		return a;
	}

	@Override
	public String[] getConnectionPackageAndClass() {
		String[] a = {"oracle.jdbc.driver.OracleConnection"}; 
		return a;
	}

	@Override
	public String getVersion() {
		return "unk";
	}

}
