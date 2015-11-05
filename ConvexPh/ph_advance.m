function PHS = ph_advance(ph,model,model2,opt)
  tend = opt.tend; 
  tstep = opt.tstep; 
  tspace = opt.tspace;
  assert(tend>tstep && tstep>tspace);
  maxu = opt.maxu; 

  tcurr = 0; 
  dt = tstep;
  
  PHS = cell(0,1);
  while(tcurr<tend)
    assert(ph.status==0); % projected

    % compute reachable regions in [tcurr,tnext]
    ldi = model(ph.bbox);
    %A = ldi.A; b = ldi.b; u = ldi.u; 
    assert(all(ldi.u<maxu)); % otherwise, no way to move forward

    % predict a region bbox that has model error < maxU
    N = ceil(dt/tspace)+1;
    tps = linspace(0,dt,N); 

    %ldi = int_create(A,b,maxu);
    %ldi = int_create(A,b,u);
    phs = ph_forward(ph, ldi, tps);

    miss = true; 
    while(miss)
      bbox = phs_box(phs(1:N));
      ldi = model(bbox); 
      % TODO: better algorithm, find the largest one ?
      if(any(ldi.u>=maxu))
        assert(N>2);
        N = ceil(N/2);
        tps = tps(1:N); 
      else
        miss = false; 
      end
    end

    % refine models with real error
    %ldi = int_create(A,b,u);

    % continue computation till outside bbox
    gofurther = true; 
    while(gofurther)
      phs = ph_forward(ph,ldi,tps); 
      isc = phs_inBox(phs,bbox);
      if(all(isc))
        % increase time
        tps = [tps,tps(end)+tps];
      else
        gofurther = false;
        % find the last one in the region
        idx = find(~isc,1)-1;  
        assert(idx>1) 
        tps = tps(1:idx); 
      end
    end

    N = length(tps);
    PHS(end+1:end+N)  = phs(1:N); 
    ph = phs{N}; % initial ph for next step
    dt = tps(end);
    tcurr = tcurr+dt;
    dt = dt*2;
    tcurr
    % TODO: predict next dt based history
  end

