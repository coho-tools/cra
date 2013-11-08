function [v,x,status] = cplex_lp(H,f,A,b,IndEq,QC,LB,UB)
% [v,x,status] = cplex_lp(H,f,A,b,IndEq,QC,LB,UB)
% Input
%     min    0.5*x'*H*x + f'*x
%      x
%     s.t.:  A x {'<=' | '='} b
% 			 A(IndEq,:) x = b(IndEq,:).
%            x' * QC(i).Q * x + QC(i).L * x <= QC(i).r,  i=1,...,nQC
%            x >= LB
%            x <= UB
% Output
% v: optimal value
% x: optimal point
% status: exit status from cplexint
%  0: error before getting the result
%  1: optimal
%  2: unbounded
%  3: infeasible
%  4: infeasible or unbounded
% For more, see CPX_STAT http://www.uni-koeln.de/rrzk/software/fachuebergreifend/or/cplex_doku/refcallablelibrary/html/index/index.html

if(nargin<5)
	IndEq = [];
end
if(nargin<6)
	QC = [];
end
if(nargin<7)
	LB=[];
end
if(nargin<8)
	UB=[];
end

% get license
persistent license;
while(isempty(license)||~license)
	license = cplex_request;
end 

[x,v,status] = cplexint(H,f,A,b,IndEq,QC,LB,UB);
%% optimal basis can be found using detail.slack

% I have put the code to get a license in the "cplex_request" function
%OPTIONS.lic_rel=1e7;
%MAXTRY=3600;
%finish = false; trytime=0; 
%while(~finish && trytime<MAXTRY)
%	try
%		[x,v,status,detail] = cplexint(H,f,A,b,IndEq,QC,LB,UB,[],[],OPTIONS);
%		finish = true;
%	catch % no cplex licence, pause and try to get again. 
%		clear functions;
%		pause(1);
%		trytime=trytime+1;
%	end
%end
%
