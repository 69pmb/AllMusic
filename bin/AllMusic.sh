#!/bin/bash

if [ $# -ne 1 ]; then
    echo "Usage: $0 [update|build|sonar|format|light|full|code|cmd]"
    exit 1
fi

case "$1" in
update)
    git fetch --all --prune && git checkout main && git pull --rebase --autostash && echo "Application updated successfully"
    ;;
build)
    mvn install -q -Dmaven.test.skip=true && echo "Application built successfully"
    ;;
sonar)
    mvn sonar:sonar -q && echo "Application analysed successfully"
    ;;
format)
    mvn git-code-format:format-code -q && echo "Application formatted successfully"
    ;;
light)
    mvn exec:java -Dexec.args=true
    ;;
full)
    mvn exec:java -Dexec.args=false
    ;;
code)
    code .
    ;;
cmd)
    wt -w 0 nt -d "."
    ;;
*)
    echo "Invalid Command. Please retry."
    echo "Usage: $0 [update|build|sonar|format|light|full|code|cmd]"
    exit 1
    ;;
esac
