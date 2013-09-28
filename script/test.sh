for file in `find ./ -name "*"`
do
	echo $file
done

if ( grep "end;" java_close.m>/dev/null )
then
	echo "ok"
else
	echo "not ok"
fi

file=1;
file2=1;
if [ $file -eq 1 ]
then echo "yes";
else echo "no";
fi
