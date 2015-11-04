function bloatLP = lp_bloat(lp, bloatAmt,includeBox)
% bloatLP = lp_bloat(lp, bloatAmt,includeBox)
%  The function bloat each variable by bloatAmt. 
%  Each point in the feasible region of lp can move by bloatAmt. 
%  bloatLP is feasible region that contains all such points.
% 
%  bloatAmt: 1) a scalar, bloat same on each direction
%            2) a vector, decrease and increase bloat amount are the same
%            3) nx2 matrix: bloatAmt(i,1) is the decrease bloat amount on direction i
%                           bloatAmt(i,2) is the increase bloat amount on direction i
%            bloatAmt must be positive
%  includeBox: A bounding box should be added to the constraint to contronl error. 
% 		If lp does not include redundant bounding box, this function will add four
% 		contrains before bloating.  True by default. 

if(nargin<3||isempty(includeBox))
    includeBox=true;
end;

if(lp_isempty(lp))
	bloatLP = []; return;
end

lp = lp_norm(lp);

% process bloatAmt; 
dim = size(lp.A,2);
switch size(bloatAmt,1)
    case 1
        bloatAmt = repmat(bloatAmt,dim,2);
    case dim
        %nothing
    otherwise
        error('lp_bloat, bloatAmt must be scalar or nx1 vector or nx2 matrix');
end
switch size(bloatAmt,2)
    case 1
        bloatAmt = repmat(bloatAmt,1,2);
    case 2
        %nothing
    otherwise
        error('lp_bloat, bloatAmt must be scalar or nx1 vector or nx2 matrix');
end;
bloatAmt = reshape(bloatAmt,dim*2,1); %[x-;x+];

%  adding boxLP
if(~includeBox)
    lp = lp_and(lp,lp_createByBox(lp_box(lp))); 
end;

% compute the moving distance of each face 
A1 = zeros(size(lp.A)); A2=A1; % A1 is the index of dxl dyl and A2 for dxr dyr
index = lp.A<0; A1(index) = -lp.A(index);
index = lp.A>0; A2(index) =  lp.A(index);
b = [A1,A2]*bloatAmt;
bloatLP = lp_create(lp.A, lp.b + b);
