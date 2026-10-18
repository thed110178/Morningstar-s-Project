@echo off
title DontKnow
cls
echo ========================================================
echo         DONTKNOW - TOP-DOWN ROGUELITE SHOOTER
echo ========================================================
echo.
echo Dang khoi dong tro choi...
echo.

if exist "runtime\bin\java.exe" (
    "runtime\bin\java.exe" -jar DontKnow.jar
) else (
    java -jar DontKnow.jar
)

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ========================================================
    echo [THONG BAO LOI] Khong the khoi dong tro choi!
    echo Nguyen nhan: May cua ban chua cai dat Java 21 hoac bi xung dot phien ban.
    echo.
    echo Huong dan khac phuc:
    echo 1. Tai va cai dat Java 21 LTS (Mien phi, an toan) tai:
    echo    https://adoptium.net/temurin/releases/?version=21
    echo 2. Sau khi cai dat xong, nhap dup vao file RunGame.bat de choi!
    echo ========================================================
    echo.
    pause
)
