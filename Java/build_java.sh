#!/bin/sh

echo "Compiling JAVA files ..."
javac -classpath ./lib/cup.jar -sourcepath src -d bin src/coho/interp/MyParser.java

echo "Creating JAR file..."
cd bin
jar cf coho.jar coho
rm -rf coho
cd ..

