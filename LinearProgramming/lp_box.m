function bbox = lp_box(lp)
% bbox = lp_box(lp)
% The function computes a bounding box for lp 
% Input:
% 	lp: the lp to solve
% Output:
% 	bbox: bbox(:1/2) is the lower/upper bound

if(lp_isempty(lp)), bbox = zeros(0,2); return; end;
n = size(lp.A,2);

[vs,~,flags] = lp_opt(lp,[eye(n),-eye(n)]);
if(any(flags~=0))
	bbox = zeros(0,2);
else
	bbox = [vs(1:n),-vs(n+1:2*n)];
end
