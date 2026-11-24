@echo off
setlocal
title Wild Indonesia — JSP Travel Guide & Expedition Rentals (http://localhost:8080)
if "%JAVA_HOME%"=="" set "JAVA_HOME=C:\Program Files\Microsoft\jdk-21.0.9.10-hotspot"
set "PATH=%JAVA_HOME%\bin;%PATH%"
cd /d "%~dp0"

if not exist "target\classes\com\wildindonesia\AppServer.class" (
    echo Compiling project with Maven...
    call "%~dp0mvnw.cmd" package -DskipTests
)

echo Starting Wild Indonesia JSP Server on http://localhost:8080 ...
start "" "http://localhost:8080"
"%JAVA_HOME%\bin\java.exe" -cp "target\classes;target\dependency\*" com.wildindonesia.AppServer
pause
