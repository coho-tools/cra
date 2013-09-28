function lp = lp_createByHull(hull,dims)
% lp = lp_createByHull(hull,dims)
% This function conhull a convex hull to a LP,
% where the hull lies in the plane spanned by dims(:,1) and dims(:,2)
%
% hull is a 2 x m matrix, each column is assumed to be a 2D point 
% dims is an n x 2 matrix, whose columns are orthonormal
% 
% lp is a standard LP, however, it is not normalized. 
%
% NOTE: hull is assumed to be convex, and no vertices should be repeated.
% If hull is not convex, the LP is incorrect, may reduce the reachable
% region, or even infeasible

if(nargin<2)
    dims = eye(2);
end;

% Algorithm: 
% The vector from (x0,y0) to (x1,y1) is <dx,dy>, the normal of the vector
% is <dy,-dx>. Therefore, the line is <dy,-dx>.<x,y> = <dy,-dx>.<x0,y0>. 

verts = hull'; n = size(verts,1);
% compute A,b in the 2D plane
switch(n)
case 0 
	nvec = zeros(0,2); v = zeros(0,1);	
case 1 
	nvec = [eye(2);-eye(2)]; v = [verts,-verts]';
case 2
	vec = diff(verts,1,1); % [dx, dy]
	% the order is : [outer normal, forward, inner normal, back]
	nvec = [[vec(2),-vec(1)];vec;[-vec(2),vec(1)];-vec]; 
	v = sum(nvec.*verts([1,2,1,1],:),2); % force v(3)=-v(4)
otherwise
	vec = diff(verts([1:end,1],:),1,1); % [dx,dy]
	nvec = [vec(:,2),-vec(:,1)]; % normal vector, [dy,-dx];
	v = sum(nvec.*verts,2); % [dy,-dx]*[x0,y0]'
end; 
if(any(all(nvec==0,2))) % a row is zero
	error('repeated points found'); 
end; 

% Now we have Ax<=b in the 2D coordinate, M is the transformation matrix
% y = M*x, therefore, the lp is the new coordinate is A*M'*y<=b 
% it also works when dims/nvec is two columns of M/A.
A = nvec*dims'; b = v; 

lp = lp_create(A, b);
lp = lp_norm(lp);
