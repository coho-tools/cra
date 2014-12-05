% This function calculate the LDI model for IRO circuits
% Xl = Eprev * X; Eprev(i,i-1)=1;
%\dot(X) = -T*(X+Vsat*tanh(Xl/Vs) 
%        = -T*X -T*Vsat*tanh(Xl/Vs)
%        = -T*X -T*Vsat*Eprev*tanh(X/Vs)
% f(x)   = tanh(x) 
%        = tanh(x0) + sechx0^2 *dx +/- err 
%        = sechx0^2 * x + tanh(x0) - sechx0^2*x0 +/- err
% dx \in [-C,C]
% err    = tanh(x) - tanh(x0) - sech(x0)^2*dx 
%        = (tanh(x0)+tanh(dx))/(1+tanh(x0)*tanh(dx))-tanh(x0) - sech(x0)^2*dx
%        = tanh(dx)*(1-tanh(x0)^2)/(1+tanh(x0)*tanh(dx)) - sech(x0)^2*dx
%        = tanh(dx)*sech(x0)^2/(1+tanh(x0)*tanh(dx)) - sech(x0)^2*dx
%        = sech(x0)^2* (tanh(dx)/(1+tanh(x0)*tanh(dx)) - dx)
% let a  = tanh(x0)
%  g(x)  = tanh(x)/(1+a*tanh(x)) -x
%        = 1/a - 1/(a*(1+a*tanh(x))) - x 
%  let dg(x) = 0, get x = atanh(-2*a/(1+a^2))
%  So the max value is in x = [-C,C,atanh(-2*a/(1+a^2))] < C 
% 
function [A,b,u]= iro_model(bbox) 
  Vsat = 1; T = 1; Vs = 0.25; 
  x0 = mean(bbox,2);  dx = diff(bbox,[],2)/2;
  N = length(x0); 

  % for X part
  A1 = eye(N); b1 = zeros(N,1); u1 = zeros(N,1);

  % for tanh(X/Vs)  part
  % Let y = X/Vs, the range of y is 
  y0 = x0/Vs; dy = dx/Vs;
  a = tanh(y0); b = sech(y0).^2;
  A2 = diag(b); b2 = a - b.*y0; 
  % u2 = [min(p1,p2,p3),max(p1,p2,p3)], then balance it
  vl = tanh(dy)./(1+a.*tanh(dy)) - dy; 
  vh = -(tanh(dy)./(1-a.*tanh(dy)) - dy); 
  py = atanh(-2*a./(1+a.^2));
  vc = (tanh(py)./(1+a.*tanh(py)) - py).*(abs(py)<dy);
  u2 = [min(min(vl,vh),vc).*b,max(max(vl,vh),vc).*b];
  b2 = b2+mean(u2,2); u2 = diff(u2,[],2)/2;
  % A2/b2/u2 for x
  A2 = A2/Vs; b2 = b2; u2 = u2;
  % for Eprev part
  %Eprev = eye(N); Eprev = Eprev(:,[2:end,1]); 
  Eprev = eye(N); Eprev = Eprev([end,1:end-1],:);
  A2 = Eprev*A2; b2 = Eprev*b2; u2 = Eprev*u2;

  A = A1+Vsat*A2; b = b1+Vsat*b2; u = u1+abs(Vsat)*u2;
  A = -T*A; b = -T*b; u = abs(T)*u;

