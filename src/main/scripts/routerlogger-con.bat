@ECHO OFF
IF "%JAVA_HOME%" == "" java.exe -Xms4m -Xmx8m -classpath "%~dp0routerlogger.jar;%~dp0lib/*" it.albertus.router.RouterLoggerCon
IF NOT "%JAVA_HOME%" == "" "%JAVA_HOME%\bin\java.exe" -Xms4m -Xmx8m -classpath "%~dp0routerlogger.jar;%~dp0lib/*" it.albertus.router.RouterLoggerCon