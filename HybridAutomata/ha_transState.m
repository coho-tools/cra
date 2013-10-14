function state = ha_transState(name,modelFunc,inv,phOpt)
% It creates a state where the trajectory will eventually leave the state inv. 
% It has the similar interface with ha_state,  except providing callbacks: 
%   exitCond: when the forward reachable region is empty 
%   sliceCond: alway slice the forward regions with gates 
%   others:   nil
if(nargin<2), error('not enough parameters'); end
if(nargin<3), inv = []; end
if(nargin<4), phOpt = []; end

callBacks.exitCond = ha_callBacks('exitCond','transit');
callBacks.sliceCond = ha_callBacks('sliceCond','transit');
state = ha_state(name,modelFunc,inv,phOpt,callBacks); 
