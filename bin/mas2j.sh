#!/bin/sh

if [ ! -f $1 ]
then
    echo File $1 not found
    exit
fi


CURDIR=`pwd`

JASONDIR=`dirname $0`/..
cd $JASONDIR
JASONDIR=`pwd`

cd $CURDIR

if [ -z $SACI_HOME ] ; then
	export SACI_HOME=$JASONDIR/lib/saci
fi

java -classpath $JASONDIR/bin/jason.jar:$SACI_HOME/bin/saci.jar:$JASONDIR/lib/log4j.jar:. jIDE.parser.mas2j $1 $JASONDIR $SACI_HOME

chmod u+x *.sh

if [ -f ./compile.sh ]
then
    ./compile.sh
fi
