if [ "$1" = "-c" ] || [ "$1" = "-C" ]
  then if [ "$JAVA_HOME" != "" ]
  then "$JAVA_HOME/bin/java" -Xms4m -Xmx8m -classpath "routerlogger.jar:lib/*" it.albertus.router.RouterLoggerCon
  else java -Xms4m -Xmx8m -classpath "routerlogger.jar:lib/*" it.albertus.router.RouterLoggerCon
  fi
elif [ "$1" = "" ]
  then if [ "$JAVA_HOME" != "" ]
  then "$JAVA_HOME/bin/java" -Xms8m -Xmx64m -classpath "routerlogger.jar:lib/*" it.albertus.router.RouterLoggerGui
  else java -Xms8m -Xmx64m -classpath "routerlogger.jar:lib/*" it.albertus.router.RouterLoggerGui
  fi
elif [ "$1" = "--help" ]
  then
  echo "Launches RouterLogger application."
  echo
  echo "Usage: routerlogger.sh [-c] [--help]"
  echo
  echo "  -c        Runs in console mode"
  echo "  --help    Shows this help"
else
  echo "routerlogger: unrecognized option '"$1"'"
  echo "Try 'routerlogger.sh --help' for more information."
fi
