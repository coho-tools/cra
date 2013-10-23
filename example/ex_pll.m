% This is the PLL example from Jijie. 
function ex_pll 
  addpath('~/cra');
  cra_open;
  %ha = ex_pll_ha;
  ha = ex_pll_ha_zz;
  ha = ha_reach(ha);
  ha_reachOp(ha,@(reachData)(phs_display(reachData.sets)));
  cra_close;

function ha = ex_pll_ha
  % Partition by sign(phase) 
  A  = [0,0,-1; 0,0,1]; 
  b1 = [2*pi;0]; b2 = [0;2*pi];
  inv1 = lp_create(A,b1); inv2 = lp_create(A,b2);
  phOpt.fwdOpt = ph_getOpt;
  % skip them as simple
  phOpt.fwdOpt.object = 'ph'; 
  phOpt.fwdOpt.maxBloat = 0.2; 
  callBacks.exitCond = ha_callBacks('exitCond','maxFwdStep',800); 
  callBacks.sliceCond = ha_callBacks('sliceCond','complete');
%  callBacks.afterStep = ha_callBacks('afterStep','display');
  states(1) = ha_state('neg',@(lp)(ex_pll_model(lp,1)),inv1,phOpt,callBacks); 
  states(2) = ha_state('pos',@(lp)(ex_pll_model(lp,2)),inv2,phOpt,callBacks); 

  % transistion
  trans = [];

  % source
  source = {'neg','pos'};

  % initial 
  dim = 3; planes = [1,2;1,3;2,3];
  initBox = [0.9,1.1;1.5,2.5;-2*pi,2*pi];
  initPh = ph_createByBox(dim,planes,initBox); 
  initPh = ph_convert(initPh,'convex');
  inv = lp_createByBox(initBox); % must converge
  
  ha = ha_create('pll',states,trans,source,{initPh,initPh},inv);

% From jijie
%   cdot = sign(ph)*-p.g1
%   vdot = p.g2*p.fref*(c - p.cc);
%   phdot = 1/p.N*v/c - p.fref -p.K*ph ;
% Rewrite by x=c,y=v,z=ph
%   xdot = a = k*p.g1;  % k=[-1,1,0,0]
%   ydot = b*x + c;   % b = p.g2*p.fref, c = -b*p.cc;
%   zdot = d*y/x + e*z + f; % d = 1/p.N; e = -p.K; f = -p.fref;
% By ignorning y/x term, it's simple
%   Xdot = A*X+b\pm err
%     A = [0,0,0; b,0,0; 0,0,e]; 
%     b = [a; c; f]; 
%     u = 0;
% For zdot = y/x, linearize by Jacob
%   zdot  = -avg(y)/avg(x)^2 * x + 1/avg(x) * y + avg(y)/avg(x)
% The error term is   
%   y/x + avg(y)/avg(x)^2 * x - 1/avg(x) * y - avg(y)/avg(x)
function ldi = ex_pll_model(lp,mode)
  x=1;y=2;z=3;
  bbox = lp_box(lp);
  avgs = mean(bbox,2);  extras = diff(bbox,[],2)/2;
  switch(mode)
    case 1 % x(3)<=0
      k = -1; 
    case 2 % x(3)>=0
      k = 1; 
    otherwise % saturate
      k = 0; 
  end  
  p = struct('fref', 2,  'g1', -0.01, 'g1_ph', -0.1,...
    'g2', -0.002,'cmin',0.9,'cmax',1.1,'cc',1.0, 'K',0.8,'N',1);
  
  a = -k*p.g1; b = p.g2*p.fref; c = -b*p.cc; d = 1/p.N; e = -p.K; f = -p.fref;

  A1 = [0,0,0; ...
        b,0,0; ...
        0,0,e]; 
  B1 = [a;c;f];
  U1 = zeros(3,1);  
  % NOTE: trick, do not split regions for c=cmin/cmax
  if(extras(x)<1e-2 & (bbox(x,1)<=p.cmin | bbox(x,2)>=p.cmax)) % stay there 
    B1(1) = a/2; U1(1) = abs(a/2);   
  end

  % y/x
  A2 = [0,0,0; ...
        0,0,0; ...
        -avgs(y)/avgs(x)^2, 1/avgs(x), 0];
  B2 = [0;0;avgs(y)/avgs(x)];
  % err = (avgs(y)dx^2-avgs(x)*dx*dy)/avgs(x)^2(avgs(x)+dx)
  % dx=[-h1,h1]; dy=[-h2;h2];
  ep11 = (avgs(y)*extras(x)^2-avgs(x)*extras(x)*extras(y))/avgs(x)^2*(avgs(x)-extras(x));
  ep12 = (avgs(y)*extras(x)^2+avgs(x)*extras(x)*extras(y))/avgs(x)^2*(avgs(x)-extras(x));
  ep21 = (avgs(y)*extras(x)^2+avgs(x)*extras(x)*extras(y))/avgs(x)^2*(avgs(x)+extras(x));
  ep22 = (avgs(y)*extras(x)^2-avgs(x)*extras(x)*extras(y))/avgs(x)^2*(avgs(x)+extras(x));
  exx1 = -avgs(x)^2/(4*avgs(y))*extras(y)^2/avgs(x)^2*(avgs(x)-extras(x));
  exx2 = -avgs(x)^2/(4*avgs(y))*extras(y)^2/avgs(x)^2*(avgs(x)+extras(x));
  eps = [ep11;ep12;ep21;ep22;exx1;exx2]; err   = [min(eps); max(eps)];
  B2(z) = B2(z) + mean(err); err = err - mean(err); % make error balance
  U2 = [0;0;abs(err(1))];

  A = A1+d*A2; B = B1+d*B2; U = U1+d*U2; 
  U = U+1e-9; % only necessary when object is face-none/height
  ldi = int_create(A,B,U);

