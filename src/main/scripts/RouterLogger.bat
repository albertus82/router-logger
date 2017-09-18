@ECHO OFF
IF "%1" == "" GOTO GUI
:CON
IF "%JAVA_HOME%" == "" java.exe -Xms4m -Xmx16m -classpath "%~dp0RouterLogger.jar;%~dp0dropins\*;%~dp0lib\*" it.albertus.router.RouterLogger %1 %2
IF NOT "%JAVA_HOME%" == "" "%JAVA_HOME%\bin\java.exe" -Xms4m -Xmx16m -classpath "%~dp0RouterLogger.jar;%~dp0dropins\*;%~dp0lib\*" it.albertus.router.RouterLogger %1 %2
GOTO END
:GUI
IF "%JAVA_HOME%" == "" START /BELOWNORMAL "" javaw.exe -Xms8m -Xmx64m -classpath "%~dp0RouterLogger.jar;%~dp0dropins\*;%~dp0lib\*" it.albertus.router.RouterLogger
IF NOT "%JAVA_HOME%" == "" START /BELOWNORMAL "" "%JAVA_HOME%\bin\javaw.exe" -Xms8m -Xmx64m -classpath "%~dp0RouterLogger.jar;%~dp0dropins\*;%~dp0lib\*" it.albertus.router.RouterLogger
GOTO END
:END