planes = [1,2;1,3]; 
bbox = [1.0,1.2;-0.1,0.1;0.9,1.1];
ph = ph_createByBox(bbox,planes); 

opt.tend = 8;
opt.tstep = 1;
opt.tspace = 0.001;
opt.maxu = 0.2; 
phs = ph_advance(ph,@(bbox)(model(bbox)), opt);
