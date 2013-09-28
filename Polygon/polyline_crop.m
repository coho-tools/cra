function pts = polyline_crop(pl,x) 
% pts = polyline_crop(pl,x) 
% The function finds the part of a polyline in the range of [x(1),x(2)]
	if(x(1)>x(2))
		error('x(1) can not be greater than x(2)');
	end
	if(x(1)>max(pl(1,:)) || x(2)<min(pl(1,:)))
		error('x is out of the range of polyline');
	end

	ind = ( x(1)<=pl(1,:) & pl(1,:)<=x(2) );
	pts = pl(:,ind);
	lind = find( pl(1,(1:end-1)) < x(1) & x(1) < pl(1,(2:end)));
	if(~isempty(lind))
		p1 = pl(:,lind); p2 = pl(:,lind+1);
		xx = x(1);
		x1 = p1(1); y1 = p1(2); x2 = p2(1); y2 = p2(2);
		yy = (y1*(x2-xx)+y2*(xx-x1))/(x2-x1);
		pts = [[xx;yy],pts];
	end
	hind = find( pl(1,(1:end-1)) < x(2) & x(2) < pl(1,(2:end)));
	if(~isempty(hind))
		p1 = pl(:,hind); p2 = pl(:,hind+1);
		xx = x(2);
		x1 = p1(1); y1 = p1(2); x2 = p2(1); y2 = p2(2);
		yy = (y1*(x2-xx)+y2*(xx-x1))/(x2-x1);
		pts = [pts,[xx;yy]];
	end
end % polyline_crop
