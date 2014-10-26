package org.headlessintrace.jdbc;


public class Db2Provider implements IJdbcProvider {

	@Override
	public String[] getStatementPackageAndClass() {
		String[] a = {
				"com.ibm.db2.jcc.DB2Statement"
		};
		return a;
	}

	@Override
	public String[] getConnectionPackageAndClass() {
		String[] a = {
				 "com.ibm.db2.jcc.b.bb"
		}; 
		
		return a;
	}

	@Override
	public String getVersion() {
		return "10.5";
	}

}


