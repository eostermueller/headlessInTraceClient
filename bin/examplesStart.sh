mvn -f $(dirname $0)/../pom.xml \
	-Dintrace.test.class=example.FirstTraceExample \
	antrun:run 1> out.1 2>&1 &

sleep 5

mvn -f $(dirname $0)/../pom.xml \
	-Dintrace.test.class=example.SecondTraceExample \
	antrun:run 1> out.2 2>&1 &

sleep 5

pushd $(dirname $0)/../example.webapp

mvn antrun:run 1> out.3 2>&1 &
