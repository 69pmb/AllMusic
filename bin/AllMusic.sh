#!/bin/bash

while true; do
    clear
    echo "1. Code    2. Update    3. Build    4. Sonar    5. Start with Artist Panel    6. Start without Artist Panel    7. Quit"
    read -p "Votre choix: " answer

    case "$answer" in
    1)
        code .
        ;;
    2)
        git pull --rebase --autostash
        ;;
    3)
        mvn install -q -Dmaven.test.skip=true
        ;;
    4)
        mvn sonar:sonar -q
        ;;
    5)
        mvn exec:java -Dexec.args=true
        ;;
    6)
        mvn exec:java -Dexec.args=false
        ;;
    7)
        exit
        ;;
    *)
        echo "Choix invalide. Veuillez réessayer."
        ;;
    esac
done
