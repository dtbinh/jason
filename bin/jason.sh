#!/bin/sh

CURDIR=`pwd`
JASON_HOME=`dirname $0`
cd "$JASON_HOME/.."
JASON_HOME=`pwd`
#cd "$JASON_HOME/bin"
cd $CURDIR

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
java -classpath "$JASON_HOME/lib/jason.jar":"$JASON_HOME/bin/jedit/jedit.jar":"$JASON_HOME/lib/saci.jar"  $DPAR org.gjt.sp.jedit.jEdit $1
#"$JASON_HOME/lib/ant.jar":"$JASON_HOME/lib/ant-launcher.jar":
#-settings=$JASON_HOME/bin/.jedit 
