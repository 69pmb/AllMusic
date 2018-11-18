@echo off
cd C:\Users\Pierre-Marie\git\AllMusic
:debut
set /p answer="1. CMD\n2. Update\n3.Start\n"
set "result=nothing"
IF /i "%answer%"=="1" (
	call "AlMusic - CMD.bat"
) else IF /i "%answer%"=="2" (
	call "AlMusic - Update.bat"
) else IF /i "%answer%"=="3" (
	call "AlMusic - Start.bat"
) else (
 goto debut
)