function ha = ex_pll_ha_zz
  % Partition by sign(phase) 
  A  = [0,-1; 0,1]; 
  b1 = [2*pi;0]; b2 = [0;2*pi];
  inv1 = lp_create(A,b1); inv2 = lp_create(A,b2);

  phOpt.fwdOpt = ph_getOpt;
  phOpt.fwdOpt.object = 'ph'; 
  phOpt.fwdOpt.maxBloat = 0.2; 
  callBacks.exitCond = ha_callBacks('exitCond','phempty');
  callBacks.sliceCond = @(info)(info.fwdStep>1);  % skip the initial region
  %callBacks.afterStep = ha_callBacks('afterStep','display');
  states(1) = ha_state('n1',@(lp)(ex_pll_model_zz(lp,1)),inv1,phOpt,callBacks); 
  states(2) = ha_state('p1',@(lp)(ex_pll_model_zz(lp,2)),inv2,phOpt,callBacks); 
  states(3) = ha_state('n2',@(lp)(ex_pll_model_zz(lp,1)),inv1,phOpt,callBacks); 
  states(4) = ha_state('p2',@(lp)(ex_pll_model_zz(lp,2)),inv2,phOpt,callBacks); 

  % transistion
  trans(1) = ha_trans('n1','p1'); 
  trans(2) = ha_trans('p1','n2'); 
  trans(3) = ha_trans('n2','p2'); 

  % source
  source = {'n1'};

  % initial 
  dim = 2; planes = [1,2]; 
  initBox = [-1,1;-2*pi,2*pi]; 
  initPh = ph_createByBox(dim,planes,initBox); 
  initPh = ph_convert(initPh,'convex');
  inv = lp_createByBox(initBox); % must converge
  
  ha = ha_create('pll_zz',states,trans,source,initPh,inv);

% From jijie
% wdot = g2xfrefx(c-cc)+sign(ph_diff)xaxg1
% ph_diffdot = w - K x ph_diff
% Where c-cc in [-0.1,0.1], a = (v_max-v_min)/2
% NOTE v = c*fdvco = c*(fref+w)
function ldi = ex_pll_model_zz(lp,mode)
  w=1;ph=2;
  bbox = lp_box(lp);
  avgs = mean(bbox,2);  extras = diff(bbox,[],2)/2;
  p = struct('fref', 2,  'g1', -0.01, 'g1_ph', -0.1,...
    'g2', -0.002,'cmin',0.9,'cmax',1.1,'cc',1.0, 'K',0.8,'N',1);
  v_max = p.cmax*(p.fref+bbox(w,2)); v_max = min(v_max,2.5); 
  v_min = p.cmin*(p.fref+bbox(w,1)); v_min = min(v_min,1.5);
  dv = (v_max-v_min)/2;
  dc = (p.cmax-p.cmin)/2;
  switch(mode)
    case 1 % phase<=0
      k = -1; 
    case 2 % phase>=0
      k = 1; 
    otherwise % saturate
      k = 0; 
  end  
  A = [0,0;1,-p.K];
  b = [k*dv*p.g1;0];
  err = [abs(p.g2*p.fref*dc);0];
  err = err+1e-9; % only necessary for object as 'face-none'/'face-height'
  ldi = int_create(A,b,err);
