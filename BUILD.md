# Build Instructions

## I Just want to play the game
* [It's on Steam](https://store.steampowered.com/app/1660910/Tanks_The_Crusades/)
* [Latest Release](https://github.com/aehmttw/tanks/releases)

## Dependencies
* [Java](https://www.java.com/en/)
* [Maven](https://maven.apache.org/)

## Building with make
This is a wrapper for building with maven **YOU STILL NEED MAVEN**
### Initialize the environment
```
make init
```

### Package & run
```
make
```

### Package
```
make package
```

### Run
```
make run
```

### clean
```
make clean
```

## Building with Maven

### Installing Steamworks
```
mvn install:install-file    -Dfile=libs/steamworks4j-1.10 0-SNAPSHOT.jar    -DgroupId=com.code-disaster.steamworks4j    -DartifactId=steamworks4j    -Dversion=1.10.0-SNAPSHOT    -Dpackaging=jar    -DgeneratePom=true
mvn install:install-file    -Dfile=libs/steamworks4j-lwjgl3-1.10.0-SNAPSHOT.jar    -DgroupId=com.code-disaster.steamworks4j    -DartifactId=steamworks4j-lwjgl3    -Dversion=1.10.0-SNAPSHOT    -Dpackaging=jar    -DgeneratePom=true
```
Steamworks4J has had some issues with its maven repository. As a fix (for now), steamworks4J is bundled with Tanks.

### Packaging
```
mvn clean package
```

### Running
```
java -jar target/Tanks.jar
```

### Clean
```
mvn clean
```


