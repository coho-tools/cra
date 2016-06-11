% Using CRA on the helicopter demo
function test 
  %for jNum = [1,2,4,8,16]
  for jNum = 8
    cra_cfg('set','javaThreads',jNum);
    heli
  end
end

function heli
  cra_open(true);

  dim = 28;
  coords = (1:dim)'; 
  planes = [coords coords+1];
  planes(dim,2) = 1;
  
  bbox = [zeros(dim, 1) 0.1*ones(dim,1)];
	lp = lp_createByBox(bbox);
  m = heli_model();
 
  %T = 30;
  T = 10;
  fwdT = 0; fwdStep = 1; lps= {}; 
  lps{fwdStep} = lp; 
  tic 
  for fwdT=0.1:0.1:T
    fwdStep=fwdStep+1;
    lps{fwdStep} = int_forward(lp,m,fwdT);
  end
  toc

  hulls = cell(length(lps),dim);
  tic
  %for j=1:dim
  for j=2 
    fprintf('working on the %i-th plane\n',j);
	  plane = planes(j,:);
		m = eye(dim); 
		x = m(:,plane(1)); y = m(:,plane(2));
    hs = java_lpsProject(lps,x,y,0.0);
    hulls(:,j) = hs;
  end
  toc

  save('data','lps','hulls');
 
  cra_close; 
end
