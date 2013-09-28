function ph = ph_rand(dim,planes,type,iscanon,N)
if(nargin<3||isempty(type))
	type = 0;
end
if(nargin<4||isempty(iscanon))
	iscanon = false;
end
if(nargin<5||isempty(N))
	N = 30;
end

while(true)
	switch(type)
		case {0,1} 
			ns = size(planes,2); 
			hulls = cell(ns,1); polys = cell(ns,1);
			for i=1:ns 
				polys{i} = poly_rand(N,type==1); 
				hulls{i} = poly_convexHull(polys{i});
			end
			ph = ph_create(dim,planes,hulls,polys,type,false);
		case 2
			bbox = sort(rand(dim,2),2)*2-1;
			ph = ph_createByBox(dim,planes,bbox);
		otherwise
			error('do not support'); 
	end
	if(iscanon) 
		ph = ph_canon(ph);
	end
	if(~ph_isempty(ph))
		return;
	end
end
