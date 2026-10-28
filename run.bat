@echo off
title Mellow - Emergency Dispatch Simulator
cd /d "%~dp0"
if not exist out mkdir out

set "JAVAC_CMD=javac"
set "JAVA_CMD=java"

where javac >nul 2>nul
if %errorlevel% neq 0 (
    if exist "C:\Program Files\Apache NetBeans\jdk\bin\javac.exe" (
        set "JAVAC_CMD=C:\Program Files\Apache NetBeans\jdk\bin\javac.exe"
        set "JAVA_CMD=C:\Program Files\Apache NetBeans\jdk\bin\java.exe"
    ) else if exist "C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.2\jbr\bin\javac.exe" (
        set "JAVAC_CMD=C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.2\jbr\bin\javac.exe"
        set "JAVA_CMD=C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.2\jbr\bin\java.exe"
    )
)

echo Compiling Mellow...
"%JAVAC_CMD%" -d out -sourcepath src src/Main.java
if %errorlevel% neq 0 (
    echo Compilation failed!
    pause
    exit /b %errorlevel%
)
echo Launching Mellow...
"%JAVA_CMD%" -cp out Main
