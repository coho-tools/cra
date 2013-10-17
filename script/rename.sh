#!/bin/sh
# This script rename a matlab function  
oldname=$1; newname=$2;
if [ "$3" ]
then
	dir=$3;
else
	dir=".";
fi;
if [ "$4" ]
then 
	prjDir=$4;
else
	prjDir='.';
fi

echo "#step1. checking if $dir/$newname.m exists ..." 
if [ -e $dir/$oldname.m ]
then
	:
else
	echo "$dir/$oldname.m does not exist, try another name";
	exit 1;
fi
if [ -e $dir/$newname.m ]
then
	echo "$dir/$newname.m exists, try another name";
	exit 1;
fi
echo 

echo "#step2. checking if $newname is used in any matlab files under $prjDir/ ..."
cmd="find $prjDir/ -name \"*.m\"";
find=0;
for file in `eval $cmd` 
do 
	cmd="grep \"\\<$newname\\>\" $file > /dev/null";
	if ( eval $cmd )
	then
		echo "find $newname in $file";
		find=1;
	fi
done;
if [ $find -eq 1 ]
then
	echo "please fixed the value and try again"
	exit 1;
fi;
echo 


echo "#step3. move $dir/$oldname.m to $dir/$newname.m ..." 
cmd="mv $dir/$oldname.m $dir/$newname.m"
eval $cmd;

echo "#step4. repalce $oldname with $newname in all matlab files under $prjDir/ ..."
cmd="find $prjDir/ -name \"*.m\"";

for file in `eval $cmd` 
do 
	cmd="grep \"\\<$oldname\\>\" $file > /dev/null;";
	if ( eval $cmd )
	then
		echo 
		cmd="cp $file $file.org"
		echo $cmd;
		cmd="sed -e 's/\\<$oldname\\>/$newname/g' $file.org >$file"
		echo $cmd;
		cmd="rm $file.org";
		echo $cmd;
	fi
done;
