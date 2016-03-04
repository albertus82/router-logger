#!/bin/sh
if [ "$1" = "" ]
  then if [ "$JAVA_HOME" != "" ]
  then nice "$JAVA_HOME/bin/java" -XstartOnFirstThread -Xms8m -Xmx64m -classpath "`dirname $0`/routerlogger.jar:`dirname $0`/lib/*" it.albertus.router.RouterLogger >/dev/null 2>&1 &
  else nice java -XstartOnFirstThread -Xms8m -Xmx64m -classpath "`dirname $0`/routerlogger.jar:`dirname $0`/lib/*" it.albertus.router.RouterLogger >/dev/null 2>&1 &
  fi
  osascript -e 'tell application "Terminal" to quit' &
  exit
else
  if [ "$JAVA_HOME" != "" ]
  then "$JAVA_HOME/bin/java" -Xms4m -Xmx8m -classpath "`dirname $0`/routerlogger.jar:`dirname $0`/lib/*" it.albertus.router.RouterLogger $1 $2
  else java -Xms4m -Xmx8m -classpath "`dirname $0`/routerlogger.jar:`dirname $0`/lib/*" it.albertus.router.RouterLogger $1 $2
  fi
fi
