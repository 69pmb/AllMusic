# AllMusic
### Purposes of AllMusic:

The main goal is to discover the very best of music according of publications like NME, Pitchfork, Rolling Stones...

It can import, convert files of  songs or albums into XML files.

A datable file like is then created (the `final.xml` file).

You can search throught this database/file by artists, titles, dates or types (song or album) for instance.

You can browse by files and also create annual ranking by song, album and artist.

A file property can be configured to define directory for the various files of the application. It can be found there:  
` AllMusic/src/main/resources/config.properties`
### To run AllMusic:

- Install Java 8, Maven and Git.
- In your workspace (in command line):
  - Run `git clone https://github.com/69pmb/AllMusic.git` to checkout the sources.
  - Go in the `AllMusic` folder.
  - Run `mvn install` to download the dependencies.
  - Finally `mvn exec:java` to launch AllMusic.

