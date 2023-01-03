#Check for JRE
echo "Searching for JRE..."
if(java --version -And jar --version){
    echo "FOUND!"
}else{
    echo "No JRE found, read BUILD.md for instructions"
    exit 1
}

#Check for JDK

echo "Searching for JDK..."
if(javac --version){
    echo "FOUND!"
}else
{
    echo "No JDK found, read BUILD.md for instructions"
    exit 1
}

#Check for Depedencies

echo "Searching for Dependencies..."
if(Test-Path "libs")
{
    echo "FOUND!"
}else{
    echo "Fetching dependencies from maven..."
    mvn dependency:copy-dependencies -DoutputDirectory=./libs
    if(Test-Path "libs")
    {
        echo "DOWNLOADED!"
    }
    else
    {
        echo "DOWNLOAD FAILED"
    }
}

#Actual Script

mkdir build
cd build
echo "Extracting Dependencies..."
$jars = find ../libs -name "*.jar"
foreach($jar in $jars){
    jar -xf $jar
}
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