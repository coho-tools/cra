function [fwdPh,ph] = ph_construct(ph)
% [fwdPh,ph] = ph_construct(ph)

opt = ph.fwd.opt;
object = opt.object;
tol = opt.tol;

if(strcmpi(object,'ph'))
	hulls = ph.fwd.projs; 
	for i=1:ph.ns
		switch(ph.type)
			case 0
				error('impossible');
			case 1
				hulls{i} = poly_simplify(hulls{i},tol);
			case 2
				bbox = [min(hulls{i},[],2),max(hulls{i},[],2)];
				hulls{i} = poly_createByBox(bbox);
			otherwise
				error('unknown projectagon type');
		end
	end
	polys = hulls;
else 
	projs = cell(ph.ns);
	for i=1:ph.ns
		projs{i} = cell(0,1);
	end
	for i=1:ph.ns
		slice = ph.fwd.slices(i);
		for j=1:slice.nf;
			face = slice.faces(j);
			ps = face.projs;
			if(length(ps)==1)
				projs{i}{end+1} = ps{1};
			else % project all
				for k=1:length(ps)
					projs{k}{end+1} =ps{k};
				end
			end
		end
	end	
	hulls = cell(ph.ns,1); polys = cell(ph.ns,1); 
	for i=1:ph.ns
		switch(ph.type)
			case 0
			  poly = poly_union(projs{i}); 
				poly = poly_simplify(poly,tol);
				hull = poly_convexHull(poly);
				hull = poly_simplify(hull,tol);
			case 1
				pts = cell2mat(projs{i});
				hull = poly_convexHull(pts);
				hull = poly_simplify(hull,tol);
				poly = hull;
			case 2
				pts = cell2mat(projs{i});
				bbox = [min(pts,[],2),max(pts,[],2)];
				hull = poly_createByBox(bbox);
				poly = hull;
			otherwise
				error('unknown projectagon type');
		end
		hulls{i} = hull; polys{i} = poly;
	end
end

fwdPh =  ph_create(ph.dim,ph.planes,hulls,polys,ph.type,false);
ph.fwd.fwdPh = fwdPh;
