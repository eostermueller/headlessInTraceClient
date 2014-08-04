#mvn -Dtest=TestRequestEventCollection test
#mvn -Dtest=org.intrace.client.test.level2.connection.lowLevel.TestMultipleConnections_requiresTwoExternalAgents test
#mvn -Dtest=org.intrace.client.test.level2.connection.lowLevel.TestMultipleConnections_requiresTwoExternalAgents test
#mvn -Dtest=org.intrace.client.test.level2.connection.JdbcInterfaceTest test
mvn -Dtest=org.intrace.client.test.level2.connection.lowLevel.TestBasicConnection_RequiresExternalAgent test
#mvn -Dtest=org.intrace.client.test.level2.connection.lowLevel.TestOtherBasicConnection_RequiresExternalAgent test
#mvn -Dtest=org.intrace.client.test.level2.connection.InterfaceTracingTest test
