@ECHO OFF
IF "%1" == "/?" GOTO HLP
IF NOT "%2" == "" GOTO ER2
IF "%1" == "/c" GOTO CON
IF "%1" == "/C" GOTO CON
IF "%1" == "" GOTO GUI
GOTO ER1
:CON
IF "%JAVA_HOME%" == "" java.exe -Xms4m -Xmx8m -classpath "%~dp0routerlogger.jar;%~dp0lib/*" it.albertus.router.RouterLoggerCon
IF NOT "%JAVA_HOME%" == "" "%JAVA_HOME%\bin\java.exe" -Xms4m -Xmx8m -classpath "%~dp0routerlogger.jar;%~dp0lib/*" it.albertus.router.RouterLoggerCon
GOTO END
:GUI
IF "%JAVA_HOME%" == "" START /BELOWNORMAL "" javaw.exe -Xms8m -Xmx64m -classpath "%~dp0routerlogger.jar;%~dp0lib/*" it.albertus.router.RouterLoggerGui
IF NOT "%JAVA_HOME%" == "" START /BELOWNORMAL "" "%JAVA_HOME%\bin\javaw.exe" -Xms8m -Xmx64m -classpath "%~dp0routerlogger.jar;%~dp0lib/*" it.albertus.router.RouterLoggerGui
GOTO END
:ER1
ECHO.RouterLogger: unrecognized option - %1
ECHO.Try 'ROUTERLOGGER /?' for more information.
GOTO END
:ER2
ECHO.RouterLogger: too many parameters - %2
ECHO.Try 'ROUTERLOGGER /?' for more information.
GOTO END
:HLP
ECHO.Launches RouterLogger application.
ECHO.
ECHO.Usage: ROUTERLOGGER [/C]
ECHO.
ECHO.  /C    Runs in console mode.
ECHO.
:END