#!/bin/bash
mkdir build
cd build
find ../libs -name "*.jar" -exec jar -xfv {} \;
cp -rv ../src/main/java/ .
cp -rv ../src/main/resources/ .
javac ./main/Tanks.java
rm -rfv *.java
jar -cfmv Tanks.jar ./META-INF/MANIFEST.MF *
mv Tanks.jar ../Tanks.jar
cd ..
rm -rf build
