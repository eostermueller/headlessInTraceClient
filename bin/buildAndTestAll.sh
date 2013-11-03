#Prerequisites:
# a) Maven is installed, with mvn in the path
# b) you have already downloaded a copy of source code for Headless InTrace Client

#http://stackoverflow.com/questions/4774054/reliable-way-for-a-bash-script-to-get-the-full-path-to-itself
#Get the path of this script file
export SCRIPT_PATH=$( cd $(dirname $0) ; pwd -P )

# Build the sample web application
cd $SCRIPT_PATH/../example.webapp
mvn clean package

# Build the Headless InTrace Client
cd $SCRIPT_PATH/..
mvn clean -Dmaven.test.skip=true package

# Start the example application, each with in InTrace agent
$SCRIPT_PATH/examplesKill.sh
$SCRIPT_PATH/examplesStart.sh
#Allow some time for the examples to start so we can see what ports are listening
sleep 2
$SCRIPT_PATH/examplesDispInTraceAgents.sh

# Run the unit tests
mvn test
