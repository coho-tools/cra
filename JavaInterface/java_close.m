function java_close
% function java_close
% tells the java CG engine to exit and closes the java pipes
% assumes that the global file handles have been set by java_open()
%
javaIn = cra_cfg('get','javaIn');
javaOut = cra_cfg('get','javaOut'); 
javaCrashed = cra_cfg('get','javaCrashed');

if(javaCrashed)
	disp('Warning: Java thread crashed. Please kill the process manually!');
else
  if(javaOut>3)
	  java_writeLine('exit();');
  end
end

if(javaIn>2) 
	fclose(javaIn);
end
if(javaOut>3)
	fclose(javaOut);
end

threadPath = cra_cfg('get','threadPath');
cmd = sprintf('unlink %s/matlab2java',threadPath);
utils_system('cmd',cmd);
cmd = sprintf('unlink %s/java2matlab',threadPath);
utils_system('cmd',cmd);

% we keep tmpDir and TC now, will be reset when java_open 
cra_cfg('set','javaIn',1,'javaOut',2);
