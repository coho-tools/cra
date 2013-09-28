#!/bin/sh
# This script replace oldvar with newvar in all files under dir
oldvar=$1; newvar=$2; dir=$3; 
: ${dir:="."}; 
if [ "$3" ]
then 
	dir=$3;
else
	dir='.';
fi

cmd="find $dir/ -name \"*.m\"";

for file in `eval $cmd` 
do 
	cmd="grep \"\\<$oldvar\\>\" $file >/dev/null";
	if  (eval $cmd) 
	then
		echo "";
		cmd="cp $file $file.org"
		echo $cmd
		cmd="sed -e 's/\\<$oldvar\\>/$newvar/g' $file.org >$file"
		echo $cmd
		cmd="rm $file.org"
		echo $cmd;
	fi
done;

