function [x,y] = line_intersect(x1,y1,x2,y2,isline)
% [x,y] = line_intersect(x1,y1,x2,y2,isline)
% 	This function computes intersection ponits of lines (isline = true) 
% 		or sements (islien = false). 
% Inputs
% 	x1,y1: 	2xN matrix, coordinates of endpoints of line segments
%			each column for one line. Similar for x2,y2
%	isline:	compute the intersection of lines if isline = true
%			compute the intersection of segments otherwise
% Outputs
%	x,y:	coordinates of intersection points, each column for a pair of lines
%			NaN if there is no intersection points, Inf if there are more than 
%			one intersection point

% Algorithm
% It is based on the intsecl and iscross funtions of SAGA package now. 
% I may optimize it for performane in the future.  
if(nargin<5||isempty(isline))
	isline = true;
end

% compute intersection of lines
[x,y] = intsecl(x1,y1,x2,y2);
y(isnan(x)) = NaN; % line is degenerate into a point and no intersection
pind = isinf(x)&(y==0); % parallel non-coincident lines
x(pind)=NaN; y(pind) = NaN; 
y(isinf(x)) = Inf; % coincident lines

% check for segments
if(~isline)
	isc = iscross(x1,y1,x2,y2); % 0, 0.5, 1
	isc = isc>0; % has intersection point
	x(~isc) = NaN; y(~isc) = NaN;
	tind = find(isc&isinf(x)); % two segments on the same line
	if(~isempty(tind))
		% find the common endpoints
		xx = [x1;x2]; yy = [y1;y2]; % for value
		xx = xx(:,tind); yy = yy(:,tind);
		xx = sort(xx,1); yy = sort(yy,1); % sort
		oind = (xx(2,:)==xx(3,:) & (yy(2,:)==yy(3,:))); % only share one point
		xx = xx(:,oind); yy = yy(:,oind); tind = tind(oind); 
		xs = xx(2,:); ys = yy(2,:); % same in 2 and 3
		x(tind) = xs; y(tind) = ys;
	end
end
