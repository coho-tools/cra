%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% This is the code used by CAV14 paper to compute circuit states of DPLL. 
%% For details of the DPLL circuit, please check the CAV14 paper and FMCAD13 paper.
%% For details of zig-zag modeling, please check Mark's documents strategy.pdf & example2.pdf. 
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% Verification outline 
%    The problem is to show that all circuit states [c,v,theta] \in [cmin,cmax;vmin,vmax;-2*pi,2*pi]
%    will converge to the lock state [c,v,theta] = [c_cntr,v_cntr,0] (or small region near by). 
% 
%    To check the property, we split the state space into three pieces by lines v/fref = c+dc and v/fref = c-dc. 
%    We call the regions as R1,R2,R3, which can be further divided by theta=0, 
%    with names R*.a for mode 1 (theta>=0) and R*.b for mode 2. (theta<=0). 
%
%    Step1: We show that all trajectories in R1 will leave the region by face v/fref=c-dc or c=cmin. 
%           Trajectories crossing v/fref=c-dc, will be handled in Step 4. 
%           Trajectories crossing c = cmin will be handled in Step 2-3. 
%           R3 can be treated similarly, by symmetric, we ignore it. 
%    Step2: Show trajectories from R1 will climb the wall: c=cmin as saturated, v will increase and then theta. 
%    Step3: Show trajectories from Step 2 will leave the wall and enter zig-zag mode. 
%    Step4: Show trajectories in R2 will converge to [c_cntr,v_cntr,0].
%           By symmetric, we only consider v \in [vmin, v_cntr]. 
%           The trajectories behavior like zig-zag style, therefore,  we used zig-zag model from Mark. 
%           We partition v into overlapped small intervals and show that for each interval, the value of v increase monotonically. 
%    Step4.b: Use full models to show that both lower/upper bounds of v/c increase in the zig-zag model.  
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

function run_dpll 
  addpath('~/cra');
  cra_open;

  p = struct('fref', 3, 'g1', -7.8e-4,'g2',-0.002,'K',0.8,'N',1, ... 
             'cmin',0.9,'cmax',1.1,'cc',1.0,'vmin',2,'vmax',4, ...
             'dc', 0.01);

  disp('This program is to compute circuit states of DPLL for CAV14 papers.');
  disp('It takes around half an hour to complete the work, so please take some coffee.'); 
  disp('There are mainly four steps to complete the proof. Plots used in the paper will be generated at the end.'); 

  % Step 1.a  
if(1)  
  disp('Step1: Show trajectories leaves region R1'); 
  ha = dpll_ha_r1r3(p,1);
  ha = ha_reach(ha);
  %ha_reachOp(ha,@(reachData)(phs_display(reachData.sets)));
end

  % Step 1.b, can be ignored by symmetry  
if(0)
  disp('Step1.b: Show trajectories leaves region R3'); 
  ha = dpll_ha_r1r3(p,0);
  ha = ha_reach(ha);
  %ha_reachOp(ha,@(reachData)(phs_display(reachData.sets)));
end
  
  % Step 2: climbing the wall 
if(1)
  disp('Step2: Show trajectories leaves R1 by face c=cmin will climb to theta>0'); 
  ha = dpll_ha_r1_cw(p); 
  ha = ha_reach(ha);
  %ha_reachOp(ha,@(reachData)(phs_display(reachData.sets)));
  % NOTE: HA will not slice if not used by any trans. 
  % So we have to compute by ourself. TODO: upate HA?
  load dpll_r1_cw_neg.mat;
  lp = lp_create([0,0,-1],0); % theta > 0
  % skip the first one as it contains the initial values
  for i=2:length(reachData.tubes);
    slices{i} = ph_intersectLP(reachData.tubes{i},lp);
  end
  ph = ph_canon(ph_union(slices));
  disp(sprintf('The result of climbing wall step is [%d,%d;%d,%d]',ph.bbox(1,1),ph.bbox(1,2),ph.bbox(2,1),ph.bbox(2,2)));
end
  
  % Step 3: leaving the wall
if(1)
  disp('Step3: Show trajectories leaves R1 by c=cmin leaves c=cmin'); 
  N = 10;
  ha = dpll_ha_r1_lw(p,N);
  ha = ha_reach(ha);
  %ha_reachOp(ha,@(reachData)(phs_display(reachData.sets)));
