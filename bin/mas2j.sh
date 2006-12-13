#!/bin/sh
#
# this script creates the Ant script (build.xml) 
# to run a Jason project  
#

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

java -classpath $JASONDIR/lib/jason.jar:$JASONDIR/lib/saci.jar:. jason.mas2j.parser.mas2j $1

#chmod u+x *.sh
