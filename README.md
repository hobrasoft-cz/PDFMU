# PDF Manipulation Utility

This project is maintained by [Hobrasoft s.r.o.](http://www.hobrasoft.cz/)

Homepage: [http://hobrasoft-cz.github.io/PDFMU/](http://hobrasoft-cz.github.io/PDFMU/)

## Quick start in Windows

1. Download and install [JDK 7u80](http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html)
2. Download and install [NetBeans 8.1](https://netbeans.org/downloads/)
3. Run NetBeans
4. File > Open Project > "." (the root directory of this repository)
5. Run > Build Project (PDF Manipulation Utility)

An executable will be generated in "target/exe/pdfmu.exe".
Go to "target/exe" and run `pdfmu --help` to see the available operations.

## Software dependencies

* [JDK 7u80](http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html)
* [NetBeans 8.1](https://netbeans.org/downloads/) (optional)
* [Maven](http://maven.apache.org/download.cgi) 3.3.3 (part of NetBeans)

## Actions

All the paths mentioned below are relative to the repository root.

### Build for Windows using NetBeans

1. Run NetBeans
2. File > Open Project > "."
3. Run > Build Project (pdfmu)

The resulting file is "target/exe/pdfmu.exe".

### Build for Windows using Maven

1. Go to "." in command line
2. `mvn package`

The resulting file is "target/exe/pdfmu.exe".

### Build a self-contained JAR using Maven

1. Go to "." in command line
2. `mvn package assembly:single`

The resulting file is "target/pdfmu-${version}-jar-with-dependencies.jar".

### Run the self-contained jar

1. Build the self-contained JAR (see above)
2. Go to "target" in command line
3. `java -jar pdfmu-${version}-jar-with-dependencies.jar`

### Generate documentation using Maven and javadoc

1. Go to "." in command line
2. `mvn javadoc:javadoc`

To inspect the resulting documentation open "target/site/apidocs/index.html".

## Options

### SSL

Information on the web:

* [Generating a KeyStore and TrustStore](http://docs.oracle.com/cd/E19509-01/820-3503/6nf1il6er/index.html)
* [Configuring JSSE System Properties](https://access.redhat.com/documentation/en-US/Fuse_MQ_Enterprise/7.1/html/Security_Guide/files/SSL-SysProps.html)

#### TrustStore

Only the types JKS and JCEKS (not PKCS #12) are supported for TrustStore
(`--ssl-truststore-type`).
You do not need to specify the password (`--ssl-truststore-password`) –
it seems that the CA certificates in the keystore are not password protected.

#### KeyStore

The KeyStore must be protected by a non-empty password
(`--ssl-keystore-password`).
All the private keys in a JKS or JCEKS KeyStore must be protected by the same password as the KeyStore.

## Copyright and license

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
can be inspected in the file `LICENSE.txt`.
