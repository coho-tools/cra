function figs = ph_display(ph,figs,style,vars,varargin)
% figs = ph_display(ph,figs,style,vars,varargin)
% This function displays a projectahedron graphically
% 
% Inputs:
% 	ph: 	The polygon to be plot
%	figs: 	what figures should be used. (1:ph.nc by default)
% 	style: 	a string specifying how to plot projected polygons (default = poly)
%   		'poly': show only the nonconvex version (as an outline)
%   		'hull': show only the convex hull (as an outline)
%   		'both': show both the convex hull and the nonconvex polygon
%               	(polygon will be solid, and hull will be an outline)
% 	vars:	the variable names
%  	varargin:	parameters passed to poly_display function

if(nargin < 1 || isempty(ph))
	figs = [];
	return;
end
if(nargin < 2 || isempty(figs))
	for i=1:ph.ns
		figs(i) = figure(i); 
	end
end
if(nargin < 3 || isempty(style))
	style = 'poly';
end
if(nargin < 4 || isempty(vars))
	vars = cell(ph.ns,1);
	for i=1:ph.dim
		vars{i} = ['x',num2str(i)];
	end
end

for i=1:ph.ns
	figure(figs(i)); 
	switch(lower(style))
		case 'poly'
			poly_display(ph.polys{i},varargin{:});
		case 'hull'
			poly_display(ph.hulls{i},varargin{:});
		case 'both'
			poly = ph.polys{i};
        	fill(poly(1,:), poly(2,:),[0.7,0.7,0.7]);
			hold on;
        	poly_display(ph.hulls{i},varargin{:});
		case 'fwd'
			poly_display(ph.polys{i},varargin{:});
			if(isfield(ph,'fwd'))
				slice = ph.fwd.slices(i);
				projs = [slice.faces.projs];
				polys_display(projs,'k--',varargin{:});
			end
		otherwise
			error('do not support');
	end
	set(gcf, 'Name', ['Slice ',num2str(i)]);
	plane = ph.planes(i,:);
	xlabel(['$\mathbf{',vars{plane(1)},'}$'],'Interpreter','LaTex','fontsize',20);
	ylabel(['$\mathbf{',vars{plane(2)},'}$'],'Interpreter','LaTex','fontsize',20);
	hold on;
end
