#!/bin/sh
PRG="$0"
while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done
PRGDIR=`dirname "$PRG"`
if [ "$1" = "" ]
  then if [ "$JAVA_HOME" != "" ]
  then nice "$JAVA_HOME/bin/java" -Xms8m -Xmx64m --add-modules=java.activation -classpath "$PRGDIR/routerlogger.jar:$PRGDIR/lib/*" it.albertus.router.RouterLogger
  else nice java -Xms8m -Xmx64m --add-modules=java.activation -classpath "$PRGDIR/routerlogger.jar:$PRGDIR/lib/*" it.albertus.router.RouterLogger
  fi
else
  if [ "$JAVA_HOME" != "" ]
  then "$JAVA_HOME/bin/java" -Xms4m -Xmx16m --add-modules=java.activation -classpath "$PRGDIR/routerlogger.jar:$PRGDIR/lib/*" it.albertus.router.RouterLogger $1 $2
  else java -Xms4m -Xmx16m --add-modules=java.activation -classpath "$PRGDIR/routerlogger.jar:$PRGDIR/lib/*" it.albertus.router.RouterLogger $1 $2
  fi
fi
