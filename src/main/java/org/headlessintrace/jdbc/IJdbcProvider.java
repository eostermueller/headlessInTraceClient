package org.headlessintrace.jdbc;

/**
 * This interface specifies the names of the java classes to instrument to capture InTrace JDBC events for one specific JDBC driver.
 * 
 * @author erikostermueller
 *
 */
public interface IJdbcProvider {
	String[] getStatementPackageAndClass();
	String[] getConnectionPackageAndClass();
	String getVersion();
}
