% This is the 2D Vdp example from "Towards Formal Verification of Analog Designs".
% NOTE: For the non-osc model, divergence on the metastable region, see simulation result. 
% Use slicing to reduce error 
% See ex_2tdo_cs for more accurate result by using changing variables 
function ex_2tdo
  addpath('~/cra');
	cra_open;
	disp('Working on oscillation mode');
	ha = ex_2tdo_ha(1);
	ha = ha_reach(ha);
	ha_reachOp(ha,@(reachData)(phs_display(reachData.sets)));
	disp('Working on non-oscillation mode');
	ha = ex_2tdo_ha(0);
	ha = ha_reach(ha);
	ha_reachOp(ha,@(reachData)(phs_display(reachData.sets)));
	cra_close;

% NOTE: In this example, the magitute of V and I have significant difference. 
% So bloatAmt should be different for V and I. Otherwise, either the timeStep 
% is too tiny or model error is too large. So please use different maxBloat for V/I.
% Or use Guess-verify to update bloatAmt for V/I automatically. 
function ha = ex_2tdo_ha(osc)
	% Slice to reduce error.
	% states
	phOpt.fwdOpt = ph_getOpt;
	phOpt.fwdOpt.object = 'ph';
	% NOTE: large maxBloat introduce large model error, esp for I.
	phOpt.fwdOpt.maxBloat = [1e-2;1e-4]; 
	% Do not go too fast, make the plot clear
	phOpt.fwdOpt.maxStep = 5e-11; % 1e-10 
	% NOTE: timeStep doesn't work well as the timeStep changes significantly
	% NOTE: bloatAmt is slower
	callBacks = [];
	callBacks.exitCond = @(info)(ph_isempty(info.ph)); 
  %callBacks.afterStep = ha_callBacks('afterStep','display');
  
	% TODO: do not partition by I to save # of states
  % partition by v=[0.055,0.35] to make moding easier. 
  % partition by v = 0.1 because vdot close to zero when v=0.055&i\approx 1e-3;	
	if(osc)
	  v_min = -0.1; v_max = 0.6; v_t1 = 0.055; v_t2 = 0.1; v_t3 = 0.35; 
	  i_min = -0.3e-3; i_max = 1.3e-3; i_mid = (i_min+i_max)/2; 
	else
	  v_min = -0.1; v_max = 0.6; v_t10 = 0.04; v_t1=0.055; v_t20 = 0.07; v_t2 = 0.1; v_t3 = 0.35; 
	  i_min = -0.3e-3; i_max = 1.3e-3; i_mid = (i_min+i_max)/2; i_t1 = 0.9e-3; i_t2 = 0.95e-3; i_t3 = 0.98e-3;
	end
	inv1 = lp_createByBox([v_t3,v_max; i_min,i_mid]);
	inv2 = lp_createByBox([v_t2,v_t3 ; i_min,i_mid]);
	inv3 = lp_createByBox([v_t1,v_t2 ; i_min,i_mid]);
	inv4 = lp_createByBox([v_min,v_t1; i_min,i_mid]);
	if(osc)
	  inv5 = lp_createByBox([v_min,v_t1; i_mid,i_max]);
	  inv6 = lp_createByBox([v_t1,v_t2 ; i_mid,i_max]);
	  inv7 = lp_createByBox([v_t2,v_t3 ; i_mid,i_max]);
	  inv8 = lp_createByBox([v_t3,v_max; i_mid,i_max]);
	else
	  inv5 = lp_createByBox([v_min,v_t10; i_mid,i_t1]);
	  inv6 = lp_createByBox([v_min,v_t10; i_t1,i_t2]);
	  inv7 = lp_createByBox([v_min,v_t10; i_t2,i_max]);
	  inv8 = lp_createByBox([v_t10,v_t1 ; i_t2,i_max]);
	  inv9 = lp_createByBox([v_t1,v_t20 ; i_t3,i_max]);
	end
	func = @(lp)(ex_2tdo_model(lp,osc));
	states(1) = ha_state('s1',func,inv1,phOpt,callBacks);
	states(2) = ha_state('s2',func,inv2,phOpt,callBacks);
	states(3) = ha_state('s3',func,inv3,phOpt,callBacks);
	states(4) = ha_state('s4',func,inv4,phOpt,callBacks);
	states(5) = ha_state('s5',func,inv5,phOpt,callBacks);
	states(6) = ha_state('s6',func,inv6,phOpt,callBacks);
  states(7) = ha_state('s7',func,inv7,phOpt,callBacks);
  states(8) = ha_state('s8',func,inv8,phOpt,callBacks);
	if(~osc)
	callBacks.exitCond = @(info)(ph_isempty(info.ph)||info.fwdT>7e-9);
  states(9) = ha_state('s9',func,inv9,phOpt,callBacks);
  end


	% trans
	trans(1) = ha_trans('s1','s2');
	trans(2) = ha_trans('s2','s3');
	trans(3) = ha_trans('s3','s4');
	trans(4) = ha_trans('s4','s5');
	trans(5) = ha_trans('s5','s6');
  trans(6) = ha_trans('s6','s7');
  trans(7) = ha_trans('s7','s8');
	if(~osc)
  trans(8) = ha_trans('s8','s9');
  end

	% source
	source = 's1'; 

  % initial 
	% NOTE: Large I interval may cause large error on Vdot. 
	dim = 2; planes = [1,2]; 
	bbox = [0.4,0.5;(i_mid-1e-6),(i_mid+1e-6)]; 
	initPh = ph_createByBox(dim,planes,bbox);
	initPh = ph_convert(initPh,'convex');

	if(osc)
	  ha = ha_create('2tdo_osc',states,trans,source,initPh);
  else
	  ha = ha_create('2tdo_nonosc',states,trans,source,initPh);
  end

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

function ldi = ex_2tdo_model(lp,osc) 
	x=1;y=2;
	bbox = lp_box(lp);
	avgs = mean(bbox,2);  extras = diff(bbox,[],2)/2;
	x1 = 0.055; x2 = 0.35; 

	% Linear terms
	if(osc)
	  A1 = [0,1; -1,-200]; % osc
  else
	  A1 = [0,1; -1,-242]; % non-osc
	end
	b1 = [0;0.3]; u1 = [0;0];

	% Id(x) term
	A2 = zeros(2,2); b2 = zeros(2,1); u2 = zeros(2,1);
	if(avgs(x)<=x1) 
	  p1 = [0;0.0545;-0.9917;6.0105];
	  r1 = min(x1,bbox(x,:));
	  [sa,sb,su] = ex_2tdo_model_help(r1,p1);
	elseif(avgs(x)<=x2) 
	  p2 = [8.9579e-4;0.0040;-0.0421;0.0692];
	  r2 = min(x2,max(x1,bbox(x,:)));
	  [sa,sb,su] = ex_2tdo_model_help(r2,p2);
  else
	  p3 = [-0.0112;0.0968;-0.2765;0.2634];
	  r3 = max(x2,bbox(x,:));
	  [sa,sb,su] = ex_2tdo_model_help(r3,p3);
  end
	A2(x,x) = sa; b2(x) = sb; u2(x) = su;

	% NOTE: Add some error here, otherwise, zero error make projection error (poly_regu) even larger.
	A = A1-A2; b = b1-b2; u = u1+u2; u = u+1e-12;
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

