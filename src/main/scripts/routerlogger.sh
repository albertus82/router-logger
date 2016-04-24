#!/bin/sh
if [ "$1" = "" ]
  then if [ "$JAVA_HOME" != "" ]
  then nice "$JAVA_HOME/bin/java" -Xms8m -Xmx64m -classpath "routerlogger.jar:lib/*" it.albertus.router.RouterLogger
  else nice java -Xms8m -Xmx64m -classpath "routerlogger.jar:lib/*" it.albertus.router.RouterLogger
  fi
else
  if [ "$JAVA_HOME" != "" ]
  then "$JAVA_HOME/bin/java" -Xms4m -Xmx16m -classpath "routerlogger.jar:lib/*" it.albertus.router.RouterLogger $1 $2
  else java -Xms4m -Xmx16m -classpath "routerlogger.jar:lib/*" it.albertus.router.RouterLogger $1 $2
  fi
fi
