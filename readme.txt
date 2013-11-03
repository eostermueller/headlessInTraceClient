November 3, 2013

Building/testing the headlessInTraceClient.
----------------------------------------------------------------
a) Make sure maven is installed and mvn in in the path
b) Download source for headlessInTraceClient
c) execute bin/buildAndTestAll.sh.  Current directory does not matter.


Main Features
---------------------------------
1) Support for collecting events from multiple intrace agents on multiple hosts/ports
	For details, see org.intrace.client.test.level2.connection.lowLevel.TestMultipleConnections_requiresTwoExternalAgents

2) Test infrastructure captures intrace events from the example webapp, 
	which is deployed to Jetty by maven and used during JUnit tests.
	For details, see ./example.webapp/src/main/java/example/webapp/run/WebAppLauncher.java
	and bin/examplesStart.sh

3) Can optionally group all events that belong to a single stateless request, like a SOA request.  
	Includes a callback class that will notify you when all events from each request are complete.
	For details, see org.intrace.client.test.level3.request.TestRequestEventCollection

4) Initial support for multi-language support for human-readable messages.

5) Support for a customize-able factory, to load customized versions/subclasses of many components.
	For details, see src/test/java/org/intrace/client/test/level3/request/TestMultiThreadedRequestEventCollection.java 
	Feature 3, above, was built using this customize-able factory.


Environment
---------------------------------
OS:
	This has only been tested on MacOS/Mavericks.
	I expect the scripts in the bin folder will work on any bash/*nix.
	I expect that every thing outside the bin folder will work on MS-Windows.

Maven:
	Apache Maven 3.1.1 (0728685237757ffbf44136acec0402957f723d9a; 2013-09-17 10:22:22-0500)
	Maven home: /usr/local/Cellar/maven/3.1.1/libexec
	Java version: 1.7.0_12-ea, vendor: Oracle Corporation
	Java home: /Library/Java/JavaVirtualMachines/jdk1.7.0_12.jdk/Contents/Home/jre
	Default locale: en_US, platform encoding: UTF-8
	OS name: "mac os x", version: "10.9", arch: "x86_64", family: "mac"

