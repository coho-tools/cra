function state = ha_transState(name,modelFunc,inv,phinfo,fwdOpt,entryAct,stepAct,exitAct)
% This function creates a state where the trajectory will eventually leave this region. 
% It has the similar interface with ha_state, but here we provide some default value for 
% exitFunc and doSlice functions
if(nargin<2), error('not enough parameters'); end
if(nargin<3), inv = []; end
if(nargin<4), phinfo = []; end
if(nargin<5), fwdOpt = []; end
if(nargin<6), entryAct = []; end
if(nargin<7), stepAct = []; end
if(nargin<8), exitAct = []; end

exitFunc = ha_funcTemp('exitFunc','transit');
doSlice = ha_funcTemp('doSlice','transit');
state = ha_state(name,modelFunc,inv,phinfo,fwdOpt,exitFunc,doSlice,entryAct,stepAct,exitAct);