end
  
  % Step 4: working on zig-zag mode
if(1)
  disp('Step4.a: Show trajectories converge to [c_cntr,v_cntr] using the zig-zag model.'); 
  % NOTE: we use overlapped intervals. May change later.
  v0mid = [p.cmin:p.dc:p.cc-p.dc, p.cmin+p.dc*8.99];
  for i=1:length(v0mid)
    v0 = (v0mid(i)+[-p.dc,p.dc])*p.fref;
    disp(sprintf('Working on interval of v:[%d,%d].',v0(1),v0(2)));
    ha = dpll_ha_r2_zz(p,v0); 
    ha = ha_reach(ha);
    ha_reachOp(ha,@(reachData)(dpll_check_v1(reachData.sets))); 
  end
end


  % Step 4.b: working on zig-zag mode by using full model
if(1)
  disp('Step4.b: Show lower/upper bound of c/v increase using the full mode.') 
  v0mid = [p.cmin:p.dc:p.cc-p.dc, p.cmin+p.dc*8.5];
  for i=1:length(v0mid)
    v0 = (v0mid(i)+[-p.dc,p.dc])*p.fref;
    disp(sprintf('Working on interval of v:[%d,%d].',v0(1),v0(2)));
    ha = dpll_ha_r2_zz_full(p,v0); 
    ha = ha_reach(ha);
    % check Mark's property
    load dpll_r2_zz_full_neg_1.mat;
    line.Aeq = [0,0,1]; line.beq = 0;
    slice = ph_intersectLine(ph_union(reachData.tubes(2:end)),line); 
    bbox = slice.bbox; 
    if(bbox(2,1)>v0(1) && bbox(2,2)>v0(2))
      disp('PASSED: lower and upper bounds of v increase');
    else
      disp(sprintf('FAILED: The bound of v is [%d,%d] at the beginning, while is [%d,%d] at the end.',v0(1),v0(2),bbox(2,1),bbox(2,2))); 
    end
  end
end

  dpll_plot_results(N);
  cra_close;


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% Checkers & Helpers 
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%% Check if v1 >=0
function dpll_check_v1(sets)
  % NOTE: ignore the intiail region where v1 could be negative to make it as a ph
  for i=2:length(sets)
    ph = sets{i};
    if(~ph_isempty(ph))
      if(ph.bbox(2,1)<0)
        disp(sprintf('FAILED: v1 is negative with minimum value %d.',ph.bbox(2,1))); 
      end
    end
  end

