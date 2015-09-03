# PDF Manipulation Utility

## Quick start

1. Install [JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html) 8u60
2. Install [NetBeans](https://netbeans.org/downloads/) 8.0.2
3. Run NetBeans
4. File > Open Project > "pdfmu" (the directory (Project Folder) in this repository)
5. Run > Run Project (pdfmu)

## Software dependencies

* [JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html) 8u60
* [NetBeans](https://netbeans.org/downloads/) 8.0.2
* [Doxygen](http://www.stack.nl/~dimitri/doxygen/) 1.8.10

## Actions

All the paths below are relative to the repository root.

### Run from command line

1. Open the "pdfmu" project in NetBeans (see above)
2. Run > Build Project (pdfmu)
3. Go to "pdfmu/target" in command line
4. `java -jar pdfmu-1.0-SNAPSHOT-jar-with-dependencies.jar`

### Generate documentation using Doxygen

1. Install [Doxygen](http://www.stack.nl/~dimitri/doxygen/) 1.8.10
2. Run `doxygen Doxyfile` in repository root
3. Open `doxygen/html/index.html`
