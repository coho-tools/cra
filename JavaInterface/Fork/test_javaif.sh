#/bin/sh
echo "****This script is to test the pipe between JAVA and Matlab works correctly****"
echo "Please make sure Java/C codes compiled,  and the Java thread works correctly."

echo "Start Java program ..."
../Fork/fork -c "java -cp ../../Java/lib/cup.jar:../../Java/bin/coho.jar coho.interp.MyParser < ../../Java/test/m2j.log  > ../../Java/test/j2m.log"

echo "Wait 2 seconds for results returning from JAVA"
sleep 2

echo "Compare results ..."
diff ../../Java/test/j2m.log ../../Java/test/j2m.ref
rm ../../Java/test/j2m.log

echo "The pipe works correctly if no error above" 
