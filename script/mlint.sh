#!/bin/sh
DIR=$1;
CMD="";
for file in `ls $DIR/*.m`
do
	#echo $file;
	CMD=$CMD"disp('$file');mlint $file;";
done;
CMD=$CMD"exit;";
#CMD="matlab -nodisplay -nojvm -logfile $DIR/mlint.log -r \""$CMD"\"";
#echo $CMD
matlab -nodisplay -nojvm -logfile $DIR/mlint.log -r "$CMD";

#	matlab -nodisplay -r "mlint $file >> $DIR/log"; 
