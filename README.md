[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=69pmb_AllMusic&metric=alert_status)](https://sonarcloud.io/dashboard?id=69pmb_AllMusic)
# AllMusic
### Purposes of AllMusic:

The main goal of this app is to discover the very best of music according of publications like NME, Pitchfork, Rolling Stones...

*AllMusic* can import and convert songs or albums files into XML files.

A datable file like is then created (the `final.xml` file).

You can search throught this database/file by artists, titles, dates or types (song or album) for instance.

You can browse by files and also create annual ranking by song, album and artist.

### Configuration:

A property file is used for the application.  
It can be found there: ` AllMusic/src/main/resources/config.properties`  
It defines the following properties:  

* resources: absolute path of the resources folder, where all files will be found

* output: absolute path where files will be generated 

* final: file name that's hold all the compositions

* music: folder name of the *txt* files

* notepad: absolute path of `notepad++.exe`

* excel: absolute path of `excel.exe`

* xml: folder name of the *XML* files

* level: logging level (`DEBUG`, `INFO`, `WARN` or `ERROR`)

* csv_separator: separator in generated csv files

* debug_ui: if the ui debug mode is enabled

### To run AllMusic:

- Install Java 11, Maven 3 and Git.
- In your workspace (in command line):
```bash
# To checkout the sources
git clone https://github.com/69pmb/AllMusic.git
# Go in the 'AllMusic' folder
cd AllMusic
# To download the dependencies
mvn install -q -Dmaven.test.skip=true
# To launch AllMusic
mvn exec:java
```
- Or you can use the *Windows* utility script `AllMusic.bat` or `AllMusic.ps1`
- Or you can install the *Unix* utility script with the `install.sh` and use it by entering `music`

## With Docker

You must install wsl and activate wslg, for that see:  
https://github.com/microsoft/wslg/tree/main?tab=readme-ov-file#install-and-run-gui-apps
To build with the image:  
```
docker build -t music https://raw.githubusercontent.com/69pmb/AllMusic/feat/docker/docker/Dockerfile
```
To run it:  
```
docker run -it -v /tmp/.X11-unix:/tmp/.X11-unix -e DISPLAY=$DISPLAY music
```
