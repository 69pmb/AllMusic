@echo off
cd C:\Users\Pierre-Marie\git\AllMusic
:debut
set /p answer="1. CMD    2. Update    3.Start  "
set "result=nothing"
IF /i "%answer%"=="1" (
	call "AllMusic - CMD.bat"
) else IF /i "%answer%"=="2" (
	call "AllMusic - Update.bat"
) else IF /i "%answer%"=="3" (
	call "AllMusic - Start.bat"
) else (
 goto debut
)