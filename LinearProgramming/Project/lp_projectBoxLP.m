function hull = lp_projectBoxLP(lp,xx,yy) 
% This is for a special case of lp_project, where 
%   PEx<=q
% where Px<=q is a box LP
% 
%    PEx <= q -> (xx,yy) 
% ==>Px<=q -> (inv(E)xx,invE(yy)) === (X,Y)
% solving
%    max (sin(theta)*X+cos(theta)Y)*x s.t. Px<=q == (L < x < H)
%    the optimial point p(theta) is 
%            p(i) = (cos(theta)*Xi + sin(theta)*Yi) >0 ? H : L
% so the critical point of theta is 
%    theta_i = atan(-Xi/Yi) + pi/0
% after finding all optimal points, the polygon is P
% convert back to (xx,yy) system, as inv(E)P

  n = size(lp.A,2);
  A = lp.A; b = lp.b; 
  if (~isempty(lp.bwd)) 
  	E = lp.bwd; invE = lp.fwd; 
  else 
    E = eye(n); invE = eye(n);
  end

  assert(all(all(A(1:n,:)==-eye(n)))); assert(all(all(A(n+1:end,:)==eye(n))));
  L = -b(1:n); H = b(n+1:end);

  X = invE*xx; Y = invE*yy; 
  % atan(1/0) or atan(-1/0) OK, atan(0/0) is NaN 
  theta = atan(-X./Y);


  LL = repmat(L,1,n); HH = repmat(H,1,n);
  p = LL; pp = HH; 
  THETA = repmat(reshape(theta,1,[]),n,1);
  XX = repmat(X,1,n); YY = repmat(Y,1,n);
  pos = (cos(THETA).*XX+sin(THETA).*YY)>0;
  p(pos) = HH(pos); pp(pos) = LL(pos);

%  p = zeros(n,n); pp = zeros(n,n);
%  for i=1:n
%    % NaN leads to more optimal points, but ok, removed by convex
%    %if(isNaN(theta)), break; end
%    pos = (cos(theta(i))*X+sin(theta(i))*Y)>0;
%    %pos(i) = 0;  % doesn't matter positive or negative
%    p(:,i) = L; p(pos,i) = H(pos);
%    pp(:,i) = L; pp(~pos,i) = H(~pos); % symmatric
%  end
  P = [p,pp];
  invP = invE*P;
  invP = [xx,yy]\invP;  % convert to 2D

  index = convhull(invP(1,:),invP(2,:));
  hull = invP(:,index(1:end-1));

  
