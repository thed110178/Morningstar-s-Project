@echo off
cd /d "%~dp0"
title Alone in the zone
echo Starting Alone in the zone...
if exist "runtime\bin\javaw.exe" (
    start "" "runtime\bin\javaw.exe" -Dsun.java2d.d3d=true -Dsun.java2d.ddforcevram=true -Dsun.java2d.accthreshold=0 -jar AloneInTheZone.jar
) else (
    start javaw -Dsun.java2d.d3d=true -Dsun.java2d.ddforcevram=true -Dsun.java2d.accthreshold=0 -jar AloneInTheZone.jar
)
exit
