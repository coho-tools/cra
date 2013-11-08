clc;
echo on;

% HERE ARE SEVERAL SIMPLE TEST CALLS TO THE CPLEXINT - MATLAB MEX INTERFACE
% FOR CPLEX. RUNNING THIS DEMO SHOULD GIVE YOU AN IDEA HOW TO USE INTERFACE.
% FOR MORE DETAILS YOU SHOULD CHECK EXISTING HELP FILE.
%
%     Copyright (C) 2001-2005  Mato Baotic
%

pause;
clc;

% First problem we want to solve is an LP
%    min 2x
%    s.t. -x <= 1
% We should get as an output:
% xmin =
%     -1
% fmin =
%     -2
% solstat =
%      1
% details = 
%     statstring: 'optimal'
%     solnmethod: 2
%       solntype: 1
%       pfeasind: 1
%       dfeasind: 1
%       lpsolved: []
%           dual: -2
%          slack: 0
%        qcslack: []
%        redcost: 0
%-----------------------------------------------------------
[xmin,fmin,solstat,details]=cplexint([],2,-1,1)

pause;
clc;

% Second problem we want to solve is QP
%    min 0.5* x*5*x + 2x
%    s.t. -4*x <= 1
% We should get as an output:
% xmin =
%   -0.25000000000000
% fmin =
%   -0.34375000000000
% solstat =
%      1
% details = 
%     statstring: 'optimal'
%     solnmethod: 4
%       solntype: 2
%       pfeasind: 1
%       dfeasind: 1
%       lpsolved: []
%           dual: -0.18750000000000
%          slack: 0
%        qcslack: []
%        redcost: 0
%-----------------------------------------------------------
[xmin,fmin,solstat,details]=cplexint(5,2,-4,1)

pause;
clc;

% We can also include equality constraints,
%    min 5*x1 + 2*x2
%    s.t. -4*x1 + x2 <= 10
%            x1 - 0.5 x2 = 4
%          2*x1 + x2 <= 12
% For this puprose we use 5th input argument were we specify indices
% of equality constraints (in our case INDEQ=[2])
%
% We should get as an output:
% xmin =
%     -9
%    -26
% fmin =
%    -97
% solstat =
%      1
% details = 
%     statstring: 'optimal'
%     solnmethod: 2
%       solntype: 1
%       pfeasind: 1
%       dfeasind: 1
%       lpsolved: []
%           dual: [3x1 double]
%          slack: [3x1 double]
%        qcslack: []
%        redcost: [2x1 double]
%-----------------------------------------------------------
[xmin,fmin,solstat,details]=cplexint([],[5;2],[-4 1; 1 -0.5; 2 1],[10; 4; 12],[2])
pause;
clc;


% Now we could solve MILP
%    min 1.6*x
%    s.t. x<=1, x is integer between -100 and 200
% We should get as an output:
% xmin =
%   -100
% fmin =
%   -160
% solstat =
%    101
% details = 
%     statstring: 'integer optimal solution'
%     solnmethod: []
%       solntype: []
%       pfeasind: []
%       dfeasind: []
%       lpsolved: 0
%           dual: []
%          slack: 101
%        qcslack: []
%        redcost: []
%------------------------------------------------------------------------------
[xmin,fmin,solstat,details]=cplexint([],[1.6],[1],[1],[],[],[-100],[200],'I')

pause;
clc;

% Finaly we could solve an MIQP problem
%    min 0.5*x*x+1.6*x
%    s.t. x<=1, x is integer
% We should get as an output:
% xopt =
%     -2
% fopt =
%   -1.20000000000000
% lpsolved =
%    101
% stat = 
%     statstring: 'integer optimal solution'
%     solnmethod: []
%       solntype: []
%       pfeasind: []
%       dfeasind: []
%       lpsolved: 0
%           dual: []
%          slack: 3
%        qcslack: []
%        redcost: []
%----------------------------------------------------------
[xmin,fmin,solstat,details]=cplexint(1,1.6,1,1,[],[],[],[],'I')

pause;
clc;


% Let us solve the following (more complicated) MIQP problem
%
%       Minimize
%        obj: - x1 - 2 x2 - 3 x3 - x4
%             + 0.5 ( 33x1*x1 + 22*x2*x2 + 11*x3*x3
%                    -  12*x1*x2 - 23*x2*x3 )
%       Subject To
%        c1: - x1 + x2 + x3 + 10x4  <= 20
%        c2: x1 - 3 x2 + x3         <= 30
%        c3:       x2       - 3.5x4  = 0
%       Bounds
%        0 <= x1 <= 40
%        0 <= x4 <= 3
%       Binary
%         x4
%       End
%-----------------------------------------------------------

