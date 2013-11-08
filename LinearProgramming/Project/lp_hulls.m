function [ohull,uhull] = lp_hulls(lp,angles,index)
% [ohull,uhull] = lp_hulls(lp,angless,index)
% This function computes an over-approximation and 
% an under-approximation of projection polygon using
% optimization direction with specified angless. 
% index: the axis of projection plane  
tol = 1e-3;

% remove angles that close to each other
angles = reshape(angles,[],1);
angles = mod(angles,2*pi); % [0,2*pi]
angles = sort(angles);
diffs = [angles(2:end);angles(1)+2*pi]-angles;
angles = angles(diffs>tol);
ndir = [cos(angles),sin(angles)];

% solve the LP
dirs = zeros(size(lp.A,2),length(angles));
dirs(index,:) = ndir'; % to full dimension
% use max
[v,x,status] = lp_opt(lp,-dirs); 
v = -v;
if(any(status~=0))
	ohull = zeros(2,0); uhull = zeros(2,0);
	return;
end

% compute the intersection points
% Ax = b, solve A([i,i+1],:)x=b([i,i+1])
A = ndir; b = v;
AA(1,:) = reshape(A',1,[]);
AA(2,:) = reshape(A([2:end,1],:)',1,[]);
bb = [b,b([2:end,1])]';
ohull = utils_solveLS(AA,bb);

% Make it convex, use snap to avoid convexhull error.
ohull = poly_convexHull(snap(ohull)); 
% under approximation
uhull = poly_convexHull(snap(x(index,:)));


function pts = snap(pts)
	tol = 10*eps; opts = pts;
	pts = round(pts/tol)*tol;
	pts = unique(pts','rows')'; 
	if(size(pts,2)<3)
		pts = poly_regu(opts,1e-12);
	end
