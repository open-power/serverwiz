# Serverwiz #


## Building ##

Serverwiz uses an ant build script.

To start a build:

    cd <repodir>
    ant -f build.xml clean compile jar
    
The jars are built in the ./build directory.    

The OS specific jar can be ran directly.  In order to do this, there
must be a ./xml directory from where java is executed that contains
the library files.  So if you are in the repository directory, you 
can run:

	java -jar build/serverwiz2_[OS].jar
	
where OS is linux64, macosx64, win32, or win64.

Important: The first time Serverwiz is ran, it will ask you for your 
local git repository location.  It will look for common-mrw-xml repository 
in that directory.  If it does not exist, it will clone the repository from:

	https://github.com/open-power/common-mrw-xml.git

This is the XML based library that contains attribute and target
definitions.   The library can be updated to latest by using
"Manage Library" button.

## Update mode in serverwiz ##
There is an update mode in the Serverwiz tool. It is used to update an MRW XML
with new attributes and target XMLs. This mode does not load the GUI. It only
takes the attributes, targets and parts XMLs. Opens the input MRW file, updates
it and saves it to output file.
In order to do this, there must be a ./xml directory from where java is executed
that contains the library files.  So if you are in the repository directory, you
can run:

	java -jar build/serverwiz2_[OS].jar -u -i <input MRW file name>
		-o <output MRW file name>
	
In update mode, it always looks for common-mrw-xml repository in the current
directory. If it does not exist, it will clone the repository from:

	https://github.com/open-power/common-mrw-xml.git

If additional libraries need to be added, then add repository details to the
serverwiz.preference file.

## Precompiled jars ##

There are precompiled binaries at:

	https://github.com/open-power/serverwiz/releases
	
Download serverwiz2.jar which is the main launcher application.  This
application checks the OS and downloads the proper OS specific jar from
the latest github release.  It will not use the jar that was built locally
unless using the -d flag is used.  

To run:

	java -jar serverwiz2.jar

	