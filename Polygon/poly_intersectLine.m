function points = poly_intersectLine(poly,line,isline)
% points = poly_intersectLine(poly,line,isline)
% 	This function computes the intersection of a polygon and a line or segment
% Inputs
% 	poly: a polygon from poly_create
% 	line: [x1,x2;y1,y2];
%	isline: compute the intersection of poly and line if isline is true
%		compute the intersection of poly and segments otherwise
% Outputs:
%	points: the intersection points
% Note: The endpoints of segment are included if it is in the polygon

% Algorithm
% 	The function calls intsecpl of SAGA package now. 
%	I may optimize it for performance in the future.

if(nargin<3||isempty(isline))
	isline = true;
end

[x,y] = intsecpl(poly(1,:),poly(2,:),line(1,:),line(2,:)); 
x = x'; y = y';

if(~isline) 
	% remove points outside the bounding box of the segment
	tol = 10*eps;
	bbox = [min(line,[],2)-tol,max(line,[],2)+tol];	% allow round-off error	
	ind = x>=bbox(1,1) & x<=bbox(1,2) & y>=bbox(2,1) & y<=bbox(2,2);   
	x(~ind)=[]; y(~ind)=[];

	% add endpoints if they are in the polygon and not included in [x,y]
	include = false(2,1);
	np = length(x);
	if(np>0)
		s = (repmat([x;y],1,2)==[repmat(line(:,1),1,np),repmat(line(:,2),1,np)]);
		include = any(reshape(all(s,1),[],2),1);
	end
	if(~all(include)) 
		isc = poly_containPts(poly,line(:,~include)); 
		x = [x,line(1,isc)]; y = [y,line(2,isc)];
	end
end
points = [x;y];


