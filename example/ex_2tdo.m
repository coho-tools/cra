% This is the 2D Vdp example from "Towards Formal Verification of Analog Designs".
function ex_2tdo
  addpath('~/cra');
	cra_open;
	ha = ex_2tdo_ha;
	ha = ha_reach(ha);
	ha_reachOp(ha,@(reachData)(phs_display(reachData.sets)));
	cra_close;

% NOTE: This is an example where "bloatAmt" method doesn't work well. 
% This is because V is much large then I, so does the bloatAmt. 
% So if the bloatAmt is suitable for I, it's too small for V. 
% If it's suitable for V, the model error from I is too large.
% Guess-verify can handel this automatically. 
% Another solution is to support matrix in opt.maxBloat.
function ha = ex_2tdo_ha
	% states
	phOpt.fwdOpt = ph_getOpt;
	phOpt.fwdOpt.object = 'ph';
	% default 0.1 is too large for I.
	phOpt.fwdOpt.maxBloat = 1e-2; 
	% Do not go too fast, make the plot clear
	phOpt.fwdOpt.maxStep =  5e-11; 
	% NOTE: timeStep doesn't work well as the timeStep changes significantly during computation
	%phOpt.fwdOpt.model = 'timeStep';
	%phOpt.fwdOpt.timeStep = 5e-11; 
	callBacks.exitCond = ha_callBacks('exitCond','maxFwdT',1.5e-8); 
	callBacks.sliceCond = @(info)(0);  % do not slice
  %callBacks.afterStep = ha_callBacks('afterStep','display');
	states(1) = ha_state('s1',@(lp)(ex_2tdo_model(lp)),[],phOpt,callBacks);

	% source
	source = 's1'; 

  % initial 
	% NOTE: Large I interval may cause large error on Vdot. 
	dim = 2; planes = [1,2]; 
	bbox = [0.4,0.5;(0.5-1e-3)*1e-3,(0.5+1e-3)*1e-3]; 
	initPh = ph_createByBox(dim,planes,bbox);
	initPh = ph_convert(initPh,'convex');

	ha = ha_create('2tdo',states,[],source,initPh);

% Vd_dot = 1/C*(Il-Id(Vd))
% Il_dot = 1/L*(-Vd-R*Il+Vin)
% C = 1pF, L = 1uH, R = 200, Vin = 0.3V
% Rewrite as
% xdot = (y - Id(x))*1e12
% ydot = (0.3-x-200y)*1e6
%
% Id(vd) is from Scott's thesis, but it's not continuous because of error
% Id(v) = 6.0105*v^3 - 0.9917*v^2 + 0.0545*v            (x<=0.055)
% Id(v) = 0.0692*v^3 - 0.0421*v^2 + 0.0040*v + 8.9579e-4 (0.055<=x<=0.35)
% Id(v) = 0.2634*v^3 - 0.2765*v^2 + 0.0968*v - 0.0112   (v>=0.35)

function ldi = ex_2tdo_model(lp) 
	x=1;y=2;
	bbox = lp_box(lp);
	avgs = mean(bbox,2);  extras = diff(bbox,[],2)/2;
	t1 = 0.055; t2 = 0.35; 

	% Linear terms
	A1 = [0,1; -1,-200];
	b1 = [0;0.3]; u1 = [0;0];

	% Id(x) term
	A2 = zeros(2,2); b2 = zeros(2,1); u2 = zeros(2,1);

	p1 = [0;0.0545;-0.9917;6.0105];
	p2 = [8.9579e-4;0.0040;-0.0421;0.0692];
	p3 = [-0.0112;0.0968;-0.2765;0.2634];
	r1 = min(t1,bbox(x,:));
	r2 = min(t2,max(t1,bbox(x,:)));
	r3 = max(t2,bbox(x,:));
	[s1a,s1b,s1u] = ex_2tdo_model_help(r1,p1);
	[s2a,s2b,s2u] = ex_2tdo_model_help(r2,p2);
	[s3a,s3b,s3u] = ex_2tdo_model_help(r3,p3);
	if(avgs(x)<=0.055)
		sa = s1a; sb = s1b; su = s1u;
	elseif(avgs(x)<=0.35)
		sa = s2a; sb = s2b; su = s2u;
  else
		sa = s3a; sb = s3b; su = s3u;
  end
	% add error from other phase
	% NOTE: the error could be smaller, but as the system converges quickly
	% We simplify the computation by using the difference of two linear term + u
	% (sa*x+sb) - (sia*x+sib) 
	if(avgs(x)>t1  && bbox(x,1)<t1)
		su = su+max(abs((sa-s1a)*bbox(x,1)+(sb-s1b)), abs((sa-s1a)*bbox(x,2)+(sb-s1b)))+s1u;
	end
	if((avgs(x)<=t1 && bbox(x,2)>t1) || (avgs(x)>t2 &&  bbox(x,1)<t2)) 
		su = su+max(abs((sa-s2a)*bbox(x,1)+(sb-s2b)), abs((sa-s2a)*bbox(x,2)+(sb-s2b)))+s2u;
  end
	if(avgs(x)<=t2 && bbox(x,2)>t2)
		su = su+max(abs((sa-s3a)*bbox(x,1)+(sb-s3b)), abs((sa-s3a)*bbox(x,2)+(sb-s3b)))+s3u;
	end
	A2(x,x) = sa; b2(x) = sb; u2(x) = su;
	u2(x) = 0;

	A = A1-A2; b = b1-b2; u = u1+u2; u = u+1e-9;
	A(x,:) = 1e12*A(x,:); b(x) = b(x)*1e12; u(x) = u(x)*1e12;
	A(y,:) = 1e6*A(y,:);  b(y) = b(y)*1e6;  u(y) = u(y)*1e6;
	ldi = int_create(A,b,u);
	


function [A,b,err]= ex_2tdo_model_help(r,p)
%   This function creates linear approximation of polynomials. 
%    f(x) = p(4)*x^3+p(3)*x^2+p(2)*x+p(1)
%    x \in [r(0),r(1)]
  x0 = mean(r); dx = abs(diff(r))/2; 
  A = 3*p(4)*x0^2+2*p(3)*x0+p(2); 
  b = -2*p(4)*x0^3-p(3)*x0^2+p(1)+(3*p(4)*x0+p(3))/2*dx^2;
  err = abs(3*p(4)*x0+p(3))/2*dx^2+abs(p(4))*dx^3;

