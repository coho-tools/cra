function ise = lp_isempty(lp)
	if( isempty(lp) || ...
		(isempty(lp.A)&&isempty(lp.b)&&isempty(lp.Aeq)&&isempty(lp.beq)) )
		ise = true;
	else
		ise = false;
	end
