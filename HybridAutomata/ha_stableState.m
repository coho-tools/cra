function state = ha_stableState(name,modelFunc,inv,phOpt,tbnd)
% It creates a state where the trajectory will stable in a region.  
% It has the similar interface with ha_state,  except providing callbacks: 
%   exitCond:  reachble state converges when minT satisfied OR maxT is reached
%   sliceCond: slice when minT satisfied or complete 
%   others:   nil
% Parameters:
%   tbnd:  [minT,maxT]: can't be empty
if(nargin<5), error('not enough parameters'); end
if(isempty(tbnd)||length(tbnd)~=2), error('tbnd must be [minT,maxT]'); end;

callBacks.exitCond = ha_callBacks('exitCond','stable',tbnd(1),tbnd(2));
callBacks.sliceCond = ha_callBacks('sliceCond','stable',tbnd(1));
state = ha_state(name,modelFunc,inv,phinfo,phOpt,callBacks); 