function dpll_plot_results(N)
  figure(1); hold on; xlabel('C'); ylabel('V');
  load dpll_r1_lw_pos_1.mat
  initPh = reachData.sets{1};
  poly_display(initPh.hulls{1},'b');
  clo(1) = initPh.bbox(1,1); chi(1) = initPh.bbox(1,2);
  vlo(1) = initPh.bbox(2,1); vhi(1) = initPh.bbox(2,2);
  
  load dpll_r1_lw_ha.mat
  for i=1:2*N-1
    ph = ha.states(i).slices{1};
    hull = ph.hulls{1};
    if(mod(i,2)==1)
      poly_display(hull,'r');
    else 
      poly_display(hull,'b');
    end
    clo(i+1) = ph.bbox(1,1); chi(i+1) = ph.bbox(1,2);
    vlo(i+1) = ph.bbox(2,1); vhi(i+1) = ph.bbox(2,2);
  end
  print -depsc dpll_zz_cv.eps
  
  figure(2); hold on; xlabel('Iters'); ylabel('C'); 
  for i=1:2*N
    if(mod(i,2)==1)
      plot([i,i], [clo(i),chi(i)],'b-*');
    else
      plot([i,i], [clo(i),chi(i)],'b--+');
    end
  end
  print -depsc dpll_zz_c.eps
  
  figure(3); hold on; xlabel('Iters'); ylabel('V'); 
  for i=1:2*N
    if(mod(i,2)==1)
      plot([i,i], [vlo(i),vhi(i)],'b-*');
    else
      plot([i,i], [vlo(i),vhi(i)],'b--+');
    end
  end
  print -depsc dpll_zz_v.eps
  
  
  figure(1);
  load dpll_r1_cw_neg.mat
  K = length(reachData.sets)-1;
  for i=1:length(reachData.sets)-1
    ph = reachData.sets{i};
    poly_display(ph.hulls{1},'k');
    %cclo(i) = ph.bbox(1,1); cchi(i) = ph.bbox(1,2);
    vvlo(i) = ph.bbox(2,1); vvhi(i) = ph.bbox(2,2);
  end
  print -depsc dpll_zz_cv_cw.eps
  
  figure(3); clf; hold on; xlabel('Iters'); ylabel('V'); 
  for i=1:length(vvlo)
    plot([i,i], [vvlo(i),vvhi(i)],'k-*');
  end
  for i=1:2*N
    if(mod(i,2)==1)
      plot(K+[i,i], [vlo(i),vhi(i)],'b-*');
    else
       plot(K+[i,i], [vlo(i),vhi(i)],'b--+');
    end
  end
  print -depsc dpll_zz_v_cw.eps


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% Hybrid Automata 
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Step 1: show all trajectories leave r1/r3
function ha = dpll_ha_r1r3(p,r1)
if(nargin<2), r1 =1; end
  POS_MODE = 1; NEG_MODE=2; KEEP_MODE = 0;
  % initial 
  dim = 3; planes = [1,2;1,3;2,3];
  bbox = [p.cmin,p.cmax; p.vmin, p.vmax; -2*pi, 2*pi]; 
  % global invariant
  if(r1)
    % v <= (c-dc)*freq => -c + v/freq <=  -dc
    inv = lp_create([-1,1/p.fref,0],-p.dc);
  else % r3
    % v >= (c+dc)*freq => c - v/freq <=  -dc
    inv = lp_create([1,-1/p.fref,0],-p.dc);
  end
  inv = lp_and(inv,lp_createByBox(bbox)); 
  % initial
  initBox = bbox;
  initPh = ph_convert(ph_createByBox(dim,planes,initBox),'convex'); 
 
  % mode 1: x(3)/theta > 0 
  % mode 2: x(3)/theta < 0
  A1 = [0,0,-1; 0,0,1]; b1 = [0;2*pi]; 
  A2 = [0,0,1; 0,0,-1]; b2 = [0;2*pi]; 
  inv_pos = lp_create(A1,b1); inv_neg = lp_create(A2,b2);

  % phOpt
  phOpt.fwdOpt = ph_getOpt;
  phOpt.fwdOpt.object = 'ph'; 

  % pos  
  callBacks.exitCond = ha_callBacks('exitCond','phempty');
  callBacks.sliceCond = @(info)(0);
  pname = 'pos'; 
  states(1) = ha_state(pname,@(lp)(dpll_model(lp,p,POS_MODE)),inv_pos,phOpt,callBacks); 
  
  % neg  
  callBacks.exitCond = ha_callBacks('exitCond','phempty');
  callBacks.sliceCond = @(info)(info.fwdStep>1);  % skip the initial region
  nname = 'neg';
  states(2) = ha_state(nname,@(lp)(dpll_model(lp,p,NEG_MODE)),inv_neg,phOpt,callBacks); 

  % source
  source = {'pos','neg'}; 
  
  if(r1)
    sname  = 'dpll_r1';
  else
    sname  = 'dpll_r3';
  end

  ha = ha_create(sname,states,[],source,{initPh,initPh},inv);

% Step 2: climbing the wall
function ha = dpll_ha_r1_cw(p)
  POS_MODE = 1; NEG_MODE=2; KEEP_MODE = 0;
  % initial  & global inv
  dim = 3; planes = [1,2;1,3;2,3];
  bbox = [p.cmin,p.cmax; p.vmin, p.vmax; -2*pi, 2*pi]; 
  inv = lp_createByBox(bbox); 
  initBox  = [p.cmin,p.cmin+1e-3; p.vmin, (p.cmin-p.dc)*p.fref; -2*pi, 0]; 
  initPh = ph_convert(ph_createByBox(dim,planes,initBox),'convex'); 
 
  % mode 1: x(3)/theta > 0 
  % mode 2: x(3)/theta < 0
  A1 = [0,0,-1; 0,0,1]; b1 = [0;2*pi]; 
  A2 = [0,0,1; 0,0,-1]; b2 = [0;2*pi]; 
  inv_pos = lp_create(A1,b1); inv_neg = lp_create(A2,b2);

  % phOpt
  phOpt.fwdOpt = ph_getOpt;
  phOpt.fwdOpt.object = 'ph'; 

  callBacks.exitCond = ha_callBacks('exitCond','phempty');
  pname = ['neg']; 
  states(1) = ha_state(pname,@(lp)(dpll_model(lp,p,KEEP_MODE)),inv_neg,phOpt,callBacks); 
  
  % source
  source = {'neg'}; 

  sname  = 'dpll_r1_cw';
  ha = ha_create(sname,states,[],source,initPh,inv);

