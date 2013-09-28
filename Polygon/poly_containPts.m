function [isc,inside,boundary,vertex] = poly_containPts(p,pts)
% isc = poly_containPts(p,pts) 
% This function determines if p contains points pts.  
%	p: 		A simple polygon (2xn matrix).
%	pts: 	A set of points, 2xm matrix. Each column is a 2D point.  
% 	isc:	a 1xm logical vector. 
% 			= 0 if pt is outside p;
% 			= 1 if pt is inside p or on the boundary of p. 
%
%   - this routine uses the fact that the sum of angles between
%     lines from a point to successive vertices of a polygon is 0
%     for a point outside the polygon, and +/- 2*pi for a point
%     inside. A point on an edge will have one such succesive angle
%     equal to +/- pi. 

% special case
if(isempty(p)), isc = false(size(pts,2),1); return; end
if(isempty(pts)), isc = false(1,0); return; end;

pp = p(:,[1:end,1]); % add first point;
px = pp(1,:)'; py = pp(2,:)'; % columns
x = pts(1,:); y = pts(2,:); % rows
n=length(px); m=length(x); 

% distances from points to vertices
dx=ones(n,1)*x-px*ones(1,m);  
dy=ones(n,1)*y-py*ones(1,m);

% Compute angular intervals between vertex points. We shift 
% to put things in the range -pi<= ang <pi, with some slop for 
% possible edge points 
angs=rem( diff(atan2(dy,dx)) + 3*pi+eps , 2*pi ) - pi-eps;
angs = angs./pi; %[-1,1]
% If the angles sum to 0, 4*pi, etc., we are outside 
% if they sum to  -2*pi, 2*pi, 6*pi, etc. we are inside. 
inside =  rem(abs(round( sum(angs) )), 4) > 1;
% If any angle equal -pi, pi, etc. we are on the boundary,
% if angle = 0, then the point is on the line but not on an edge.
boundary = any( abs(abs(angs)-1) <= eps ); % angs = -1 or 1.
% boundary may not work for vertices because atan2(0,0)=0;
vertex = any(abs(dx)<=eps & abs(dy)<=eps); 

isc = false(1,m);
isc(inside|boundary|vertex)=true;
