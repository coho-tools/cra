function PHS = ph_advance(ph,model,opt,isCRA)
  tend = opt.tend; 
  tstepL = opt.tstepL; 
  tstepS = opt.tstepS;
  assert(tend>tstepL && tstepL>=tstepS);
  maxu = opt.maxu; 

  tcurr = 0; 
  dt = tstepL;
  
  PHS = cell(0,1);
  while(tcurr<tend)
    assert(ph.status==0); % projected

    % compute reachable regions in [tcurr,tnext]
    ldi = model(ph.bbox);

    % predict a region bbox that has model error < maxU
    N = ceil(dt/tstepS)+1;
    tps = linspace(0,dt,N); 
    tps = tps(2:end);

    phs = ph_forward(ph, ldi, tps);

    N = length(tps);
    PHS(end+1:end+N)  = phs(1:N); 
    ph = phs{N}; % initial ph for next step
    % NOTE: the condition number can't be too large 
    cond(ph.hullLP.bwd)
    if(isCRA)
      ph = ph_createByHull(ph.dim, ph.planes, ph.hulls);
    end
%    if(cond(ph.hullLP.bwd)>1.2)
%      num = ph_boxNum(ph);
%      if(0&&max(num)<1.2)
%        disp('projecting to bbox')
%        ph = ph_createByBox(ph.bbox,ph.planes); 
%      else
%        disp('projecting to hull')
%        ph = ph_createByHull(ph.dim, ph.planes, ph.hulls);
%      end
%    end
    tcurr = tcurr+dt;
    %tcurr
  end

