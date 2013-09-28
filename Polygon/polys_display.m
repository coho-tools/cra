function polys_display(ps,varargin)
% poly_display(ps,varargin)
% This function plots a  cell of polygosn
%  ps: polygons to be plotted
%  varargin: parameters passed to plot(x,y,...) function
if(isempty(ps))
	return;
end
hold on;
for i=1:length(ps)
	poly_display(ps{i},varargin{:});
end
