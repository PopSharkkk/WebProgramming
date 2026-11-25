@echo off
setlocal
if "%JAVA_HOME%"=="" (
    if exist "C:\Program Files\Microsoft\jdk-21.0.9.10-hotspot" (
        set "JAVA_HOME=C:\Program Files\Microsoft\jdk-21.0.9.10-hotspot"
    )
)
if defined JAVA_HOME set "PATH=%JAVA_HOME%\bin;%PATH%"

where mvn >nul 2>nul
if %ERRORLEVEL%==0 (
    call mvn %*
    exit /b %ERRORLEVEL%
)

if exist "%USERPROFILE%\.m2\maven-3.9.9\apache-maven-3.9.9\bin\mvn.cmd" (
    call "%USERPROFILE%\.m2\maven-3.9.9\apache-maven-3.9.9\bin\mvn.cmd" %*
    exit /b %ERRORLEVEL%
)

if defined M2_HOME (
    if exist "%M2_HOME%\bin\mvn.cmd" (
        call "%M2_HOME%\bin\mvn.cmd" %*
        exit /b %ERRORLEVEL%
    )
)

if defined MAVEN_HOME (
    if exist "%MAVEN_HOME%\bin\mvn.cmd" (
        call "%MAVEN_HOME%\bin\mvn.cmd" %*
        exit /b %ERRORLEVEL%
    )
)

echo [mvnw] Apache Maven not found on PATH or in %%USERPROFILE%%\.m2\maven-3.9.9.
echo [mvnw] Please install Apache Maven or run start-server.bat.
exit /b 1
