function proc_file(src,dst,macros)
% proc_file(src,dst)
% The function update a matlab with defined macros

rf = fopen(src,'r');
line = 1;
while(true)
	str = fgetl(rf);
	if(~ischar(str)), break, end
	strs{line} = str;
	line = line+1;
end
fclose(rf);

strs = proc_macros(strs,macros);
of = fopen(dst,'w');
for i=1:length(strs)
	fprintf(of, '%s\n',strs{i});
end
fclose(of);

