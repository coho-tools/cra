% This is the 3D voltage controlled oscillator from 
% "Verifying analog oscillator circuit using forward/backward abstraction refinement".
%
% The circuit is modeled by ODE
%   \dot{Vd1} = -1/C * (Ids(Vd2-Vdd,Vd1-Vdd)+IL1)
%   \dot{Vd2} = -1/C * (Ids(Vd1-Vdd,Vd2-Vdd)+Ib-IL1)
%   \dot{IL1} = 1/2L * (Vd1-Vd2-R*(2IL1-Ib)); 
% Ids(Vgs,Vds) function
%   Ids = 0; (Vgs > Vtp)
%   Ids = Kp * W/L * ((Vgs - Vtp)*Vds - 1/2*Vds^2) * (1- r*Vds); (Vgs <= Vtp & (Vds-Vgs) > -Vtp)
%   Ids = 1/2 * Kp * W/L * (Vgs-Vtp)^2 * (1-r*Vds); (Vgs <= Vtp & (Vds-Vgs) <= -Vtp)
% Parameters: 
%   C = 3.43pF; L = 2.857nH; R = 3.7 Ohm; Ib = 18mA; Vdd=1.8V.
%   Vtp = -0.69V; Kp = -86 uA/V^2; W/L = 240um/0.25um; r = -0.07V^{-1}
% Initial region
%   Vd1 =[-1.4,-1.0], Vd2=[1.6,1.9], IL1 = 0;
%
% NOTE: Kp is positive in the paper. But I think it's a mistake as Kp is negative for PMOS. 
% NOTE: The result in the paper is incorrect. IL1&IL2 should be symmetric by Ib.
%     Their region of IL1 is [-0.2,0.8], our is [-0.04,0.06].

% NOTE: change variable by IL1'=10*IL1 helps slightly
function ex_3vco
  addpath('~/cra');
	cra_open;

	% with slicing
	ha = ex_3vco_ha;
	ha = ha_reach(ha);
	ha_reachOp(ha,@(reachData)(phs_display(reachData.sets)));

  % without slicing
	ha_ns = ex_3vco_ha_ns;
	ha_ns = ha_reach(ha_ns);
	ha_reachOp(ha_ns,@(reachData)(phs_display(reachData.sets)));

	cra_close; 

% NOTE: reduce error by slicing
function ha = ex_3vco_ha
	inv1 = lp_create([0,0,1],0); % IL1 is negative
	inv2 = lp_create([0,0,-1],0); % IL1 is positive 
	% states
	phOpt.fwdOpt = ph_getOpt;
	phOpt.fwdOpt.object = 'ph';
	% Variable regions Vd1/Vd2 = [-1.5,2.0], Il1 = [-0.04,0.06]
	phOpt.fwdOpt.maxBloat = [1;1;1/25]*0.1; 
	callBacks.sliceCond = @(info)(info.fwdT > 1e-10);  % no slice initial region
	states(1) = ha_state('s1',@(lp)(ex_3vco_model(lp)),inv1,phOpt,callBacks);
	states(2) = ha_state('s2',@(lp)(ex_3vco_model(lp)),inv2,phOpt,callBacks);

	% trans
	trans(1) = ha_trans('s1','s2');
  
	% source
	source = 's1'; 

	% initial 
	dim = 3; planes = [1,2;1,3;2,3]; 
	bbox = [-1.4,-1.0; 1.6,1.9; -1e-6,1e-6];
	%bbox = [-1.4,-1.0; 1.7,1.8; -1e-6,0];
	initPh = ph_createByBox(dim,planes,bbox);
	initPh = ph_convert(initPh,'convex');

	ha = ha_create('3vco',states,trans,source,initPh);

% NOTE: Without slicing, we reduce initial region and step size to show convergence. 
function ha = ex_3vco_ha_ns
	% states
	phOpt.fwdOpt = ph_getOpt;
	phOpt.fwdOpt.object = 'ph';
	%phOpt.fwdOpt.maxBloat = [1;1;1/30]*0.1; 
	phOpt.fwdOpt.maxBloat = [1;1;1/25]*0.05;  % reduce step size
	callBacks.exitCond = ha_callBacks('exitCond','maxFwdT',7.5e-10); 
	callBacks.sliceCond = @(info)(0);  % do not slice
	states(1) = ha_state('s1',@(lp)(ex_3vco_model(lp)),[],phOpt,callBacks);

	% source
	source = 's1'; 

	% initial 
	dim = 3; planes = [1,2;1,3;2,3]; 
	%bbox = [-1.4,-1.0; 1.6,1.9; -1e-6,0];
	bbox = [-1.4,-1.0; 1.7,1.8; -1e-6,0]; % smaller initial regions
	initPh = ph_createByBox(dim,planes,bbox);
	initPh = ph_convert(initPh,'convex');

	ha = ha_create('3vco_ns',states,[],source,initPh);

