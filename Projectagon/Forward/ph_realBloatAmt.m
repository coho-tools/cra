function [realBloatAmt,ph] = ph_realBloatAmt(ph)

opt = ph.fwd.opt;
object = opt.object;
bloatAmt = ph.fwd.bloatAmt;


realBloatAmt = zeros(ph.dim,2);
if(strcmpi(object,'ph'))
	projs = ph.fwd.projs;
	for i=1:ph.ns
		index = ph.planes(i,:);
		% use the bloatAmt as the opt dir. use 1e-6 when bloat is zero.
		w = max(1e-6,reshape(bloatAmt(index,:),[],1));
		% hullLP
		hull = ph.hulls{i};
		hullLP = lp_createByHull(hull);
		bbox = [min(hull,[],2),max(hull,[],2)];
		bboxLP = lp_createByBox(bbox);
		lp = lp_and(hullLP,bboxLP);
		rba = ph_realBloatAmt_help(lp,projs{i},w);
		realBloatAmt(index,:) = max(realBloatAmt(index,:),rba);
	end
else
	for i=1:ph.ns
		slice = ph.fwd.slices(i); index = ph.planes(i,:);
		rba = zeros(2,2);
		% use the bloatAmt as the opt dir. use 1e-6 when bloat is zero.
		w = max(1e-6,reshape(bloatAmt(index,:),[],1));
		for j=1:slice.nf
			face = slice.faces(j);
			if(length(face.projs)==1)
				proj = face.projs{1};
			else
				proj = face.projs{i}; % only check this one
			end
			% The 2D lp for the polygon edge is
			lp = lp_dims(face.projLP,'project',index); % reduce to 2d
			rbaf = ph_realBloatAmt_help(lp,proj,w);
			rba = max(rba,rbaf);
		end
		realBloatAmt(index,:) = max(realBloatAmt(index,:),rba);
	end
end

ph.fwd.realBloatAmt = realBloatAmt;

function realBloatAmt = ph_realBloatAmt_help(lp,hull,f)
	dim = 2;
	A = lp.A; b = lp.b;
	% Ax<=b after relax is
	% Ax<=b+AA*[bloatAmt-;bloatAmt+]. (see lp_bloat function).
	A1 = zeros(size(A)); A2 = A1;
	ind = A<0; A1(ind) = -A(ind);
	ind = A>0; A2(ind) = A(ind);
	AA = [A1,A2];
	% solve the optimization problem
	% min f(dx1,dx2,dy1,dy2)
	% s.t AA*[dx1;dy1;dx2;dy2] >= A*pt-b for all pts 
	%     [dx1;dy1;dx2;dy2] >=0;
	n = size(hull,2); M = repmat(AA,n,1);
	p = reshape(A*hull-repmat(b,1,n),[],1);
	M = -[M;eye(dim*2)]; p = -[p;zeros(dim*2,1)];
	lp = lp_create(M,p);
	
	% solve the lp
	[v,x] = lp_opt(lp,f);
	realBloatAmt = reshape(x,[],2);
%end
