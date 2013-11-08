function isccw = poly_isCCW(p)
%function isccw = poly_isCCW(p)
% This function check if a 2d simple polygon is counter-clock-wise or not.
% The algorithm compute the area of the polygon, return true if the area 
% is not negative
isccw = poly_area(p)>=0;
