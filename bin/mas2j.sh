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

java -classpath $JASONDIR/bin/jason.jar:$JASONDIR/lib/saci.jar:"$JASONDIR/lib/log4j.jar":. jason.mas2j.parser.mas2j $1

chmod u+x *.sh
