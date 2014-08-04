#http://stackoverflow.com/questions/4774054/reliable-way-for-a-bash-script-to-get-the-full-path-to-itself
#Get the path of this script file
export SCRIPT_PATH=$( cd $(dirname $0) ; pwd -P )
export LOG_PATH=$SCRIPT_PATH/../logs

mkdir -p $LOG_PATH


mvn -f $(dirname $0)/../pom.xml \
	-Dheadlessintrace.test.class=example.FirstTraceExample \
	antrun:run 1> $LOG_PATH/out.1 2>&1 &

sleep 5

mvn -f $(dirname $0)/../pom.xml \
	-Dheadlessintrace.test.class=example.SecondTraceExample \
	antrun:run 1> $LOG_PATH/out.2 2>&1 &

sleep 5

pushd $(dirname $0)/../example.webapp

mvn antrun:run 1> $LOG_PATH/out.3 2>&1 &
