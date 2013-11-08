function ph = ph_chplanes(ph,planes)
% ph = ph_chplanes(ph,planes)
% This function is used to update planes of ph 
if(nargin<2), error('not enough parameters'); end
if(ph_isempty(ph)), return; end
if(all(size(ph.planes)==size(planes)) && all(ph.planes(:)==planes(:))), return; end;

%ns = size(planes,1);
[hulls,polys] = ph_project(ph,planes);
ph = ph_create(ph.dim,planes,hulls,polys,ph.type,ph.iscanon);
