function state = ha_fastStableState(name,modelFunc,inv,phinfo,fwdOpt,entryAct,stepAct,exitAct,tbnd,maxstep,tph,tol)
if(nargin<2), error('not enough parameters'); end
if(nargin<3), inv = []; end
if(nargin<4), phinfo = []; end
if(nargin<5), fwdOpt = []; end
if(nargin<6), entryAct = []; end
if(nargin<7), stepAct = []; end
if(nargin<8), exitAct = []; end
if(nargin<9), tbnd = []; end
if(nargin<10), maxstep = []; end
if(nargin<11), tph = []; end
if(nargin<12), tol = []; end
if(isempty(tbnd)), tbnd = [0,Inf]; end
if(length(tbnd)<2), tbnd = [tbnd,Inf]; end

exitFunc = ha_funcTemp('exitFunc','faststable',tbnd(2),maxstep,tph,tol);
doSlice = ha_funcTemp('doSlice','faststable',tbnd(1));
state = ha_state(name,modelFunc,inv,phinfo,fwdOpt,exitFunc,doSlice,entryAct,stepAct,exitAct);
