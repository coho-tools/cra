
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
  % NOTE: I use global vars because of the Matlab bug. 
	%       (When the code is in linked dir, persistent vars are re-inited 
	%        when firstly changing to a new directory). 
  %       Please don't modify the value by other functions. 
  %persistent  CRA_INFO;
  global CRA_INFO;
  if(isempty(CRA_INFO)) 
    CRA_INFO = cra_info_init; % evaluate once
  end
  if(nargin<1||isempty(field))
    val = CRA_INFO;
  else
    val = CRA_INFO.(field);
  end; 
end
function  info = cra_info_init
  % CRA root path
  cra_home='/ubc/cs/research/scl/people/inutard/cra'; 

  % CRA directories 
  cra_dirs = {
    'HybridAutomata',
    'Projectagon',
    'Projectagon/Ph',
    'Projectagon/Forward',
    'ConvexPh',
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
    'Utils/Logger'};

  % cplex is avail in the system
  has_cplex = 0; 
  
  % current user
  [~,user] = unix('whoami');
  user = user(1:end-1);

	% path to save CRA system data or files
  sys_path = ['/var/tmp/',user,'/coho/cra/sys']; 

  % JAVA java_classpath
  java_classpath = [cra_home,'/Java/lib/cup.jar',':',cra_home,'/Java/bin/coho.jar'];
  fork_bin = [cra_home,'/JavaInterface/Fork/fork'];

  version = 1.1;
  license = 'bsd';
  
  info = struct('version',version, 'license',license, ...
                'cra_dirs',{cra_dirs}, 'cra_home',cra_home, ...
                'user',user, 'has_cplex', has_cplex, ...
                'sys_path', sys_path, 'fork_bin', fork_bin, ...
                'java_classpath', java_classpath); 
end % cra_info

