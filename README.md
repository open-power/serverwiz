# Serverwiz #


## Building ##

Serverwiz uses an ant build script.

To start a build:

    cd <repodir>
    ant -f build.xml clean compile jar
    
The jars are built in the ./build directory.    

The OS specific jar can be ran directly.  In order to do this, there must be a
./xml directory from where java is executed that contains the library files.
So if you are in the repository directory, you can run:

	java -jar build/serverwiz2_[OS].jar
	
where OS is linux64, macosx64, win32, or win64.


serverwiz2.jar is the main launcher jar.  This jar checks the OS and downloads
the proper OS specific jar from the latest github release.  It also downloads
the XML library files.   It will not use the jar that was built locally unless using 
the -d flag.  

To run:

	java -jar serverwiz2.jar

	