% This function creates a hybrid automata for a N-stage inveter-ring oscillator circuits. 
% N must be an odd number. 
function ha = iro_ha(varargin)
  opt = struct('N',3,'fwdOpt',ph_getOpt,'callBacks',[], ...
               'type','convex', 'haName',[],'rpath','.'); 
  opt = utils_struct(opt,'set',varargin{:});
  N = opt.N; K = (N-1)/2; fwdOpt = opt.fwdOpt; callBacks = opt.callBacks; 
  type = opt.type; haName = opt.haName; rpath = opt.rpath;
  if(isempty(callBacks))
    callBacks.exitCond = ha_callBacks('exitCond','maxFwdStep',100); 
  end
  if(isempty(haName)), haName = ['iro_',num2str(N)]; end

  % states
  phOpt.fwdOpt = fwdOpt;
  callBacks.sliceCond = ha_callBacks('sliceCond','never');  % do not need slice
  states(1) = ha_state('s1',@(lp)(iro_ldi(lp)),[],phOpt,callBacks); 
  source = 's1'; % source

  % initial, HLHL...HLH
  dim = N; planes = [1:N; [2:N,1]]'; 
  bbox(1:2:N,:) = repmat([0.9,1],K+1,1); 
  bbox(2:2:N,:) = repmat([-1,-0.9],K,1); 
  if(N==3), bbox = bbox/2; end % orbit is different for N=3;
  initPh = ph_convert(ph_createByBox(dim,planes,bbox),type);
  inv = lp_createByBox(repmat([-1,1],N,1));

  ha = ha_create(haName,states,[],source,initPh,inv,rpath);
end

% linear differential equation
function ldi = iro_ldi(lp)
  bbox = lp_box(lp);
  [A,b,u] = iro_model(bbox); 
  ldi  = int_create(A,b,u);
end