H=     [33    -6     0      0;...
        -6    22   -11.5    0;...
         0   -11.5  11      0;...
         0     0     0      0];         % quadratic cost
f=[-1 -2 -3 -1]';                       % linear cost
A=[-1 1 1 10; 1 -3 1 0; 0 1 0 -3.5];    % constraints
b=[20; 30; 0];                          % constraints
INDEQ=[3];                              % equality constraints
LB=[0; -inf; -inf; 0];                  % lower bound on variables
UB=[40; inf; inf; 3];                   % upper bound on variables
VARTYPE=['C' 'C' 'C' 'B']';             % variable type
PARAM=[];                               % use default CPLEX parameters
OPTIONS=[];                             % use standard options
OPTIONS.save_prob='test_cplex.lp';      % save problem to the file test_cplex.lp



pause;

[xmin,fmin,solstat,details]=cplexint(H,f,A,b,INDEQ,[],LB,UB,VARTYPE,PARAM,OPTIONS)

pause;
clc;


% If we neglect Q matrix we can solve above problem as an MILP
%-------------------------------------------------------------
[xmin,fmin,solstat,details]=cplexint([],f,A,b,INDEQ,[],LB,UB,VARTYPE,PARAM,OPTIONS)

pause;
clc

% We can choose to display intermediate (internal) results on the screen
% and save the problem to some file
%------------------------------------------------------------------------
OPTIONS.verbose = 2;                    % display all intermidiate results on screen
OPTIONS.save_prob='test_cplex.lp';      % save problem to the file test_cplex.lp
pause;

[xmin,fmin,solstat,details]=cplexint([],f,A,b,INDEQ,[],LB,UB,VARTYPE,PARAM,OPTIONS)

pause;
clc;

% We can also change any default solver parameter.
% Let's change CPX_PARAM_PREIND to CPX_OFF
% (check CPLEX Ref manual for details)
%------------------------------------------------------------------------
OPTIONS=[];PARAM=[];PARAM.int=[1030 0];
[xmin,fmin,solstat,details]=cplexint([],f,A,b,INDEQ,[],LB,UB,VARTYPE,PARAM,OPTIONS)

% Note the difference in number of LP's that are being solved now
% compared to before (details.lpsolved)

pause;
clc;


% Finally the ultimate test: Quadratically Constrained MIQP
%
% We use previous problem
%
%       Minimize
%        obj: - x1 - 2 x2 - 3 x3 - x4
%             + 0.5 ( 33x1*x1 + 22*x2*x2 + 11*x3*x3
%                    -  12*x1*x2 - 23*x2*x3 )
%       Subject To
%        c1: - x1 + x2 + x3 + 10x4  <= 20
%        c2: x1 - 3 x2 + x3         <= 30
%        c3:       x2       - 3.5x4  = 0
%       Bounds
%        0 <= x1 <= 40
%        0 <= x4 <= 3
%       Binary
%         x4
%       End
% with the addition of a quadratic constraint
%        q1: [ x1^2 + x2^2 + 20*x3^3 + x4^4] <= 1.0
%------------------------------------------------------------------------
PARAM=[];
QC(1).Q=eye(4); QC(1).Q(3,3)=20; QC(1).L=zeros(1,4); QC(1).r = 1;

[xmin,fmin,solstat,details]=cplexint(H,f,A,b,INDEQ,QC,LB,UB,VARTYPE,PARAM,OPTIONS)

pause;
clc;

% In the end, we will show how execution of a code can be made faster if we are
% willing to not release CPLEX license after every call to CPLEXINT. To this
% end we use OPTIONS.lic_rel (see cplexint.m for details).
%
% OPTIONS=[];
% tstart=clock; 
% for ii=1:200,
%     [xmin,fmin,solstat,details]=cplexint([],f,A,b,[],[],LB,UB,[],[],OPTIONS); 
% end
% TIME_OLD=etime(clock,tstart)
% 
% OPTIONS.lic_rel=200;
% tstart=clock;
% for ii=1:200,
%     [xmin,fmin,solstat,details]=cplexint([],f,A,b,[],[],LB,UB,[],[],OPTIONS); 
% end
% TIME_NEW=etime(clock,tstart)
%
% compare the times between two runs!

echo off;

OPTIONS=[];
tstart=clock; 
for ii=1:200,
    [xmin,fmin,solstat,details]=cplexint([],f,A,b,[],[],LB,UB,[],[],OPTIONS); 
end
TIME_OLD=etime(clock,tstart)

OPTIONS.lic_rel=200;
tstart=clock;
for ii=1:200,
    [xmin,fmin,solstat,details]=cplexint([],f,A,b,[],[],LB,UB,[],[],OPTIONS); 
end
TIME_NEW=etime(clock,tstart)
