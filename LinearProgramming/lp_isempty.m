function ise = lp_isempty(lp)
	if( isempty(lp) || (isempty(lp.A)&&isempty(lp.b)) ) 
		ise = true;
	else
		ise = false;
	end