% Step 3: leaving the wall to zig-zag
function ha = dpll_ha_r1_lw(p,N)
  if(nargin<2), N = 2; end
  POS_MODE = 1; NEG_MODE=2; KEEP_MODE = 0;

  % initial  & global inv
  dim = 3; planes = [1,2;1,3;2,3];
  bbox = [p.cmin,p.cmax; p.vmin, p.vmax; -2*pi, 2*pi]; 
  inv = lp_createByBox(bbox); 
  % We show all trajctories leaving R1/R3, so only consider R2.
  % c-dc <= V/fref <= c+dc
  inv_r2 = lp_create([1,-1/p.fref,0; -1,1/p.fref,0], [p.dc;p.dc]);
  inv = lp_and(inv,inv_r2);

  vstep2 = p.cmin*p.fref;
  initBox  = [p.cmin,p.cmin+1e-3; vstep2-0.05, vstep2+0.05; 0, 1e-3]; 
  initPh = ph_convert(ph_createByBox(dim,planes,initBox),'convex'); 
 
  % mode 1: x(3)/theta > 0 
  % mode 2: x(3)/theta < 0
  A1 = [0,0,-1; 0,0,1]; b1 = [0;2*pi]; 
  A2 = [0,0,1; 0,0,-1]; b2 = [0;2*pi]; 
  inv_pos = lp_create(A1,b1); inv_neg = lp_create(A2,b2);

  % phOpt
  phOpt.fwdOpt = ph_getOpt;
  phOpt.fwdOpt.object = 'ph'; 

  for i=1:N
    callBacks.exitCond = ha_callBacks('exitCond','phempty');
    callBacks.sliceCond = @(info)(info.fwdStep>1);  % skip the initial region
    pname = ['pos_',num2str(i)]; 
    states(2*i-1) = ha_state(pname,@(lp)(dpll_model(lp,p,POS_MODE)),inv_pos,phOpt,callBacks); 
    if(i>1), trans(2*i-2) = ha_trans(nname,pname,1); end;
  
    callBacks.exitCond = ha_callBacks('exitCond','phempty');
    callBacks.sliceCond = @(info)(info.fwdStep>1);  % skip the initial region
    nname = ['neg_',num2str(i)];
    states(2*i) = ha_state(nname,@(lp)(dpll_model(lp,p,NEG_MODE)),inv_neg,phOpt,callBacks); 
    trans(2*i-1) = ha_trans(pname,nname,1); 
  end

  % source
  source = {'pos_1'}; 

  sname  = 'dpll_r1_lw';
  ha = ha_create(sname,states,trans,source,initPh,inv);