% ODE is 
%   \dot{Vd1} = -1/C * (Ids(Vd2-Vdd,Vd1-Vdd)+IL1)
%   \dot{Vd2} = -1/C * (Ids(Vd1-Vdd,Vd2-Vdd)-IL1+Ib)
%   \dot{IL1} = 1/2L * (Vd1-Vd2-2*R*IL1+R*Ib)); 
function ldi = ex_3vco_model(lp) 
	d1=1;d2=2;l1=3;
	pp = struct('C',3.43e-12, 'L',2.857e-9, 'R', 3.7, 'Ib', 18e-3, 'Vdd',1.8);  
	k1 = -1/pp.C ; k2 = 1/(2*pp.L); 
	A = zeros(3,3); b = zeros(3,1); u = zeros(3,1); 
	bbox = lp_box(lp);

	% A(x-Vdd) + b \pm e
	[A1,b1,e1] = ex_3vco_model_ids(bbox([d2,d1],:)-pp.Vdd); 
	[A2,b2,e2] = ex_3vco_model_ids(bbox([d1,d2],:)-pp.Vdd);
	A(d1,[d2,d1]) = A1; b(d1) = b1-pp.Vdd*sum(A1); u(d1) = e1;
	A(d2,[d1,d2]) = A2; b(d2) = b2-pp.Vdd*sum(A2); u(d2) = e2;
	A(d1,l1) = 1; A(d2,l1) = -1; b(d2) = b(d2)+pp.Ib; 
  A(l1,d1) = 1; A(l1,d2) = -1; A(l1,l1) = -2*pp.R; b(l1) = pp.R*pp.Ib; 

	% add constant
	A([d1,d2],:) = k1*A([d1,d2],:); b([d1,d2]) = k1*b([d1,d2]); u([d1,d2]) = abs(k1)*u([d1,d2]);
	A(l1,:) = k2*A(l1,:); b(l1) = k2*b(l1); u(l1) = abs(k2)*u(l1);
	ldi = int_create(A,b,u); 

