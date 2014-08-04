package org.headlessintrace.jdbc;


public class PostgresProvider implements IJdbcProvider {

	@Override
	public String[] getStatementPackageAndClass() {
		String[] a = {
				"org.postgresql.core.BaseStatement"
				,"org.postgresql.jdbc4.Jdbc4CallableStatement"
				,"org.postgresql.jdbc4.Jdbc4PreparedStatement"
				,"org.postgresql.jdbc4.Jdbc4Statement"
		};
		return a;
	}

	@Override
	public String[] getConnectionPackageAndClass() {
		String[] a = {
				 "org.postgresql.core.BaseConnection"
				,"org.postgresql.jdbc4.Jdbc4Connection"
				//,"org.postgresql.jdbc2.AbstractJdbc2Connection"
				//,"org.postgresql.jdbc3g.AbstractJdbc3gConnection"
				//,"org.postgresql.jdbc4.AbstractJdbc4Connection"
				//,"org.postgresql.jdbc3.AbstractJdbc3Connection"
		}; 
		
		return a;
	}

	@Override
	public String getVersion() {
		return "9.2-1002";
	}

}
