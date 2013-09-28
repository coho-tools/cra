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


echo "====Step5: Generating CRA configuration file ====" 
cd $CRA_HOME
while true; do 
	read -p "Is CPLEX LP solver available in your system? " yn 
	case $yn in 
		[Yy]* ) has_cplex=1; break;; 
	  [Nn]* ) has_cplex=0; break;;
	  * ) echo "Please answer yes or no.";; 
	esac 
done

echo "
% function val = cra_info(field)
%   This function returns read-only information for CRA, including: 
%     cra_home:  root path of CAR software 
%     cra_dirs:  all CRA dirs to be added in Matlab
%     user:      current user
%     has_cplex: CPLEX LP solver is available or not in the system 
%     java_classpath, fork_bin: use for create pipe between Matlab & Java
%     version:   CRA version
%     license:   CRA license
%  Ex: 
%     info = cra_info;  // return the structure
%     has_cplex = cra_info('has_cplex'); // has the value
function val = cra_info(field)
  % NOTE: I use global vars because of the Matlab 2013 bug. 
	%       (Persistent vars re-inited upon the first path changes)
  %       Please don't modify the value by other functions. 
  %persistent  CRA_INFO;
  global CRA_INFO;
  if(isempty(CRA_INFO)) 
    CRA_INFO = cra_info_init; % evaluate once
    disp('init info')
  end
  if(nargin<1||isempty(field))
    val = CRA_INFO;
  else
    val = CRA_INFO.(field);
  end; 
end
function  info = cra_info_init
  % CRA root path
  cra_home='`pwd`'; 

  % CRA directories 
  cra_dirs = {
    'HybridAutomata',
    'Projectagon',
    'Projectagon/Ph',
    'Projectagon/Forward',
    'Projectagon/Utils',
    'JavaInterface',
    'JavaInterface/Fork',
    'JavaInterface/Base',
    'LinearProgramming',
    'LinearProgramming/Project',
    'LinearProgramming/Solver',
    'Polygon',
    'Polygon/SAGA',
    'Integrator',
    'Utils',
    'Utils/BoundingBox',
    'Utils/Preprocessor',
    'Utils/Logger'};

  % cplex is avail in the system
  has_cplex = $has_cplex; 
  
  % current user
  [~,user] = unix('whoami');
  user = user(1:end-1);

	% path to save CRA system data or files
  sys_path = ['/var/tmp/',user,'/coho/cra/sys']; 

  % JAVA java_classpath
  java_classpath = [cra_home,'/Java/lib/cup.jar',':',cra_home,'/Java/bin/coho.jar'];
  fork_bin = [cra_home,'/JavaInterface/Fork/fork'];

  version = 1.0;
  license = 'bsd';
  
  info = struct('version',version, 'license',license, ...
                'cra_dirs',{cra_dirs}, 'cra_home',cra_home, ...
                'user',user, 'has_cplex', has_cplex, ...
                'sys_path', sys_path, 'fork_bin', fork_bin, ...
                'java_classpath', java_classpath); 
end % cra_info
" > cra_info.m

#sed -e "s/has_cplex =.*;/has_cplex = $has_cplex;/g" foo > cra_info.m
#cp cra_info.m foo
#echo "Config CRA_HOME as `pwd`"
#sed -e "s#cra_home = '.*';#cra_home='`pwd`';#g" foo > cra_info.m
#rm foo
echo "You can update the configurations later by editing cra_info.m file."

echo ""
echo "COHO Reachabilty Analysis Tool Installed!" 
echo "Please set environment variable CRA_HOME to install path before using CRA". 
echo ""
