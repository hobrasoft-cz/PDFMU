# PDF Manipulation Utility

## Quick start

1. Install [JDK](http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html) 7u80
2. Install [NetBeans](http://download.netbeans.org/netbeans/8.1/beta/) 8.1 Beta
3. Run NetBeans
4. File > Open Project > "pdfmu" (the directory (Project Folder) in this repository)
5. Run > Run Project (pdfmu)

## Software dependencies

* [JDK](http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html) 7u80
* [NetBeans](http://download.netbeans.org/netbeans/8.1/beta/) 8.1 Beta
* [Maven](http://maven.apache.org/download.cgi) 3.3.3
* [Doxygen](http://www.stack.nl/~dimitri/doxygen/) 1.8.10

## Actions

All the paths below are relative to the repository root.

### Build using NetBeans

1. Run NetBeans
2. File > Open Project > "pdfmu"
3. Run > Build Project (pdfmu)

The resulting file is "pdfmu/target/pdfmu-1.0-SNAPSHOT-jar-with-dependencies.jar".

### Build using Maven

1. Go to "pdfmu" in command line
2. `mvn install`

The resulting file is "pdfmu/target/pdfmu-1.0-SNAPSHOT-jar-with-dependencies.jar".

### Run from command line

1. Build (see above)
2. Go to "pdfmu/target" in command line
3. `java -jar pdfmu-1.0-SNAPSHOT-jar-with-dependencies.jar`

### Generate documentation using Doxygen

1. Go to "." in command line
2. `doxygen Doxyfile`

To inspect the resulting documentation open "doxygen/html/index.html".
