ps -ef | grep example.FirstTraceExample | awk '{print $2}' | xargs -t -n 1 kill -9 1>/dev/null 2>&1
ps -ef | grep example.SecondTraceExample | awk '{print $2}' | xargs -t -n 1 kill -9 1>/dev/null 2>&1
ps -ef | grep example.webapp.run.WebAppLauncher | awk '{print $2}' | xargs -t -n 1 kill -9 1>/dev/null 2>&1
