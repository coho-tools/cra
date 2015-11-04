function ph = ph_create(dim, planes, hulls, hullLP, bbox, bboxLP, type, status) 
% 	dim:   dimension of projectagon
%   planes:projection planes. plane(i,1/2) is the x/y axis of the i^th
%   		projection plane. slice i is projected onto the plane spanned 
% 			by orthogonal axises specified by planes(i,:) 
%   hulls: hulls{i} is the matrix of 2D hull vertices for slice i
% STATUS:
%   0: normal
%   1: advanced, not projected
% TYPE
%   0: general projectagon (impossible for this package)
%   1: convex projectagon

ph = struct('dim',dim, 'type',type, 'status', status, ...
	'hullLP',hullLP, 'bboxLP',bboxLP, ...
  'planes',planes, 'hulls',{hulls}, 'bbox',bbox );

  % TODO: add time or not?
  %'time',0 ...
