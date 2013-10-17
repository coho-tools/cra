#!/bin/sh

keyword=$1;
if [ "$2" ]
then 
	prjDir=$2;
else
	prjDir='.';
fi

cmd="find $prjDir/ -name \"*.m\"";
cmd="grep $keyword \`$cmd\`" 
echo $cmd
eval $cmd
