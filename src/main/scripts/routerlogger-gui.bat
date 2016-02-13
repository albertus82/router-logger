@ECHO OFF
IF "%JAVA_HOME%" == "" START /BELOWNORMAL "" javaw.exe -Xms8m -Xmx64m -classpath "%~dp0routerlogger.jar;%~dp0lib/*" it.albertus.router.RouterLoggerGui
IF NOT "%JAVA_HOME%" == "" START /BELOWNORMAL "" "%JAVA_HOME%\bin\javaw.exe" -Xms8m -Xmx64m -classpath "%~dp0routerlogger.jar;%~dp0lib/*" it.albertus.router.RouterLoggerGui