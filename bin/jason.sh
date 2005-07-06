#!/bin/sh

JASON_HOME=`dirname $0`
cd "$JASON_HOME/.."
JASON_HOME=`pwd`
cd "$JASON_HOME/bin"

# check current dir
if [ ! -f ./jason.sh ] ; then
   echo
   echo Current directory is not Jason/bin,
   echo Jason must be executed in this directory.
   exit
fi


if [ -z $SACI_HOME ] ; then
	export SACI_HOME=../lib/saci
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
java -classpath "$JASON_HOME/bin/jason.jar":"$SACI_HOME/bin/saci.jar":"$JASON_HOME/lib/log4j.jar"  $DPAR jIDE.JasonID "$SACI_HOME" "$JAVA_HOME" $1

# if the jIDE identified the SACI home
if [ $? = '1' ] ; then
   #grep -v "^export SACI_HOME" jason.sh > x
   #grep -v "^export JAVA_HOME" x >> setenv.sh
   #rm x
   mv jason.sh jason.sh.old
   chmod u+x setenv.sh
   mv setenv.sh jason.sh
   ./jason.sh $1
fi
