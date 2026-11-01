@echo off
chcp 65001 >nul
title Tạo phím tắt Alone In The Zone ra Desktop
echo ======================================================================
echo    ĐANG TẠO PHÍM TẮT (SHORTCUT) RA MÀN HÌNH CHÍNH (DESKTOP)
echo ======================================================================
echo.

set "GAME_DIR=%~dp0"
set "BAT_FILE=%GAME_DIR%RunAloneInTheZone.bat"
if not exist "%BAT_FILE%" (
    set "BAT_FILE=%GAME_DIR%RunGame.bat"
)

powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  "$ws = New-Object -ComObject WScript.Shell; " ^
  "$desktop = [Environment]::GetFolderPath('Desktop'); " ^
  "$shortcutPath = Join-Path $desktop 'Alone In The Zone.lnk'; " ^
  "$sc = $ws.CreateShortcut($shortcutPath); " ^
  "$sc.TargetPath = '%BAT_FILE%'; " ^
  "$sc.WorkingDirectory = '%GAME_DIR%'; " ^
  "$sc.Description = 'Alone In The Zone - Tactical Roguelike Shooter'; " ^
  "$icoPath = Join-Path '%GAME_DIR%' 'icon.ico'; " ^
  "if (Test-Path $icoPath) { $sc.IconLocation = \"$icoPath,0\"; } " ^
  "$sc.Save(); " ^
  "Write-Host '[THÀNH CÔNG] Đã tạo Shortcut Alone In The Zone trên màn hình Desktop với biểu tượng Player!' -ForegroundColor Green"

echo.
echo ======================================================================
pause

