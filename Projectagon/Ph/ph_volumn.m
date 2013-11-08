function v = ph_volumn(ph,N)
% v = ph_volumn(ph,N)
%  This function estimate the volumn of a projectagon

if(ph_isempty(ph)), v = 0; return; end;
if(nargin<2||isempty(N)), N = 10^ph.dim; end

dim = ph.dim; bbox = ph.bbox;
mid = mean(bbox,2);
dist = diff(bbox,[],2);
pts = rand(dim,N).*repmat(dist/2,1,N)+repmat(mid,1,N);
isc = ph_containPts(ph,pts);
v = sum(isc)/N*prod(dist);
