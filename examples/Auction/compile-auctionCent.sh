#!/bin/sh

# this file was generated by mas2j parser

echo -n "        compiling user classes..."
javac -classpath .:"/Users/jomi/programming/agents/Jason/bin/jason.jar":"/Users/jomi/programming/agents/Jason/lib/saci/bin/saci.jar":"":"$CLASSPATH"  ./*.java


echo ok
