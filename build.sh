#!/bin/bash

#Check for JRE

echo "Searching for JRE..."
if type -p java; then
    echo "FOUND!"
    _java=java  
elif [[ -n $JAVA_HOME ]] && [[ -x "$JAVA_HOME/bin/java" ]];  then
    echo "FOUND!"
    _java="$JAVA_HOME/bin/java"
else
    echo "No JRE found, read BUILD.md for instructions"
    exit 1
fi

#Check for JDK

echo "Searching for JDK..."
if type -p javac; then
    echo "FOUND!"
    _java=java  
elif [[ -n $JAVA_HOME ]] && [[ -x "$JAVA_HOME/bin/javac" ]];  then
    echo "FOUND!"
    _java="$JAVA_HOME/bin/java"
else
    echo "No JDK found, read BUILD.md for instructions"
    exit 1
fi

#Check for Depedencies

echo "Searching for Dependencies..."
if [ -d "libs" ]; then
    echo "FOUND!"
else
    echo "Fetching dependencies from maven..."
    mvn dependency:copy-dependencies -DoutputDirectory=./libs
    if [ -d "libs" ]; then
        echo "DOWNLOADED!"
    else
        echo "DOWNLOAD FAILED"
    fi
fi

#Actual Script

mkdir build
cd build
echo "Extracting Dependencies..."
find ../libs -name "*.jar" -exec jar -xf {} \;
echo "Compiling Source..."
cp -r ../src/main/java/* .
cp -r ../src/main/resources/* .
javac ./main/Tanks.java
rm -rf *.java
echo "Packaging Game..."
jar -cfm Tanks.jar ./META-INF/MANIFEST.MF *
mv Tanks.jar ../Tanks.jar
cd ..
echo "Cleaning Up..."
rm -rf build
echo "Done!"
