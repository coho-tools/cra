function c = utils_palette(n, cc)
  if(nargin < 2), cc = 'rygcbmk'; end;
  if(size(cc,2)==3)
  	c = cc(mod(n-1, length(cc))+1,:);
  else
	c = cc(mod(n-1, length(cc))+1);
  end
end % palette
