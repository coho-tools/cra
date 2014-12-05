% This function creates a hybrid automata for a N-stage Rambus-ring oscillator circuits. 
% N must be an even number. 
function ha = rro_ha(varargin)
  opt = struct('N',2,'r',1,'fwdOpt',ph_getOpt,'callBacks',[], ...
               'type','convex', 'haName',[],'rpath','.'); 
  opt = utils_struct(opt,'set',varargin{:});
  N = opt.N; r = opt.r; fwdOpt = opt.fwdOpt; callBacks = opt.callBacks; 
  type = opt.type; haName = opt.haName; rpath = opt.rpath;
  if(isempty(callBacks))
    callBacks.exitCond = ha_callBacks('exitCond','maxFwdStep',100); 
  end
  if(isempty(haName)), haName = ['rro_',num2str(N)]; end

  % states
  phOpt.fwdOpt = fwdOpt;
  callBacks.sliceCond = ha_callBacks('sliceCond','never');  % do not need slice
  states(1) = ha_state('s1',@(lp)(rro_ldi(lp,r)),[],phOpt,callBacks); 
  source = 's1'; % source

  % initial, HLHL...HL
  dim = 2*N; planes = [[1:2*N; [2:2*N,1]]';[1:N;N+1:2*N]']; 
  bbox([1:2:N,N+2:2:2*N],:) = repmat([0.9,1],N,1); 
  bbox([2:2:N,N+1:2:2*N],:) = repmat([-1,-0.9],N,1); 
  if(N==2), bbox = bbox/2; end % orbit is different for N=2;
  initPh = ph_convert(ph_createByBox(dim,planes,bbox),type);
  inv = lp_createByBox(repmat([-1,1],2*N,1));

  ha = ha_create(haName,states,[],source,initPh,inv,rpath);
end

% linear differential equation
function ldi = rro_ldi(lp,r)
  bbox = lp_box(lp);
  [A,b,u] = rro_model(bbox,r); 
  ldi  = int_create(A,b,u);
end
