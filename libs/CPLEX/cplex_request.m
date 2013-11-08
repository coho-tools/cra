function succ = cplex_request(nf,ntries)
if(nargin<1||isempty(nf))
	nf = 1e6; % 1 millions
end
if(nargin<2||isempty(ntries))
	ntries = 3600; % 60 minutes 
end

OPTIONS.lic_rel=nf;
succ = false; i = 0;
while(~succ && i<ntries) 
	try 
		[xmin,fmin,solstat,details]=cplexint([],2,-1,1,[],[],[],[],[],[],OPTIONS); 
		succ = true;
	catch
		i = i+1;
		pause(1); % wait a second;
	end
end
