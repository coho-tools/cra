function poly_display(p,varargin)
%function poly_display(p,varargin)
% This function plot a 2D polygon. 
%  p: The polygon to be plot
%  varargin: parameters passed to plot(x,y,...) function
if(isempty(p))
	return;
end
p = p(:,[1:end,1]); 
plot(p(1,:), p(2,:),varargin{:});

