@echo off
cd C:\Users\workspace\git\AllMusic
git sh
git pl
git pop
git ck HEAD -- src\main\resources\config.properties
mvn install -q -Dmaven.test.skip=true
pause