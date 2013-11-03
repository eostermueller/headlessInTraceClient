mvn -f $(dirname $0)/../pom.xml \
	-Dintrace.test.class=example.FirstTraceExample \
	antrun:run &

sleep 2

mvn -f $(dirname $0)/../pom.xml \
	-Dintrace.test.class=example.SecondTraceExample \
	antrun:run &

sleep 2

pushd $(dirname $0)/../example.webapp

mvn antrun:run &
