 %CRA_OPEN: Initialization CRA 
 %  cra_open;
function cra_open(debug)
	if(nargin<1||isempty(debug))
		debug = 0;
	end

	disp('Starting CRA ......'); 

	% error and warnings
	if(debug)
		dbstop if error;
	else
		warning off all;
	end
	

	% Add path
	cra_addpath;

	% Create a unique sys dir
	disp('Create system directory for CRA'); 
	utils_system('mkdir',cra_info('sys_path')); 
  cmd = sprintf('mktemp -d %s/%s_XXX',cra_info('sys_path'),datestr(now,'yy-mm-dd')); 
  [threadPath,status] = utils_system('cmd',cmd);
  if(status)
	  error(['Can not create a unique  directory for CRA']); 
  end
	cra_cfg('set','threadPath',threadPath);
  disp(sprintf('  A unique dir %s has been create for this CRA thread.',threadPath));	

	% Open Logger
	disp('Open an logger to record message'); 
	if(debug)
		log_open([threadPath,'/log']);
	else
	  log_open; 
	end

	% Open Java
	disp('Link Matlab and Java threads');
	java_open(debug);

	disp('CRA initialization complete!');
end %function cra_open


function cra_addpath
	cra_home = cra_info('cra_home'); 
	cra_dirs = cra_info('cra_dirs'); 
  disp('Add CRA directories into Matlab search path');
  %disp('Add the following directories into Matlab search path');
  for i=1:length(cra_dirs)
    dirname = [cra_home,'/',cra_dirs{i}];
    %disp(sprintf('  > %s',dirname));
    addpath(dirname); 
  end
  addpath(cra_home); 
end %function cra_addpath
