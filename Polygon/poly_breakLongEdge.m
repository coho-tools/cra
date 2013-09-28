function poly = poly_breakLongEdge(poly,MAXLEN)
% poly = poly_breakLongEdge(poly,MAXLEN)
% The function break long edges into shorter ones. 
%   poly: polygon with long edges
%   MAXLEN: the maximum length allowed. 0.1 by default
%   poly: the result is a new polygon without edges longer than MAXLEN

if(nargin<2||isempty(MAXLEN))
    MAXLEN = 0.1;
end

n = size(poly,2); % # of polygon edges
diffs = diff(poly(:,[1:end,1]),1,2);  % [dx;dy]
lens = sqrt(sum(diffs.^2,1)); % edge length
multLen = ceil(lens./MAXLEN); % # of short edges
m = sum(multLen); % # of new polygon edges
if(m > n)
	% shift position of new edge
	mm = max(multLen);
    k = repmat((0:mm-1)',1,mm)./repmat(1:mm,mm,1); 
    chops = k(:,multLen);
    chops = chops(chops<1); 
	% compute index of new edge
    ind = repVec(1:n,multLen); % repeat i by multLen(i) times 
    poly = poly(:,ind)+diffs(:,ind).*repmat(chops',2,1);
end

function v = repVec(data,num)
% v = repVec(data,num)
% The function repeat data(i) by num(i) times
% 	data,num:	two vectors with the same length
% 	v: sum(num)x1 vector. 
%	Examples: 
%		data = [1,2,3]; num = [3,0,2];
%		v = [1;1;1;3;3]

% reshape to row vector first
v1 = reshape(data,1,[]); v2 = reshape(num,1,[]);
n = length(v1); 
% repmat each element by max(num) times.
m = max(v2);
v = repmat(v1,m,1); 
% compute the indices
ind = (repmat((1:m)',1,n) <= repmat(v2,m,1));
% pickup the elements.
v = v(ind);
