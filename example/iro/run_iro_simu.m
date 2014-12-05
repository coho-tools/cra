% test simulation
%\dot{Vo} = -T*(Vsat*tanh(Vi/Vs) + Vo);
function [ts,vs]=run_iro_simu(N,iter)
  if(nargin<1||isempty(N)), N = 3; end
  if(nargin<2||isempty(iter)), iter= 1; end

  t0 = 0; t_end = 100; 
  % NOTE: when x0=x1=x2, converge to x=0, which is an unstable equilibrium point
  ts = cell(iter,1); vs = cell(iter,1);
  for i=1:iter
    x0 = 2*(rand(N,1)-0.5);  % initial point
    [t, v] = ode45(@(t,x)(iro_model_ode(x)), [t0, t_end], x0); 
    ts{i} = t; vs{i} = v;
    v = v(:,[1:end,1]); 
    for j=1:N 
      figure(j); hold on; plot(v(:,j),v(:,j+1),'g');
    end
    for j=1:N 
      figure(N+j); hold on; plot(t,v(:,j),'r');
    end
  end
end

function xdot = iro_model_ode(x)
  Vs = 0.25; Vsat = 1; T = 1; C = 1;
  xprev = x([end,1:end-1]);
  ids = -T*(x+Vsat*tanh(xprev/Vs)); 
  xdot = ids/C; 
end
