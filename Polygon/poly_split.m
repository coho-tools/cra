function [lower,upper] = poly_split(poly,mod)
% [lower,upper] = poly_split(poly,mod)
% This functio split a polyon into two polylines from min(x) to max(x) 
% Inputs: 
% 	poly: 	a polygon from poly_create;
% 	mod: 	1/2/3
% Outputs:
% 	lower/upper: polyline from min(x) to max(x). lower(upper) is the clockwise
% 		(anti-clockwise) part of the polygon. 
% 	when mod == 1
% 		lower is the polyline from left-lower point to right-upper point (clockwise)
% 		upper is the polyline from left-lower point to right-upper point (anti-clockwise)
% 	when mod == 2
% 		lower is the polyline from left-lower point to right-lower point (clockwise)
% 		upper is the polyline from left-upper point to right-upper point (anti-clockwise)
% 	when mod == 3
% 		lower is the polyline from left-upper point to right-upper point (clockwise)
% 		upper is the polyline from left-lower point to right-lower point (anti-clockwise)
if(isempty(poly))
	lower = zeros(2,0); upper = zeros(2,0);
	return;
end
if(nargin<2||isempty(mod))
	mod = 1;
end


x = poly(1,:); y = poly(2,:); 

% find the left-lower and left-upper point
lpos = find(x==min(x));
yl = y(lpos);
llpos = lpos(yl==min(yl)); % no duplicate point in poly
lupos = lpos(yl==max(yl)); 

% find the right-lower and right-upper point
rpos = find(x==max(x));
yr = y(rpos);
rlpos = rpos(yr==min(yr));
rupos = rpos(yr==max(yr));

% find start/end position
switch(mod)
	case 1  % diagonal
		lind = [llpos,rupos]; % left-lower -> right-upper (anti-clock wise)
		uind = [llpos,rupos]; % left-lower -> right-upper (clock wise)
	case 2 % overlap
		lind = [lupos,rupos]; % left-upper -> right-upper (anti-clock wise)
		uind = [llpos,rlpos]; % left-lower -> right-lower (clock wise)
	case 3 % disjoint
		lind = [llpos,rlpos]; % left-lower -> right-lower (anti-clock wise)
		uind = [lupos,rupos]; % left-upper -> right-upper (clock wise)
	otherwise
		error('unknown mode');
end

% lower polyline, anti-clock-wise
if(lind(1)<lind(2))
	lower = poly(:,(lind(1):lind(2)));
else
	lower = poly(:,[lind(1):end,1:lind(2)]);
end

% upper polyline, clock-wise
if(uind(1)<uind(2))
	upper = poly(:,[uind(1):-1:1,end:-1:uind(2)]);
else
	upper = poly(:,(uind(1):-1:uind(2)));
end
