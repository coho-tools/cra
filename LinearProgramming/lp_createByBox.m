function lp = lp_createByBox(bbox,dim,index)
% lp = lp_box2LP(bbox,dim,index)
% This function compute the lp constraints for a cube.
%	bbox: the first column is the lower bound and 
%		the second column is the upper bound.	
%	dim:  the dimension of lp, size(bbox,1) by default.
%	index: bounded variables. 1:size(bbox,1) by default. 
if(isempty(bbox)), lp = []; return; end;
m = size(bbox,1);
if(nargin<2||isempty(dim))
	dim = m;
end;
if(nargin<3||isempty(index))
	index = 1:m;
end;
index = reshape(index,1,[]);

A = zeros(m,dim); 
A(sub2ind([m,dim],1:m,index)) = -1;
A = [A;-A];
b = [-bbox(:,1); bbox(:,2)];
lp = lp_create(A,b);
