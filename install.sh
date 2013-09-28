#/bin/sh
echo ""
echo "Installing COHO Reachabilty Analysis Tool ......" 
echo ""

echo "====Step0: Set environment variable CRA_HOME====" 
CRA_HOME=`pwd`
export CRA_HOME
echo $CRA_HOME
echo "" 

echo "====Step1: Compile JAVA codes====" 
cd $CRA_HOME/Java
build_java.sh
echo "" 

echo "====Step2: Check the Java thread works correctly====" 
cd $CRA_HOME/Java/test 
test_java.sh
echo "" 

echo "====Step3: Comple C codes====" 
cd $CRA_HOME/JavaInterface/Fork
echo "Compile C files ..."
make
echo "" 

echo "====Step4: Check the pipe between Java and Matlab thread works correctly====" 
cd $CRA_HOME/JavaInterface/Fork/
test_javaif.sh
echo "" 


echo "====Step5: We still have some questions to configurate CRA====" 
cd $CRA_HOME
cp cra_info.m foo
while true; do 
	read -p "Is CPLEX LP solver available in your system? " yn 
	case $yn in 
		[Yy]* ) has_cplex=1; break;; 
	  [Nn]* ) has_cplex=0; break;;
	  * ) echo "Please answer yes or no.";; 
	esac 
done
sed -e "s/has_cplex =.*;/has_cplex = $has_cplex;/g" foo > cra_info.m
cp cra_info.m foo
echo "Config CRA_HOME as `pwd`"
sed -e "s#cra_home = '.*';#cra_home='`pwd`';#g" foo > cra_info.m
rm foo
echo "You can update the configurations later by editing cra_info.m file."

echo ""
echo "COHO Reachabilty Analysis Tool Installed!" 
echo "Please set environment variable CRA_HOME to install path before using CRA". 
echo ""
