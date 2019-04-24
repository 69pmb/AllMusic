@echo off
cd C:\Users\workspace\git\AllMusic
:debut
set /p answer="1. CMD    2. Code    3. Update    4.Build    5.Start with Artist Panel    6.Start without Artist Panel  "
set "result=nothing"
IF /i "%answer%"=="1" (
	cmd.exe /K "cd ."
) else IF /i "%answer%"=="2" (
	call code .
) else IF /i "%answer%"=="3" (
	call "AllMusic - Update.bat"
 goto debut
) else IF /i "%answer%"=="4" (
	call mvn install -q -Dmaven.test.skip=true
 goto debut
) else IF /i "%answer%"=="5" (
	mvn exec:java -Dexec.args=true
) else IF /i "%answer%"=="6" (
	mvn exec:java -Dexec.args=false
) else (
 goto debut
)