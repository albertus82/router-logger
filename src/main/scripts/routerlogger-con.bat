@ECHO OFF
IF "%JAVA_HOME%" EQU "" java.exe -Xms4m -Xmx8m -classpath "%~dp0routerlogger.jar;%~dp0lib/*" it.albertus.router.RouterLoggerCon
IF "%JAVA_HOME%" NEQ "" "%JAVA_HOME%\bin\java.exe" -Xms4m -Xmx8m -classpath "%~dp0routerlogger.jar;%~dp0lib/*" it.albertus.router.RouterLoggerCon