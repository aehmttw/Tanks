
# Build Instructions

## Installing Java JDK

Download and install java from the source:
[https://www.oracle.com/java/technologies/downloads/](https://www.oracle.com/java/technologies/downloads/)

Other version of Java 8+ will also work.

## Downloading Dependencies

Outside of the Tanks directory run
```bash 
git clone https://github.com/ghostlypi/Tanks-Libs.git
mv Tanks-Libs/libs.zip Tanks
cd Tanks
unzip libs.zip
```
## Building the Game

### Windows
Run the file `build.ps1`

### Mac OS/Linux
Opening terminal to the current directory, run `bash build.sh`
