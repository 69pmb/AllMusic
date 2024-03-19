param (
    [string]$command
)

if (-not $command) {
    Write-Host "Usage: .\script.ps1 [update|build|sonar|format|light|full|code|cmd]"
    exit 1
}

switch ($command) {
    "update" {
        git fetch --all --prune && git checkout main && git pull --rebase --autostash
        Write-Host "Application updated successfully"
    }
    "build" {
        mvn install -q -Dmaven.test.skip=true
        Write-Host "Application built successfully"
    }
    "sonar" {
        mvn sonar:sonar -q
        Write-Host "Application analyzed successfully"
    }
    "format" {
        mvn git-code-format:format-code -q
        Write-Host "Application formatted successfully"
    }
    "light" {
        mvn exec:java -D"exec.args='true'"
    }
    "full" {
        mvn exec:java -D"exec.args='false'"
    }
    "code" {
        code .
    }
    "cmd" {
	    powershell.exe -noexit -command "cd ."
    }
    default {
        Write-Host "Invalid Command. Please retry."
        Write-Host "Usage: .\script.ps1 [update|build|sonar|format|light|full|code|cmd]"
        exit 1
    }
}
