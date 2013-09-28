function [x,xdot] = ph_derivativePt(ph,var,vars,N)
% polys = ph_derivative(ph,var,vars,N)
% This function is similar with ph_derivative.
% However, it samples points inside the ph and calcualte its derivative on the point
% NOTE, this does not work with input signals
	if(nargin<4||isempty(N))
		N = 5;
	end
	x = []; xdot = [];
	if(isempty(ph))
		return;
	end
	assert(~any(var==vars));
	bbox = ph.bbox;
	nd = length(vars)+1; M = 2*N^nd;
	x = linspace(bbox(var,1),bbox(var,2),2*N); % sample more points x
	x = repmat(x,1,N^(nd-1));
	v = repmat(bbox(vars,1),1,M)+rand(nd-1,M).*repmat(bbox(vars,2),1,M);
	lp = lp_pickup(ph.hullLP,[var;vars(:)]);
	d = [x;v]; [~,I] = sort([var;vars(:)]);
	ind = all(lp.A*d(I,:) <= repmat(lp.b,1,M),1);
	x = x(:,ind); v = v(:,ind);
	if(~isempty(x))
		%% TODO: need Coho Circuit Modeling tool 
		circuit = circuit_config('get','circuit');
		pts = zeros(circuit.dim,size(x,2));
		pts(var,:) = x; pts(vars,:) = v;
		vdots = circuit.dut.vdot(pts);
		xdot = vdots(var,:);
	end
end

