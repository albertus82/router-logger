if [ "$1" = "--help" ]
  then
  echo "Start RouterLogger application."
  echo
  echo "Usage: routerlogger.sh [-c] [--help]"
  echo
  echo "  -c        start in console mode"
  echo "  --help    display this help and exit"
elif [ "$2" != "" ]
  then
  echo "routerlogger: too many parameters - $2"
  echo "Try 'routerlogger.sh --help' for more information."
elif [ "$1" = "-c" ] || [ "$1" = "-C" ]
  then if [ "$JAVA_HOME" != "" ]
  then "$JAVA_HOME/bin/java" -Xms4m -Xmx8m -classpath "routerlogger.jar:lib/*" it.albertus.router.RouterLoggerCon
  else java -Xms4m -Xmx8m -classpath "routerlogger.jar:lib/*" it.albertus.router.RouterLoggerCon
  fi
elif [ "$1" = "" ]
  then if [ "$JAVA_HOME" != "" ]
  then "$JAVA_HOME/bin/java" -Xms8m -Xmx64m -classpath "routerlogger.jar:lib/*" it.albertus.router.RouterLoggerGui
  else java -Xms8m -Xmx64m -classpath "routerlogger.jar:lib/*" it.albertus.router.RouterLoggerGui
  fi
else
  echo "routerlogger: unrecognized option '"$1"'"
  echo "Try 'routerlogger.sh --help' for more information."
fi
