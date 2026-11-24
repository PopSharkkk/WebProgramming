@echo off
setlocal
if "%JAVA_HOME%"=="" set "JAVA_HOME=C:\Program Files\Microsoft\jdk-21.0.9.10-hotspot"
set "MVN_LOCAL=C:\Users\Rinaltra\.gemini\antigravity\brain\a4b7e33e-038a-448d-9f13-d100a58d83ab\scratch\maven\apache-maven-3.9.9\bin\mvn.cmd"
if exist "%MVN_LOCAL%" (
    "%MVN_LOCAL%" %*
    exit /b %ERRORLEVEL%
)
where mvn >nul 2>nul
if %ERRORLEVEL%==0 (
    mvn %*
    exit /b %ERRORLEVEL%
)
echo Maven 3.9.9 not found. Please install Maven or run via start-server.bat.
exit /b 1
