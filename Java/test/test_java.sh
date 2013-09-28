#/bin/sh

echo "****This script is to test JAVA thread works correctly****" 
java -cp ../bin/coho.jar:../lib/cup.jar coho.interp.MyParser < m2j.log > j2m.log

echo "Compare results ..."
diff j2m.log j2m.ref

echo "The Java thread works correctly if no error above" 
