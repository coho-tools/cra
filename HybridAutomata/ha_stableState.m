function state = ha_stableState(name,modelFunc,inv,phinfo,fwdOpt,entryAct,stepAct,exitAct,tbnd)
if(nargin<2), error('not enough parameters'); end
if(nargin<3), inv = []; end
if(nargin<4), phinfo = []; end
if(nargin<5), fwdOpt = []; end
if(nargin<6), entryAct = []; end
if(nargin<7), stepAct = []; end
if(nargin<8), exitAct = []; end
if(nargin<9), tbnd = []; end
if(isempty(tbnd)), tbnd = [0,Inf]; end
if(length(tbnd)<2), tbnd = [tbnd,Inf]; end


exitFunc = ha_funcTemp('exitFunc','stable',tbnd(1),tbnd(2));
doSlice = ha_funcTemp('doSlice','stable',tbnd(1));
state = ha_state(name,modelFunc,inv,phinfo,fwdOpt,exitFunc,doSlice,entryAct,stepAct,exitAct);
