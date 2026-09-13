@echo off
setlocal enabledelayedexpansion

:: Automatically switch to the script's directory so it works from anywhere or as admin
cd /d "%~dp0"

echo ========================================================
echo   TELEPHONE BILL MANAGEMENT SYSTEM - LAUNCHER
echo ========================================================
echo.

:: Detect Maven
set "MVN_CMD=mvn"
where mvn >nul 2>nul
if %errorlevel% neq 0 (
    if exist "%USERPROFILE%\Downloads\maven-mvnd-1.0.2-windows-amd64\maven-mvnd-1.0.2-windows-amd64\mvn\bin\mvn.cmd" (
        set "MVN_CMD=%USERPROFILE%\Downloads\maven-mvnd-1.0.2-windows-amd64\maven-mvnd-1.0.2-windows-amd64\mvn\bin\mvn.cmd"
    ) else (
        echo [ERROR] Maven not found in PATH or Downloads folder.
        echo Please ensure Maven or Java is properly configured.
        pause
        exit /b 1
    )
)

echo [INFO] Project Directory: %CD%
echo [INFO] Using Maven: !MVN_CMD!
echo [INFO] Starting JavaFX application...
echo.

call "!MVN_CMD!" compile javafx:run

if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Application terminated with an error code %errorlevel%.
    pause
)
