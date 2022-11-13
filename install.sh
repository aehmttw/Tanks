#!/bin/bash
if uname -a | grep -q 'Darwin'; then
    if uname -a | grep -q "arm64"; then
      echo 'Installing for Darwin arm64...'
      curl 'https://download.java.net/java/GA/jdk17.0.2/dfd4a8d0985749f896bed50d7138ee7f/8/GPL/openjdk-17.0.2_macos-aarch64_bin.tar.gz' -o 'java.tar.gz'
      tar xf java.tar.gz
      rm java.tar.gz
      echo './jdk-17.0.2.jdk/Contents/Home/bin/java -XstartOnFirstThread -jar Tanks.jar mac' > run.sh
      chmod +x run.sh
      echo 'Run ./run.sh to run Tanks!'
    else
      echo 'Installing for Darwin x86...'
      curl 'https://download.java.net/java/GA/jdk17.0.2/dfd4a8d0985749f896bed50d7138ee7f/8/GPL/openjdk-17.0.2_macos-x64_bin.tar.gz' -o 'java.tar.gz'
      tar xf java.tar.gz
      rm java.tar.gz
      echo './jdk-17.0.2.jdk/Contents/Home/bin/java -XstartOnFirstThread -jar Tanks.jar mac' > run.sh
      chmod +x run.sh
      echo 'Run ./run.sh to run Tanks!'
    fi
elif uname -a | grep -q 'Linux'; then
    if uname -a | grep -q 'x86'; then
      echo 'Installing for Linux x86...'
      curl 'https://download.java.net/java/GA/jdk17.0.2/dfd4a8d0985749f896bed50d7138ee7f/8/GPL/openjdk-17.0.2_linux-x64_bin.tar.gz' -o 'java.tar.gz'
      tar xf java.tar.gz
      rm java.tar.gz
      echo './jdk-17.0.2/bin/java -XstartOnFirstThread -jar Tanks.jar' > run.sh
      chmod +x run.sh
      echo 'Run ./run.sh to run Tanks!'
    else
      echo 'Installing for Linux arm64...'
            curl 'https://download.java.net/java/GA/jdk17.0.2/dfd4a8d0985749f896bed50d7138ee7f/8/GPL/openjdk-17.0.2_linux-aarch64_bin.tar.gz' -o 'java.tar.gz'
            tar xf java.tar.gz
            rm java.tar.gz
            echo './jdk-17.0.2/bin/java -XstartOnFirstThread -jar Tanks.jar' > run.sh
            chmod +x run.sh
            echo 'Run ./run.sh to run Tanks!'
    fi
fi