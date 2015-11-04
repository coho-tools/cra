function [javaIn, javaOut] = java_open(debug)
% JAVA = java_open
%
% creates global file handles for pipes to and from the java CG engine
%
% the pipe to the engine is called javaIn (write to this pipe)
% the pipe from the engine is called javaOut (read from this pipe)
%
if(nargin<1||isempty(debug))
	debug = 0;
end
if( (cra_cfg('has','javaIn') && ~isempty(cra_cfg('get','javaIn'))) || ... 
	  (cra_cfg('has','javaOut') && ~isempty(cra_cfg('get','javaOut'))) )
	java_close; % close old one first;
end

jNum = cra_cfg('get','javaThreads'); 

fork_bin = cra_info('fork_bin');
java_classpath = cra_info('java_classpath'); 
% sys_path= cra_info('sys_path');

% create a dir 
threadPath = cra_cfg('get','threadPath');

fprintf('Creating Java Threads: ')
javaIn = zeros(jNum,1); javaOut = zeros(jNum,1);
for i=1:jNum
  fprintf('  %i',i)
  m2j = [threadPath,'/matlab2java_',num2str(i)];
  j2m = [threadPath,'/java2matlab_',num2str(i)];
  m2jlog = [threadPath,'/m2j_',num2str(i),'.log']; 
  j2mlog = [threadPath,'/j2m_',num2str(i),'.log'];
  
  % create fifo
  cmd = ['mkfifo -m 0600 ',m2j];
  [~,status] = utils_system('cmd',cmd);
  if(status)
  	error(['Failed to run command',cmd]);
  end
  cmd = ['mkfifo -m 0600 ',j2m]; 
  [~,status] = utils_system('cmd',cmd); 
  if(status) 
  	error(['Failed to run command',cmd]); 
  end 
  
  % run java
  if(debug) 
  	shcmd = sprintf('java -cp %s coho.interp.MyParser -l %s -o %s < %s > %s',...  
  					java_classpath,m2jlog,j2mlog,m2j,j2m); 
  else 
  	shcmd = sprintf('java -cp %s coho.interp.MyParser < %s > %s',...
  					java_classpath,m2j,j2m);
  end
  % csh will make matlab exit directly, therefore, we force to use bash 
  cmd = [fork_bin,' -c "', shcmd, '"'];
  [~,status] = utils_system('cmd',cmd);
  if(status)
  	error(['Failed to run command ',cmd]);
  end
  
  javaIn(i) = fopen(m2j, 'w'); javaOut(i) = fopen(j2m, 'r');
  if(javaIn(i) < 0) 
  	error('failed to open javaIn'); 
  end
  if(javaOut(i) < 0) 
  	error('failed to open javaOut'); 
  end
end
fprintf('\n')

cra_cfg('set','javaIn',javaIn,'javaOut',javaOut,'javaTC',0, 'javaCrashed', 0,'currThread',1); 
