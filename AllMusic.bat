@echo off
cd C:\Users\Pierre-Marie\git\AllMusic
:debut
set /p answer="1. CMD    2. Code    3. Update    4.Build    5.Start  "
set "result=nothing"
IF /i "%answer%"=="1" (
	cmd.exe /K "cd ."
) else IF /i "%answer%"=="2" (
	call code .
) else IF /i "%answer%"=="3" (
	call "AllMusic - Update.bat"
) else IF /i "%answer%"=="4" (
	call mvn install -q -Dmaven.test.skip=true
) else IF /i "%answer%"=="5" (
	call "AllMusic - Start.bat"
) else (
 goto debut
)