% This function computes the LDI model for n-stage RRO circuits
function [A,b,u]= rro_model(bbox,r) 
% [A1,b1,u1] is for the linear term 
% [A2,b2,u2] is for the tanh term
% Fwd inveter uses previous nodes
% cc inveter uses flipped nodes 

  Vsat = 1; T = 1; Vs = 0.25; 
  x0 = mean(bbox,2);  dx = diff(bbox,[],2)/2;
  N = length(x0); 

  %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
  % for X part
  A1 = eye(N); b1 = zeros(N,1); u1 = zeros(N,1);

  %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
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
  
  % for fwd/cc map 
  Eprev = eye(N); Eprev = Eprev([end,1:end-1],:); 
  Eflip = eye(N); Eflip = Eflip([N/2+1:end,1:N/2],:); 
  Afwd = A1+Vsat*Eprev*A2; bfwd = b1+Vsat*Eprev*b2; ufwd = u1+abs(Vsat)*Eprev*u2;
  Acc  = A1+Vsat*Eflip*A2; bcc  = b1+Vsat*Eflip*b2; ucc  = u1+abs(Vsat)*Eflip*u2;

  A = -T*(Afwd+r*Acc); b = -T*(bfwd+r*bcc); u = abs(T)*(ufwd+r*ucc);
