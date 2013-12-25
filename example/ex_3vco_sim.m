% This is the 3D voltage controlled oscillator from 
% "Verifying analog oscillator circuit using forward/backward abstraction refinement".
% We use simulation to verify Kp should be negative
% Range of Il1 is much smaller than [-0.2,0.8] in the paper.
function ex_3vco_sim
	t0 = 0; t_end = 2e-9;
  N = 100;
	X0 = [-1.4+rand(N,1)*0.4, 1.6+rand(N,1)*0.3,-rand(N,1)*0.01]; 
	%X0 = [-1.5+rand(N,1)*3.5, -1.5+rand(N,1)*3.5,(rand(N,1)-0.5)*0.1]; // all region
	figure(1); hold on; title('3vco'); xlabel('Vd1'); ylabel('Vd2'); grid on;
	figure(2); hold on; title('3vco'); xlabel('Vd1'); ylabel('Il1'); grid on;
	figure(3); hold on; title('3vco'); xlabel('Vd2'); ylabel('Il1'); grid on;
	figure(4); hold on; title('3vco'); xlabel('Vd1'); ylabel('Vd2'); zlabel('Il1'); grid on; view(3);
	for i=1:N
		x0 = X0(i,:);
    [t, v] = ode45(@(t,y)(ex_3vco_dot(y)), [t0, t_end], x0); 
		figure(1); plot(v(:,1),v(:,2));
		figure(2); plot(v(:,1),v(:,3));
		figure(3); plot(v(:,2),v(:,3));
		figure(4); plot3(v(:,1),v(:,2),v(:,3));
	end

% ODE is 
%   \dot{Vd1} = -1/C * (Ids(Vd2-Vdd,Vd1-Vdd)+IL1)
%   \dot{Vd2} = -1/C * (Ids(Vd1-Vdd,Vd2-Vdd)-IL1+Ib)
%   \dot{IL1} = 1/2L * (Vd1-Vd2-2*R*IL1+R*Ib)); 
function Xdot = ex_3vco_dot(X) 
	d1=1;d2=2;l1=3;
	pp = struct('C',3.43e-12, 'L',2.857e-9, 'R', 3.7, 'Ib', 18e-3, 'Vdd',1.8);  
	k1 = -1/pp.C ; k2 = 1/(2*pp.L); 
  Xdot = zeros(3,1); 
	Xdot(d1) =  k1* (ex_3vco_ids(X(d2)-pp.Vdd,X(d1)-pp.Vdd)+X(l1));
	Xdot(d2) =  k1* (ex_3vco_ids(X(d1)-pp.Vdd,X(d2)-pp.Vdd)-X(l1)+pp.Ib);
	Xdot(l1) =  k2* (X(d1)-X(d2)-2*pp.R*X(l1)+pp.R*pp.Ib); 

% Ids(Vg,Vd) is 
%   beta = Kp*W/L = 8.6e-5*960 = 0.08256  
%   Ids = 0; (Vgs > Vtp)
%   Ids = beta * ((Vgs - Vtp)*Vds - 1/2*Vds^2) * (1- r*Vds); (Vgs <= Vtp & (Vds-Vgs) > -Vtp)
%   Ids = 0.5*beta * (Vgs-Vtp)^2 * (1-r*Vds); (Vgs <= Vtp & (Vds-Vgs) <= -Vtp)
% Let's ignore the (1-r*Vds) part for the first try
function Ids = ex_3vco_ids(Vgs,Vds)
	pp = struct('Kp',-86e-6, 'W',240e-6, 'L',0.25e-6, 'Vtp', -0.69,'r',-0.07);
	beta = pp.Kp*pp.W/pp.L;

	% find all regions by corners
	if(Vgs>pp.Vtp)
		Ids = 0;
	elseif(Vds-Vgs>-pp.Vtp)
		%Ids = beta*((Vgs-pp.Vtp)*Vds-0.5*Vds^2); 
		Ids = beta*((Vgs-pp.Vtp)*Vds-0.5*Vds^2)*(1-pp.r*Vds);
	else
		%Ids = 0.5*beta*(Vgs-pp.Vtp)^2; 
		Ids = 0.5*beta*(Vgs-pp.Vtp)^2*(1-pp.r*Vds);
	end
