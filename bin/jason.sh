#!/bin/sh

JASON_HOME=`dirname $0`
cd "$JASON_HOME/.."
JASON_HOME=`pwd`
cd "$JASON_HOME/bin"

if [ -z $SACI_HOME ] ; then
	export SACI_HOME=$JASON_HOME/lib/saci
fi

# check SACI_HOME
if [ ! -f $SACI_HOME/bin/saci.jar ] ; then
   echo SACI_HOME is not properly set!
fi

OS=`uname`
if [ -z $JAVA_HOME ] ; then
	if [ $OS == Darwin ] ; then
		JAVA_HOME=/usr
	fi
	if [ $OS == Linux ] ; then
		JAVA_HOME=/usr/local/j2sdk1.4
	fi
fi
# check JAVA_HOME
if [ ! -f $JAVA_HOME/bin/javac ] ; then
   echo JAVA_HOME is not properly set!
fi

export PATH="$JAVA_HOME/bin":$PATH

DPAR=""
if [ $OS == Darwin ] ; then
	DPAR="-Dapple.laf.useScreenMenuBar=true"
fi

# run jIDE
java -classpath "$JASON_HOME/bin/jason.jar":"$SACI_HOME/bin/saci.jar":"$JASON_HOME/lib/log4j.jar"  $DPAR jIDE.JasonID $1
