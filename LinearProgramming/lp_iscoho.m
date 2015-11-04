function iscoho = lp_iscoho(lp)
% iscoho = lp_iscoho(lp)
%  True if lp is a coho lp. 
%  coho lp is a standard lp which has only 1 or 2 non-zero elements in each
%  row of A matrix. 

if(lp_isempty(lp))
    iscoho = false;
    return;
end

c = sum(lp.A~=0,2);
iscoho = all( c==1 | c==2 );
