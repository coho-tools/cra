% NOTE: The code is from jijie
% this is a version of the DPLL that modeled as mode transition
% give a initial value for x and time t_end, it will plot for c,v,ph_diff
%
% M sequence of mode and TS is the time
% function ydot is the differential equation of the circuit
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

function [M TS]=pll1(x,t_end,pp)
%parameters
if(nargin<1) x=[0.92,1.8,-1]; end
if(nargin<2) t_end = 500;end
if(nargin<3)
    pp=[];
end
p = struct('fref', 2,  'g1', -0.01, 'g1_ph', -0.1,...
    'g2', -0.002,'cmin',0.9,'cmax',1.1,'cc',1.0, 'K',0.8,'N',1);


%end parameters
t0 = 0;
M=[];
TS=[];
mode = getmode(x);
x0 = x;
index =1;
tt = cell(0,1); yy = cell(0,1);
stop_id =5;
while(t0<t_end)
    old_mode = mode;
    switch stop_id
        case 1
            x0(1)=p.cmin;
            mode = 4;
            
        case 2
            x0(1)=p.cmax;
            mode = 3;
            
        case 3
            x0(3)=0;
            mode = old_mode;
            
        case 4
            switch old_mode
                case 1
                    mode = 2;
                    
                case 2
                    mode = 1;
                    
                case 3
                    mode = 2;
                    
                case 4
                    mode = 1;
                    
            end
    end
    M(index) = mode;
    TS(index) = t0;
    index = index+1;
    ode_opt = odeset('Events', @(t, yy)ev1(yy,p));
    [t, v,~,~,ie] = ode45(@(t, y)(ydot(t,y,mode,p)), [t0, t_end], x0,ode_opt);
    tt{end+1} = t';
    yy{end+1} = v';
    t0 = t(end);
    x0 = v(end,:)';
    stop_id = ie;
    if(length(stop_id)>=1)
        stop_id = stop_id(1);
        % disp('more than one');
    end
    
    
end
plot_pll(tt,yy);

end

%circuit model
function y=ydot(t,x,mode,p)

c = x(1);
v = x(2);
ph = x(3);

switch mode
    case 1
        cdot = -p.g1;
    case 2
        cdot = p.g1;
    case 3
        cdot = 0;
    case 4
        cdot = 0;
end
vdot = p.g2*p.fref.*(c - p.cc);
f = v./c;
phdot = f./p.N - p.fref -p.K*ph ;

y=[cdot;vdot;phdot];
end

%detect the transition
function [val, isTerminal, d] = ev1(y,p)

ev = [ [p.cmin-y(1),1,1];
    [y(1)-p.cmax,1,1];
    [(y(3)-2*pi)*(y(3)+2*pi),1,0];
    [y(3), 1, 0]
    
    ];
val = ev(:,1); isTerminal = ev(:,2); d = ev(:,3);

end % ev



function mode=getmode(x)
if(x(3)>=0)
    if(x(1)>=1.1)
        mode = 3;
    else
        mode = 1;
    end
else
    if(x(1)<=0.9)
        mode = 4;
    else
        mode = 2;
    end
end
end

%plot
function plot_pll(t,y)
close all;
% dplot3(t, y);
% figure;
k= size(y,2);
B = [];
T = [];
for i = 1:k
    A = y{1,i}';
    B = [B;A];
    ts = t{1,i}';
    T = [T;ts];
end


figure;
hold on;
plot(T,B(:,1),'b');
plot(T,B(:,2),'g');
plot(T,B(:,3),'r');
legend('c','v','phase');


figure; hold on;
plot(B(:,1),B(:,2));
xlabel('c');
ylabel('v');

% figure; hold on;
% plot(B(:,1),B(:,3));
%
% figure; hold on;
% plot(B(:,2)./B(:,1)-2,B(:,3));

end
