addpath('../');

S = struct('a','str','b',10);

% disp
str = utils_struct(S,'2str');
disp('The struct is');
fprintf(str);

% list of names
names = utils_struct(S,'names');
disp('The struct has the following fields');
for i=1:length(names)
	disp(names{i});
end;

% check
valids = utils_struct(S,'has','a','b','c');
if(~all(valids==[1;1;0]))
	error('check function incorrect');
end;

% get
value = utils_struct(S,'get','a');
if(~strcmp(value,'str')) 
	error('get function incorrect');
end;
values = utils_struct(S,'get','a','b');
if(values{2}~=10)
	error('get function incorrect');
end;

% set, add and remove
S = utils_struct(S,'set','a',1);
if(S.a~=1)
	error('set function incorrect');
end;
S = utils_struct(S,'add','c',S);
fprintf(utils_struct(S,'2str'));
S = utils_struct(S,'remove','c');
fprintf(utils_struct(S,'disp'));

% check error 
[values,status] = utils_struct(S,'get','c');% expect an error
if(status==0)
	error('error check not work');
end
[values,status] = utils_struct(S,'set','c','error');% expect an error
if(status==0)
	error('error check not work');
end
[values,status] = utils_struct(S,'rm','c','error');% expect an error
if(status==0)
	error('error check not work');
end
[values,status] = utils_struct(S,'add','a','error');% expect an error
if(status==0)
	error('error check not work');
end
