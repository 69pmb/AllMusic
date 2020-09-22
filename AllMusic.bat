@echo off
cd C:\Users\Pierre-Marie\git\AllMusic
:debut
set /p answer="1. CMD    2. Code    3. Update    4.Build    5.Sonar    6.Start with Artist Panel    7.Start without Artist Panel  "
set "result=nothing"
IF /i "%answer%"=="1" (
	cmd.exe /K "cd ."
) else IF /i "%answer%"=="2" (
	START /MIN code .
	goto debut
) else IF /i "%answer%"=="3" (
	call git pull --rebase --autostash
 goto debut
) else IF /i "%answer%"=="4" (
	call mvn install -q -Dmaven.test.skip=true
 goto debut
) else IF /i "%answer%"=="5" (
	call mvn sonar:sonar -q
 goto debut
) else IF /i "%answer%"=="6" (
	mvn exec:java -Dexec.args=true
) else IF /i "%answer%"=="7" (
	mvn exec:java -Dexec.args=false
) else IF /i "%answer%"=="q" (
	exit /B
) else (
 goto debut
)