% Step 4: zig-zag by using full models 
function ha = dpll_ha_r2_zz_full(p,v0)
  POS_MODE = 1; NEG_MODE=2; KEEP_MODE = 0;

  % initial  & global inv
  dim = 3; planes = [1,2;1,3;2,3];
  bbox = [p.cmin,p.cmax; p.vmin, p.vmax; -2*pi, 2*pi]; 
  inv = lp_createByBox(bbox); 
  % We show all trajctories leaving R1/R3, so only consider R2.
  % c-dc <= V/fref <= c+dc
  inv_r2 = lp_create([1,-1/p.fref,0; -1,1/p.fref,0], [p.dc;p.dc]);
  inv = lp_and(inv,inv_r2);

  vlo = min(v0); vhi = max(v0); 
  cmin = vlo/p.fref-p.dc; cmax = vhi/p.fref+p.dc;
  initBox  = [cmin,cmax;vlo,vhi;-1e-3,1e-3]; 
  initPh = ph_convert(ph_createByBox(dim,planes,initBox),'convex'); 
 
  % mode 1: x(3)/theta > 0 
  % mode 2: x(3)/theta < 0
  A1 = [0,0,-1; 0,0,1]; b1 = [0;2*pi]; 
  A2 = [0,0,1; 0,0,-1]; b2 = [0;2*pi]; 
  inv_pos = lp_create(A1,b1); inv_neg = lp_create(A2,b2);

  % phOpt
  phOpt.fwdOpt = ph_getOpt;
  phOpt.fwdOpt.object = 'ph'; 

  N = 1;
  for i=1:N
    callBacks.exitCond = ha_callBacks('exitCond','phempty');
    callBacks.sliceCond = @(info)(info.fwdStep>1);  % skip the initial region
    pname = ['pos_',num2str(i)]; 
    states(2*i-1) = ha_state(pname,@(lp)(dpll_model(lp,p,POS_MODE)),inv_pos,phOpt,callBacks); 
    if(i>1), trans(2*i-2) = ha_trans(nname,pname,1); end;
  
    callBacks.exitCond = ha_callBacks('exitCond','phempty');
    callBacks.sliceCond = @(info)(info.fwdStep>1);  % skip the initial region
    nname = ['neg_',num2str(i)];
    states(2*i) = ha_state(nname,@(lp)(dpll_model(lp,p,NEG_MODE)),inv_neg,phOpt,callBacks); 
    trans(2*i-1) = ha_trans(pname,nname,1); 
  end

  % source
  source = {'pos_1'}; 

  sname  = 'dpll_r2_zz_full';
  ha = ha_create(sname,states,trans,source,initPh,inv);

% Step 4: zig-zag computation 
function ha = dpll_ha_r2_zz(p,v0)
  POS_MODE = 1; NEG_MODE=2; KEEP_MODE = 0;
  %v0 = (p.dc*8.99+p.cmin+[-p.dc, p.dc])*p.fref 

  % initial  & global inv
  dim = 3; planes = [1,2;1,3;2,3];
  % c1 = c-c0 = c-v0/fref \in [-dc,dc]
  % v1 = v-v0, where v \in [vlo,vhi] 
  bbox = [-p.dc,p.dc; -diff(v0),diff(v0); -2*pi, 2*pi]; 
  inv = lp_createByBox(bbox); 
  % c1-dc <= V1/fref <= c1+dc
  inv_r2 = lp_create([1,-1/p.fref,0; -1,1/p.fref,0], [p.dc;p.dc]);
  inv = lp_and(inv,inv_r2);

  % v1(0) = 0; c1(0) = c-c0 = c-v0/fref \in [-dc,dc]
  initBox  = [-p.dc, p.dc; -1e-6, 1e-6; -1e-6, 1e-6];
  initPh = ph_convert(ph_createByBox(dim,planes,initBox),'convex'); 
 
  % mode 1: x(3)/theta > 0 
  % mode 2: x(3)/theta < 0
  A1 = [0,0,-1; 0,0,1]; b1 = [0;2*pi]; 
  A2 = [0,0,1; 0,0,-1]; b2 = [0;2*pi]; 
  inv_pos = lp_create(A1,b1); inv_neg = lp_create(A2,b2);

  % phOpt
  phOpt.fwdOpt = ph_getOpt;
  phOpt.fwdOpt.object = 'ph'; 

  N = 1;
  for i=1:N
    callBacks.exitCond = ha_callBacks('exitCond','phempty');
    callBacks.sliceCond = @(info)(info.fwdStep>1);  % skip the initial region
    pname = ['pos_',num2str(i)]; 
    states(2*i-1) = ha_state(pname,@(lp)(dpll_model_zz(lp,p,POS_MODE,v0)),inv_pos,phOpt,callBacks); 
    if(i>1), trans(2*i-2) = ha_trans(nname,pname,1); end;
  
    callBacks.exitCond = ha_callBacks('exitCond','phempty');
    callBacks.sliceCond = @(info)(info.fwdStep>1);  % skip the initial region
    nname = ['neg_',num2str(i)];
    states(2*i) = ha_state(nname,@(lp)(dpll_model_zz(lp,p,NEG_MODE,v0)),inv_neg,phOpt,callBacks); 
    trans(2*i-1) = ha_trans(pname,nname,1); 
  end

  % source
  source = {'pos_1'}; 

  sname  = 'dpll_r2_zz';
  ha = ha_create(sname,states,trans,source,initPh,inv);

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% LDI Models
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% From jijie
%   cdot = sign(ph)*-p.g1
%   vdot = p.g2*p.fref*(c - p.cc);
%   phdot = 1/p.N*v/c - p.fref -p.K*ph ;
% Rewrite by x=c,y=v,z=ph
%   xdot = a = k*-p.g1;  % k=[-1,1,0,0]
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
function ldi = dpll_model(lp,p,mode)
  x=1;y=2;z=3;
  bbox = lp_box(lp);
  avgs = mean(bbox,2);  extras = diff(bbox,[],2)/2;
  switch(mode)
    case 1 % x(3)>=0
      k = 1; 
    case 2 % x(3)<=0
      k = -1; 
    otherwise % saturate
      k = 0; 
  end  

  a = -k*p.g1; b = p.g2*p.fref; c = -b*p.cc; d = 1/p.N; e = -p.K; f = -p.fref;

  A1 = [0,0,0; ...
        b,0,0; ...
        0,0,e]; 
  B1 = [a;c;f];
  U1 = zeros(3,1);  

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
  ldi = int_create(A,B,U);


