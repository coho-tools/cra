function A = poly_area(p)
% A = p_area(p)
% This function computes the area of a 2D counter-clock wise pgon. 
% The equation is A = (\sum_{1}^{n} x_i*y_{i+1}-x_{i+1}*y_{i})/2
% A is positive for counter clock wise order polygon
% A is negative for clock-wise order polygon
if(isempty(p))
	A = 0;
else
	A = sum(p(1,:).*p(2,[2:end,1]) - p(1,[2:end,1]).*p(2,:))/2;
end
