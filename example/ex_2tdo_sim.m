% This is the 2D Vdp example from "Towards Formal Verification of Analog Designs".
function [t,v] = sim_2tdo
	t0 = 0; t_end = 5e-8;
  N = 1000;
	X0 = [rand(N,1)*0.5,rand(N,1)*1e-3];
	figure; hold on; title('osc'); xlabel('V'); ylabel('Y');
	for i=1:N
		x0 = X0(i,:);
    [t, v] = ode45(@(t,y)(tdo_dot(y,1)), [t0, t_end], x0); 
	  plot(v(:,1),v(:,2));
	end

	figure; hold on; title('non-osc'); xlabel('V'); ylabel('Y');
	for i=1:N
		x0 = X0(i,:);
    [t, v] = ode45(@(t,y)(tdo_dot(y,0)), [t0, t_end], x0); 
	  plot(v(:,1),v(:,2));
	end
 
% NOTE: Id is not continuous due to approxed parameters 
function Xdot= tdo_dot(X,osc) 
	V=1;I=2;
	p1 = [0;0.0545;-0.9917;6.0105];
	p2 = [8.9579e-4;0.0040;-0.0421;0.0692];
	p3 = [-0.0112;0.0968;-0.2765;0.2634];
	Id = 0;
	if(X(V)<=0.055), Id = sum(p1.*[1;X(V);X(V)^2;X(V)^3]); end;
  if(X(V)>0.055&&X(V)<=0.35), Id = sum(p2.*[1;X(V);X(V)^2;X(V)^3]); end; 
  if(X(V)>=0.35), Id = sum(p3.*[1;X(V);X(V)^2;X(V)^3]); end; 
	Xdot(V,1) = 1e12*(X(I)-Id);
	if(osc)
	  Xdot(I,1) = 1e6*(-X(V)-200*X(I)+0.3);
  else
	  Xdot(I,1) = 1e6*(-X(V)-242*X(I)+0.3);
  end