%% DPLL model for zig-zag mode (v/c close to fref). Check Mark's documents (example2.pdf & strategy.pdf) for details. 
%% NOTE: compared with Mark's document, both jiejie and I keep the vdot the same in the full model: 
%    \dot{v} = g2*fref*(c_cntr-c)
%% NOTE: compared with Jijie's model, I did the following changes. 
%    a. Keep g1 as negative, so a = -k*p.g1, while it's a = k*p.g1 in Jijie's code. They are the same. 
%    b. Keep g2 as negative, so I have vdot = g2*p.fref*(c_cntr-c), while it's g2*p.fref*(c-c_cntr) in jijie's code. 
%    c. In Mark's model, it's 1/(1-c)^2 , not *(1-c^2) for the \dot{\theta} term. Jijie's code uses incorrect term.
%    d. cmid is not mean(c0), should be mean(c0+ct), similar for v
%% NOTE: We made simple change of variables. 
%    c = c0 + c1; 
%    v = v0 + v1;
%    v0 is the value of v when DPLL enters mode 1. i.e v1(0) = 0. 
%    c0 = v0/fref, c1(t) = c(0)-c0;
%% The derivative is 
%    c1dot = cdot = sign(ph)*-p.g1;
%    v1dot = vdot = g2*fref*(c_cntr-c) = g2*fref*(c_cntr-c0-c1);   
%    phdot = 1/N*(v0+v1)/(c0+c1) - fref - k*ph
%  Mark has derived a LDI model for the "(v0+v1)/(c0+c1) - fref" part in his documents.
%  Please note adding 1/N part for the v/c part 
%  As N = 1, we ignore it now

function ldi = dpll_model_zz(lp,p,mode,v0)
  x=1;y=2;z=3;
  bbox = lp_box(lp);
  avgs = mean(bbox,2);  extras = diff(bbox,[],2)/2;
  switch(mode)
    case 1 % x(3)>=0
      k = 1; 
    case 2 % x(3)<=0
      k = -1; 
    otherwise % saturate
      k = 0; 
  end


  c0 = v0/p.fref;
  clo = min(c0)+bbox(x,1); chi = max(c0)+bbox(x,2); cmid = (clo+chi)/2;
  vlo = min(v0)+bbox(y,1); vhi = max(v0)+bbox(y,2); vmid = (vlo+vhi)/2;
  chat = (chi-clo)/(chi+clo); vhat = (vhi-vlo)/(vhi+vlo); 

  a = -k*p.g1; b = p.g2*p.fref; c = -b*(p.cc-mean(c0)); d = 1/p.N; e = -p.K; f = -p.fref;
  g = -p.fref/(1-chat)^2/cmid; h = p.fref/(1-chat)^2/vmid; 
  u1 = abs(b*diff(c0)/2); u2 = abs(p.fref*chat*vhat/(1-chat^2));

  A = [0,0,0; ...
       b,0,0; ...
       g,h,e]; 
  B = [a;c;0];
  U = [0;u1;u2]; 

  ldi = int_create(A,B,U);
