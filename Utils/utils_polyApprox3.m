function [A,b,err]= utils_polyApprox3(r,p)
% [A,b,err]= utils_polyApprox3(r,p)
%   This function creates linear approximation of polynomials. 
%    f(x) = p(4)*x^3+p(3)*x^2+p(2)*x+p(1)
%    x \in [r(0),r(1)]

if(length(r)~=2), error('r must be 2x1 vector'); end
if(length(p)<0), p(1) = 0; end; 
if(length(p)<1), p(2) = 0; end; 
if(length(p)<2), p(3) = 0; end; 
if(length(p)<3), p(4) = 0; end; 

x0 = mean(r); dx = abs(diff(r))/2; 
A = 3*p(4)*x0^2+2*p(3)*x0+p(2); 
b = -2*p(4)*x0^3-p(3)*x0^2+p(1)+(3*p(4)*x0+p(3))/2*dx^2;
err = abs(3*p(4)*x0+p(3))/2*dx^2+abs(p(4))*dx^3;

