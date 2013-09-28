function codes = proc_macros(codes,macros)
% codes = proc_macros(codes,macros)
% This preprocessor processes matlab source code to speedup computation. 
% Inputs: 
% 	codes: A cell array, each element is a line of source code. 
% 	macros: A cell array, each element is a structure with two fields.
% 		'name': The name of macro variables
%		'value': A 1x2 vector, the old/new value of macro variables. 
% 			It allows three possible value
% 			0: the variable is false
% 			1: the variable is true
%			2: the variable is unknown 
%
% When the old value is 2, this function finds block like 
% if(DEBUG)
% 	codes here...
% end%DEBUG
% When new value of DEBUG=0, it replaces it with
% %if(DEBUG)
% %  codes here ...
% %end%DEBUG
% When new value of DEBUG=1, it replaces it with
% %if(DEBUG)
%   codes here ...
% %end%DEBUG
% When new value of DEBUG=2, it does nothing 
%
% This function does not change line number of codes. 

% remove macros whose values are the same
iss = false(length(macros),1);
for i=1:length(macros)
	iss(i) = diff(macros{i}.value)==0;
end
macros(iss) = [];

n = length(codes); m = length(macros);
if(m==0), return; end

% generate regular expression
bexps = cell(m,1); eexps = cell(m,1);
for i=1:m 
	name = macros{i}.name;
	bexps{i} = ['^\s*%*\s*if\s*\(\s*',name,'\s*\)\s*(%.*)*$'];
	eexps{i} = ['^\s*%*\s*end;*\s*%',name,'\s*(%.*)*$'];
end

line = 0;
while(line<n)
	line = line+1; code = codes{line}; 
	match = regexpi(code,bexps);
	if( ~isempty(cell2mat(match)) )
		% find BEGIN
		for i=1:m
			if(~isempty(match{i}))
				ind = i;
				break;
			end
		end
		macro = macros{ind}; value = macro.value;
		isbegin = true;
		while(true)
			isend = (~isempty(regexpi(code,eexps{ind})));
			% replace lines
			switch(value(2))
				case 0 % add % if not present
					switch(value(1))
						case 1
							if(~(isbegin||isend))
								codes{line} = add_comment(code);
							end
						case 2 
							codes{line} = add_comment(code);
						otherwise
							error('unknown type');
					end
				case 1 % remove comments of codes
					switch(value(1))
						case 0
							if(~(isbegin||isend))
								codes{line} = remove_comment(code);
							end
						case 2
							if(isbegin||isend)
								codes{line} = add_comment(code);
							end
						otherwise
							error('unknown value');
					end
				case 2 % remove comments
					switch(value(1))
						case 0
							codes{line} = remove_comment(code);
						case 1
							if(isbegin||isend)
								codes{line} = remove_comment(code);
							end
						otherwise
							error('unknown value');
					end
				otherwise
					error('unknown value'); 
			end
			if(isend), break; end
			if(line+1>n), error('incorrect format'); end
			line = line+1; code = codes{line}; 
			isbegin = false;
		end
	end
end

function str = add_comment(str) 
	str = ['%',str]; 

function str = remove_comment(str) 
	cexp = '^\s*%'; 
	s = regexpi(str,cexp,'split'); 
	if(length(s)>1) 
		str = s{2}; 
	end
