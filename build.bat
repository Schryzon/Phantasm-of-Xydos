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
set MYSQL_JAR=lib\mysql-connector-j-8.4.0.jar
if not exist !MYSQL_JAR! (
    echo [INFO] mysql-connector-j.jar missing. Fetching from Maven Central...
    powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.4.0/mysql-connector-j-8.4.0.jar' -OutFile '!MYSQL_JAR!'"
)

:: Download Jamepad (Gamepad library) if it doesn't exist
set JAMEPAD_JAR=lib\jamepad-2.0.14.1.jar
if not exist !JAMEPAD_JAR! (
    echo [INFO] jamepad-2.0.14.1.jar missing. Fetching from Maven Central...
    powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/com/badlogicgames/jamepad/jamepad/2.0.14.1/jamepad-2.0.14.1.jar' -OutFile '!JAMEPAD_JAR!'"
)

:: Download native-lib-loader (Jamepad dependency) if it doesn't exist
set NATIVELOADER_JAR=lib\native-lib-loader-2.5.0.jar
if not exist !NATIVELOADER_JAR! (
    echo [INFO] native-lib-loader-2.5.0.jar missing. Fetching from Maven Central...
    powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/scijava/native-lib-loader/2.5.0/native-lib-loader-2.5.0.jar' -OutFile '!NATIVELOADER_JAR!'"
)

echo [INFO] Compiling game engine and GUI classes...
javac -d bin -cp "bin;lib\*" src\*.java

if %ERRORLEVEL% EQU 0 (
    echo [SUCCESS] Build completed successfully. Use run.bat to execute.
) else (
    echo [ERROR] Compilation failed.
)
pause
