while ($true) {
    Clear-Host
    Write-Host "1. PS    2. Code    3. Update    4. Build    5. Sonar    6. Start with Artist Panel    7. Start without Artist Panel    8. Exit"
    $answer = Read-Host -Prompt "Votre choix"

    switch ($answer) {
        "1" { Set-Location -Path $PSScriptRoot }
        "2" { Start-Process -FilePath "code" -ArgumentList "." }
        "3" { & git pull --rebase --autostash }
        "4" { & mvn install -q -Dmaven.test.skip=true }
        "5" { & mvn sonar:sonar -q }
        "6" { & mvn exec:java -Dexec.args="true" }
        "7" { & mvn exec:java -Dexec.args="false" }
        "8" { break }
        default { Write-Host "Choix invalide. Veuillez réessayer." }
    }
}
