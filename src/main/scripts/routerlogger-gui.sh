if [ "$JAVA_HOME" != "" ]
then "$JAVA_HOME/bin/java" -Xms8m -Xmx64m -classpath "routerlogger.jar:lib/*" it.albertus.router.RouterLoggerGui
else java -Xms8m -Xmx64m -classpath "routerlogger.jar:lib/*" it.albertus.router.RouterLoggerGui
fi
