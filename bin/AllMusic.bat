@echo off

setlocal enabledelayedexpansion

if "%~1"=="" (
    echo Usage: %0 [ps^|update^|build^|sonar^|format^|light^|full]
    exit /b 1
)

set "command=%~1"

if "%command%"=="ps" (
	powershell.exe -noexit -command "cd ."
) else if "%command%"=="update" (
    git fetch --all --prune && git checkout main && git pull --rebase --autostash
    echo Application updated successfully
) else if "%command%"=="build" (
    mvn install -q -Dmaven.test.skip=true
    echo Application built successfully
) else if "%command%"=="sonar" (
    mvn sonar:sonar -q
    echo Application analyzed successfully
) else if "%command%"=="format" (
    mvn git-code-format:format-code -q
    echo Application formatted successfully
) else if "%command%"=="light" (
    mvn exec:java -Dexec.args=true
) else if "%command%"=="full" (
    mvn exec:java -Dexec.args=false
) else (
    echo Invalid Command. Please retry.
    echo Usage: %0 [ps^|update^|build^|sonar^|format^|light^|full]
    exit /b 1
)

endlocal
