if [ "$JAVA_HOME" != "" ]
then "$JAVA_HOME/bin/java" -Xms4m -Xmx8m -classpath "routerlogger.jar:lib/*" it.albertus.router.RouterLoggerCon
else java -Xms4m -Xmx8m -classpath "routerlogger.jar:lib/*" it.albertus.router.RouterLoggerCon
fi
