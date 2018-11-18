@echo off
cd C:\DEV\workspace\AllMusic
:debut
set /p answer="Artist Panel Y/N ?"
set "result=nothing"
IF /i "%answer%"=="Y" (
	echo "hllo"
	set "result=true"
) else IF /i "%answer%"=="y" (
	set "result=true"
) else IF /i "%answer%"=="yes" (
	set "result=true"
) else IF /i "%answer%"=="Yes" (
	set "result=true"
) else IF /i "%answer%"=="O" (
	set "result=true"
) else IF /i "%answer%"=="Oui" (
	set "result=true"
) else IF /i "%answer%"=="oui" (
	set "result=true"
) else IF /i "%answer%"=="o" (
	set "result=true"
) else IF /i "%answer%"=="N" (
	set "result=false"
) else IF /i "%answer%"=="n" (
	set "result=false"
) else IF /i "%answer%"=="no" (
	set "result=false"
) else IF /i "%answer%"=="No" (
	set "result=false"
) else IF /i "%answer%"=="Non" (
	set "result=false"
) else IF /i "%answer%"=="non" (
	set "result=false"
) else (
	echo "coucou"
	set "result=nothing"
)
IF not "%result%"=="nothing" ( mvn exec:java -Dexec.args=%result% ) else goto debut
pause>nul
