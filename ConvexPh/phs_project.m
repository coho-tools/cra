function phs = phs_project(phs)
% assume phs are advanced phs of a ph with same LDI model
  lps = cell(length(phs),1);
  for i=1:length(phs)
    lps{i} = phs{i}.hullLP;
  end

  ph = phs{1}; ns = size(ph.planes,1); m = eye(ph.dim); 
  hulls = cell(length(phs),ns);
  for j=1:ns
    plane = ph.planes(j,:);
		x = m(:,plane(1)); y = m(:,plane(2));
    if(ph.type==2)
      % TODO: make a uniform interface for it
      for i=1:length(phs)
        hulls{i,j} = lp_projectBoxLP(lps{i},x,y);
      end
    else
      hulls(:,j) = java_lpsProject(lps,x,y,0.01);
    end
  end

  for i=1:length(phs)
    ph = phs{i}; 
    ph = ph_projUpdate(ph,hulls(i,:)); 
    phs{i} = ph;
  end
end
