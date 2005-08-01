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

java -classpath $JASONDIR/bin/jason.jar:$JASONDIR/lib/saci.jar:"$JASONDIR/lib/log4j.jar":. jIDE.parser.mas2j $1 $JASONDIR

chmod u+x *.sh
