# PDF Manipulation Utility

## Quick start

1. Install [JDK 7u80](http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html)
2. Install [NetBeans 8.1 Beta](http://download.netbeans.org/netbeans/8.1/beta/)
3. Run NetBeans
4. File > Open Project > "pdfmu" (the directory (Project Folder) in this repository)
5. Run > Run Project (pdfmu)

## Software dependencies

* [JDK 7u80](http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html)
* [NetBeans 8.1](https://netbeans.org/downloads/) (optional)
* [Maven](http://maven.apache.org/download.cgi) 3.3.3 (part of NetBeans)
* [Doxygen](http://www.stack.nl/~dimitri/doxygen/) 1.8.10 (optional)

## Actions

All the paths mentioned below are relative to the repository root.

### Build using NetBeans

1. Run NetBeans
2. File > Open Project > "pdfmu"
3. Run > Build Project (pdfmu)

The resulting file is "pdfmu/target/pdfmu-1.0-SNAPSHOT-jar-with-dependencies.jar".

### Build using Maven

1. Go to "pdfmu" in command line
2. `mvn package`

The resulting file is "pdfmu/target/pdfmu-1.0-SNAPSHOT-jar-with-dependencies.jar".

### Run from command line

1. Build (see above)
2. Go to "pdfmu/target" in command line
3. `java -jar pdfmu-1.0-SNAPSHOT-jar-with-dependencies.jar`

### Generate documentation using Maven and javadoc

1. Go to "pdfmu" in command line
2. `mvn javadoc:javadoc`

To inspect the resulting documentation open "pdfmu/target/site/apidocs/index.html".

### Generate documentation using Doxygen

1. Go to "." in command line
2. `doxygen Doxyfile`

To inspect the resulting documentation open "doxygen/html/index.html".

# Options

## SSL

Information on the web:

* [Generating a KeyStore and TrustStore](http://docs.oracle.com/cd/E19509-01/820-3503/6nf1il6er/index.html)
* [Configuring JSSE System Properties](https://access.redhat.com/documentation/en-US/Fuse_MQ_Enterprise/7.1/html/Security_Guide/files/SSL-SysProps.html)

### TrustStore

Only the types JKS and JCEKS (not PKCS #12) are supported for TrustStore (`--ssl-truststore-type`).
You do not need to specify the password (`--ssl-truststore-password`) – it seems that the CA certificates in the keystore are not password protected.

### KeyStore

The KeyStore must be protected by a non-empty password (`--ssl-keystore-password`).
All the private keys in a JKS or JCEKS KeyStore must be protected by the same password as the KeyStore.

## Development

### How do I change the version of PDFMU?

Update the following values in "pdfmu/pom.xml":

* `project.version`
* `project.properties.exeVersion`

### How do I change the license header?

Use the NetBeans plugin [License Changer](http://plugins.netbeans.org/plugin/17960/license-changer) to update the license header including the copyright line in the source files.
Version 1.9.2 of the plugin contains a bug that makes it generate CRLF instead of LF line endings.
To fix the line endings,
use the utility `dox2unix`.
To run it on all the source files, execute the following command in the directory "pdfmu/src":
`find -type f -exec dos2unix {} \;`

### How do I update the copyright?

Update the value `project.properties.copyright` in "pdfmu/pom.xml".
Update the license header according to the instructions in the previous section,
since the license header includes a copyright line.

## License

```
Copyright (C) 2016 Hobrasoft s.r.o.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
```

The full text of the GNU Affero General Public License
can be inspected in the file `agpl-3.0.txt`.
