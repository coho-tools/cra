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
if( (cra_cfg('has','javaIn') && ~isempty(cra_cfg('get','javaIn')) && cra_cfg('get','javaIn')>1)  || ... 
	  (cra_cfg('has','javaOut') && ~isempty(cra_cfg('get','javaOut')) && cra_cfg('get','javaOut')>2) )
	java_close; % close old one first;
end

fork_bin = cra_info('fork_bin');
java_classpath = cra_info('java_classpath'); 
sys_path= cra_info('sys_path');

% create a dir 
threadPath = cra_cfg('get','threadPath');
m2j = [threadPath,'/matlab2java'];
j2m = [threadPath,'/java2matlab'];
m2jlog = [threadPath,'/m2j.log']; 
j2mlog = [threadPath,'/j2m.log'];

% create fifo
cmd = ['mkfifo -m 0600 ',m2j];
[result,status] = utils_system('cmd',cmd);
if(status)
	error(['Failed to run command',cmd]);
end
cmd = ['mkfifo -m 0600 ',j2m]; 
[result,status] = utils_system('cmd',cmd); 
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
[result,status] = utils_system('cmd',cmd);
if(status)
	error(['Failed to run command ',cmd]);
end

javaIn = fopen(m2j, 'w'); javaOut = fopen(j2m, 'r');
if(javaIn < 0) 
	error('failed to open javaIn'); 
end
if(javaOut < 0) 
	error('failed to open javaOut'); 
end

cra_cfg('set','javaIn',javaIn,'javaOut',javaOut,'javaTC',0, 'javaCrashed', 0); 
