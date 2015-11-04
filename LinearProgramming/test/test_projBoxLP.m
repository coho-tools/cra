
  bbox = [0,1;0,1];
  A = [1,0;0,1];
  m = int_create(A,[0;0],[0;0]);
	lp = lp_createByBox(bbox);
  fwdT = 1;
  x = [1;0]; y = [0; 1];
  alp = int_forward(lp,m,fwdT);
  h1 = java_lpProject(alp,x,y,0.01)
  h2 = lp_projectBoxLP(alp,x,y)
  h1
  h2