% Ids(Vg,Vd) is 
%   Vgs = Vg-1.8; Vds = Vd-1.8; beta = Kp*W/L = 8.6e-5*960 = 0.08256  
%   Ids = 0; (Vgs > Vtp)
%   Ids = beta * ((Vgs - Vtp)*Vds - 1/2*Vds^2) * (1- r*Vds); (Vgs <= Vtp & (Vds-Vgs) > -Vtp)
%   Ids = 0.5*beta * (Vgs-Vtp)^2 * (1-r*Vds); (Vgs <= Vtp & (Vds-Vgs) <= -Vtp)
% Let's ignore the (1-r*Vds) part for the first try
function [a,b,e]= ex_3vco_model_ids(bbox)
	gs =1; ds = 2; 
	pp = struct('Kp',-86e-6, 'W',240e-6, 'L',0.25e-6, 'Vtp', -0.69);
	beta = pp.Kp*pp.W/pp.L;

	% find all regions by corners
	regions = zeros(3,1);
	pts = [bbox(1,1),bbox(1,1),bbox(1,2),bbox(1,2),mean(bbox(1,:));
         bbox(2,1),bbox(2,2),bbox(2,1),bbox(2,2),mean(bbox(2,:))];
	pir1 = (pts(gs,:)> pp.Vtp);
	pir2 = (pts(gs,:)<=pp.Vtp) & (pts(ds,:)-pts(gs,:)> -pp.Vtp);
	pir3 = (pts(gs,:)<=pp.Vtp) & (pts(ds,:)-pts(gs,:)<=-pp.Vtp);
	regions(1) = any(pir1); regions(2) = any(pir2); regions(3) = any(pir3);
	cpir = [pir1(end),pir2(end),pir3(end)]; region = find(cpir); 
	bbox1 = []; bbox2 = []; bbox3 =[];
  lp0 = lp_createByBox(bbox); 
	if(regions(1))
		bbox1 = lp_box(lp_and(lp0,lp_create([-1,0],-pp.Vtp)));
	end
	if(regions(2)) 
		bbox2 = lp_box(lp_and(lp0,lp_create([1,0;1,-1],[pp.Vtp;pp.Vtp])));
  end
	if(regions(3)) 
		bbox3 = lp_box(lp_and(lp0,lp_create([1,0;-1,1],[pp.Vtp;-pp.Vtp])));
  end

	%% use middle point region to calculate linearization 
  a = [0,0]; b = 0; e = 0; 
	switch(region)
  case 1
		% use zeros
	case 2
	  avgs = mean(bbox2,2);  extras = diff(bbox2,[],2)/2;

		% -0.5Vds^2 - Vtp*Vds;
		p = zeros(4,1); p(2) = -pp.Vtp; p(3) = -0.5;   
		r = bbox2(ds,:);    
		[ad,bd,ed] = ex_3vco_model_help(r,p); 
		a(ds) = ad; b = bd; e = ed;

    % Vgs*Vds
	  % f = x1*x2; -> 
		% f \approx avgs(x2)*x1 + avgs(x1)*x2 - avgs(x1)*avgs(x2); err = dx1*dx2;	
		a(gs) = a(gs)+avgs(ds); 
		a(ds) = a(ds)+avgs(gs);
		b = b-avgs(gs)*avgs(ds); 
		e = e+extras(gs)*extras(ds);

    % constant
		a = beta*a; b = beta*b; e = abs(beta)*e;
	case 3
		% Vgs^2-2*Vtp*Vgs+Vtp^2;
		p = zeros(4,1); p(1) = pp.Vtp^2; p(2) = -2*pp.Vtp; p(3) = 1; 
		r = bbox3(gs,:);    
		[ag,bg,eg] = ex_3vco_model_help(r,p); 
		a(gs) = ag; b = bg; e = eg;
		a = 0.5*beta*a; b = 0.5*beta*b; e = 0.5*abs(beta)*e;
	end

	%% error term from other regions 
	regions(region) = 0;
  minE1 = -e; maxE1 = e; minE2 = -e; maxE2 = e; minE3 = -e; maxE3 = e;
  if(regions(1))
		% err = -(a*x+b)
	  pts = [bbox1(1,1),bbox1(1,1),bbox1(1,2),bbox1(1,2); 
           bbox1(2,1),bbox1(2,2),bbox1(2,1),bbox1(2,2)]; 
		err = 0-(a*pts+b);
		minE1 = min(err); maxE1 = max(err);
  end
  if(regions(2))
		% err = beta*( (x1-pp.Vtp)*x2 - 0.5*x2^2) - (ax+b)
	  pts = [bbox2(1,1),bbox2(1,1),bbox2(1,2),bbox2(1,2); 
           bbox2(2,1),bbox2(2,2),bbox2(2,1),bbox2(2,2)]; 
		d_pt = -(pp.Vtp+a(ds)/beta);
	  if(d_pt>=bbox2(ds,1) && d_pt<=bbox2(ds,2))
			pts = [pts,[bbox2(gs,1);d_pt],[bbox2(gs,2);d_pt]];
		end
		err = (beta*((pts(gs,:)-pp.Vtp).*pts(ds,:)-0.5*pts(ds,:).^2)) - (a*pts+b);  
    minE2 = min(err); maxE2 = max(err);
  end
	if(regions(3))
		% err = 0.5c*(x1-pp.Vtp)^2 - (ax + b)
	  pts = [bbox3(1,1),bbox3(1,1),bbox3(1,2),bbox3(1,2); 
           bbox3(2,1),bbox3(2,2),bbox3(2,1),bbox3(2,2)]; 
		g_pt = pp.Vtp+a(gs)/beta;
		if(g_pt>=bbox3(gs,1)&&g_pt<=bbox3(gs,2))
			pts = [pts,[g_pt;bbox3(ds,1)],[g_pt;bbox3(ds,2)]];
		end
		err = (0.5*beta*(pts(gs,:)-pp.Vtp).^2) - (a*pts+b);  
    minE3 = min(err); maxE3 = max(err);
	end

	% balance the error
	minE = min([minE1,minE2,minE3]); maxE = max([maxE1,maxE2,maxE3]);
	b = b+mean([minE,maxE]);
	e = (maxE-minE)/2;

function [A,b,err]= ex_3vco_model_help(r,p)
%   This function creates linear approximation of polynomials. 
%    f(x) = p(4)*x^3+p(3)*x^2+p(2)*x+p(1)
%    x \in [r(0),r(1)]
  x0 = mean(r); dx = abs(diff(r))/2; 
  A = 3*p(4)*x0^2+2*p(3)*x0+p(2); 
  b = -2*p(4)*x0^3-p(3)*x0^2+p(1)+(3*p(4)*x0+p(3))/2*dx^2;
  err = abs(3*p(4)*x0+p(3))/2*dx^2+abs(p(4))*dx^3;
