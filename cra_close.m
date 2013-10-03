% CRA_CLOSE: Release all CRA resources 
%   cra_close;
function cra_close
	disp('Close Java process');
	java_close;
	disp('Close logger'); 
	log_close;
	cra_rmpath;
	disp('CRA resources have been released!');
end %function cra_close

function cra_rmpath
	cra_home = cra_info('cra_home'); 
	cra_dirs = cra_info('cra_dirs'); 
  disp('Remove the following directories from Matlab search path');
  for i=1:length(cra_dirs)
    dirname = [cra_home,'/',cra_dirs{i}];
    disp(sprintf('  > %s',dirname));
    rmpath(dirname); 
  end
  % rmpath(cra_home);  % leave the cra_home to call cra_open
end %function cra_rmpath
