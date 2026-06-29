@echo off
setlocal enabledelayedexpansion

echo ==========================================
echo PHANTASM OF XYDOS: COMPILATION SYSTEM
echo ==========================================

:: Create directories
if not exist lib mkdir lib
if not exist bin mkdir bin
if not exist assets mkdir assets

:: Download MySQL connector jar if it doesn't exist
set JAR_FILE=lib\mysql-connector-j-8.4.0.jar
if not exist !JAR_FILE! (
    echo [INFO] mysql-connector-j.jar missing. Fetching from Maven Central...
    powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.4.0/mysql-connector-j-8.4.0.jar' -OutFile '!JAR_FILE!'"
    if exist !JAR_FILE! (
        echo [SUCCESS] MySQL JDBC Driver downloaded.
    ) else (
        echo [WARNING] Failed to download MySQL JDBC Driver. MySQL connectivity will be disabled.
    )
)

echo [INFO] Compiling game engine and GUI classes...
javac -d bin -cp "bin;lib\*" src\*.java

if %ERRORLEVEL% EQU 0 (
    echo [SUCCESS] Build completed successfully. Use run.bat to execute.
) else (
    echo [ERROR] Compilation failed.
)
pause
