addpath('../..');
coho_addpath('Utils');

files={'./sample0.m','./sample1.m','./sample2.m','./sample3.m'};
macros{1} = struct('name','DEBUG','value',[]);
macros{2} = struct('name','LOG','value',[]);


for i=3:-1:1
	for j=3:-1:1
		macros{1}.value = [i-1,j-1];
		macros{2}.value = [i-1,j-1]; 
		proc_file(files{i},files{j},macros);
	end
end

i = 3; j = 4;;
macros{1}.value = [2,0];
macros{2}.value = [2,1]; 
proc_file(files{i},files{j},macros);
