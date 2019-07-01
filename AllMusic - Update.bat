@echo off
git stash
git pull --rebase
git stash pop
git checkout HEAD -- src\main\resources\config.properties
pause