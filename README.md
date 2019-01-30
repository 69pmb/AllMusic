[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=69pmb_AllMusic&metric=alert_status)](https://sonarcloud.io/dashboard?id=69pmb_AllMusic)
# AllMusic
### Purposes of AllMusic:

The main goal is to discover the very best of music according of publications like NME, Pitchfork, Rolling Stones...

It can import, convert files of  songs or albums into XML files.

A datable file like is then created (the `final.xml` file).

You can search throught this database/file by artists, titles, dates or types (song or album) for instance.

You can browse by files and also create annual ranking by song, album and artist.

### Configuration:

A property file is used to define directories for the various files of the application. It can be found there:  
` AllMusic/src/main/resources/config.properties`

* resources: absolute path of the resources folder, where all files will be found

* output: absolute path of the folder where files will be generated 

* final: name of the file that's hold all the compositions

* music: name of the folder of the txt files

* notepad: absolute path of the nopad++.exe

* excel: absolute path of the excel.exe

* xml: name of the folder of the xml files

### To run AllMusic:

- Install Java 8, Maven and Git.
- In your workspace (in command line):
  - Run `git clone https://github.com/69pmb/AllMusic.git` to checkout the sources.
  - Go in the `AllMusic` folder.
  - Run `mvn install` to download the dependencies.
  - Finally `mvn exec:java` to launch AllMusic.

