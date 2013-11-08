function [val,status] = utils_system(op,varargin)
% [val,status] = utils_system(cmd)
% This function implement some operation using unix command
if(~isunix)
	error('we do not support it now');
end;
switch(lower(op))
	case {'mkdir'}
		cmd = 'mkdir -p ';
		for i=1:length(varargin)
			cmd = [cmd,' ',varargin{i}]; 	
		end;
	case {'rm'}
		cmd = 'rm -rf ';
		for i=1:length(varargin)
			cmd = [cmd,' ',varargin{i}]; 	
		end
	case {'mv','move'}
		cmd = sprintf('mv %s %s',varargin{1},varargin{2});
	case {'cp','copy'}
		cmd = sprintf('cp %s %s',varargin{1},varargin{2});
	case {'home','homedir'}
		cmd = 'echo $HOME';
	case {'user','whoaimi'}
		cmd = 'whoami';
	case {'unix','cmd'}
		cmd = varargin{1};
	otherwise
		%error('does not support');
end;
[status,val] = unix(cmd);
val = val(1:end-1); % remove \n
