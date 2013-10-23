function figs = phs_display(phs,figs,style,vars,varargin)
if(isempty(phs)), return; end
ise = false(length(phs),1);
for i=1:length(phs)
	ise(i) = ph_isempty(phs{i});
end
phs = phs(~ise);
if(nargin < 1 || isempty(phs))
	figs = [];
	return;
end
if(nargin < 2 || isempty(figs))
	for i=1:phs{1}.ns
		figs(i) = figure(i); 
	end
end
if(nargin < 3 || isempty(style))
	style = 'poly';
end
if(nargin < 4 || isempty(vars))
	vars = cell(phs{1}.ns,1);
	for i=1:phs{1}.dim
		vars{i} = ['x',num2str(i)];
	end
end
for i=1:length(phs)
	ph_display(phs{i},figs,style,vars,varargin{:});
end
