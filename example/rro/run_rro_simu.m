% This function simulate N-stage rambus ring oscillator
function [ts,vs]=run_rro_simu(N,r,iter)
  if(nargin<1||isempty(N)), N = 2; end
  if(nargin<2||isempty(r)), r = 1; end
  if(nargin<3||isempty(iter)), iter = 1; end

  t0 = 0; t_end = 100; 
  ts = cell(iter,1); vs=cell(iter,1);
  for i=1:iter
    x0 = ones(2*N,1); x0 = x0+(rand(2*N,1)-0.5)*0.5;
    x0([2:2:N,N+1:2:2*N]) = -1*x0([2:2:N,N+1:2:2*N]);
    if(N==2), x0 = x0/2; end
    [t, v] = ode45(@(t,x)(rro_model_ode(x,r)), [t0, t_end], x0); 
    ts{i} = t; vs{i} = v;
    v = v(:,[1:end,1]); 
    for j=1:2*N 
      figure(j); hold on; plot(v(:,j),v(:,j+1),'g');
    end
    for j=1:N
      figure(j+2*N); hold on; plot(v(:,j),v(:,j+N),'g');
    end
    figure(3*N+1); hold on; plot(t,v(:,1),'g');
  end
end

function xdot = rro_model_ode(x,r)
  N = length(x)/2;
  Vs = 0.25; Vsat = 1; T = 1;
  xprev = x([end,1:end-1]); xflip = x([N+1:end,1:N]);
  ids1 = -(x+Vsat*tanh(xprev/Vs)); 
  ids2 = -(x+Vsat*tanh(xflip/Vs));
  xdot = T*(ids1+r*ids2);
end
