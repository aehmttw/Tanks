all: package run

first: init package

run:
	java -jar target/Tanks.jar

clean:
	mvn clean

init:
	mvn install:install-file    -Dfile=libs/steamworks4j-1.10.0-SNAPSHOT.jar    -DgroupId=com.code-disaster.steamworks4j    -DartifactId=steamworks4j    -Dversion=1.10.0-SNAPSHOT    -Dpackaging=jar    -DgeneratePom=true
	mvn install:install-file    -Dfile=libs/steamworks4j-lwjgl3-1.10.0-SNAPSHOT.jar    -DgroupId=com.code-disaster.steamworks4j    -DartifactId=steamworks4j-lwjgl3    -Dversion=1.10.0-SNAPSHOT    -Dpackaging=jar    -DgeneratePom=true

package:
	mvn clean package