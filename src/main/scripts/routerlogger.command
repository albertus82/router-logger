#!/bin/sh
if [ "$1" = "" ]
  then if [ "$JAVA_HOME" != "" ]
  then nice "$JAVA_HOME/bin/java" -XstartOnFirstThread -Xms8m -Xmx64m -XX:+IgnoreUnrecognizedVMOptions --add-modules=java.activation -classpath "`dirname $0`/routerlogger.jar:`dirname $0`/dropins/*:`dirname $0`/lib/*" it.albertus.routerlogger.RouterLogger >/dev/null 2>&1 &
  else nice java -XstartOnFirstThread -Xms8m -Xmx64m -XX:+IgnoreUnrecognizedVMOptions --add-modules=java.activation -classpath "`dirname $0`/routerlogger.jar:`dirname $0`/dropins/*:`dirname $0`/lib/*" it.albertus.routerlogger.RouterLogger >/dev/null 2>&1 &
  fi
  osascript -e 'tell application "Terminal" to quit' &
  exit
else
  if [ "$JAVA_HOME" != "" ]
  then "$JAVA_HOME/bin/java" -Xms8m -Xmx32m -XX:+IgnoreUnrecognizedVMOptions --add-modules=java.activation -classpath "`dirname $0`/routerlogger.jar:`dirname $0`/dropins/*:`dirname $0`/lib/*" it.albertus.routerlogger.RouterLogger $1 $2
  else java -Xms8m -Xmx32m -XX:+IgnoreUnrecognizedVMOptions --add-modules=java.activation -classpath "`dirname $0`/routerlogger.jar:`dirname $0`/dropins/*:`dirname $0`/lib/*" it.albertus.routerlogger.RouterLogger $1 $2
  fi
fi
