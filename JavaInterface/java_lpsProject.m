function hulls = java_lpsProject(lps,xs,ys,tols)
  N = length(lps);  hulls = cell(N,1);
  if(length(tols)==1)
    tols =repmat(tols,N,1);
  end
  if(~iscell(xs))
    xs = repmat({xs},N,1); 
  end
  if(~iscell(ys))
    ys = repmat({ys},N,1);
  end
  jNum = cra_cfg('get','javaThreads');

  % the input buffer can not hold too many requests 
  cap = jNum*50;
  sidx = [1:cap:N,N+1];

  for iter = 1:length(sidx)-1
    % dispatch requests
    for i= sidx(iter):sidx(iter+1)-1
      lp = lps{i}; 
      x = xs{i}; y = ys{i}; tol = tols(i);
      curr = mod(i,jNum); curr = curr+jNum*(curr==0);
      fprintf('dispatch %i-th job with TC = %i to the %i-th thread\n',i,cra_cfg('get','javaTC')+1,curr)
      java_useThread(curr); 
      java_lpsProject_dispatch(lp,x,y,tol);
    end
    
    % get results
    for i= sidx(iter):sidx(iter+1)-1
      x = xs{i}; y = ys{i}; 
      curr = mod(i,jNum); curr = curr+jNum*(curr==0);
      fprintf('get %i-th result from the %i-th thread\n',i, curr)
      java_useThread(curr);
      hulls{i} = java_lpsProject_get(x,y);
    end
  end
end

function java_lpsProject_dispatch(lp, x, y,tol)
  n = size(lp.A,2);
  
  % send LP to java process
  A = -lp.A; b = -lp.b; % Java side uses Ax >= b
  Aeq = zeros(0,n); beq = zeros(0,1);
  pos = zeros(n,1);
  java_writeComment('BEGIN lp_project'); % comment in matlab2java
  java_writeLabel; % comment in java2matlab
  java_writeMatrix(A,'A'); % Ax >= b 
  java_writeMatrix(b,'b');
  java_writeMatrix(Aeq,'Aeq'); % Aeq x = beq
  java_writeMatrix(beq,'beq');
  java_writeBoolMatrix(pos,'pos'); % x[pos] >= 0?
  if(~isempty(lp.bwd))
	  java_writeMatrix(bwd,'bwd'); % bwdT
	  java_writeMatrix(fwd,'fwd'); % fwdT 
	  java_writeLine('lp = lpGeneral(Aeq, beq, A, b, pos,bwd,fwd);'); 
  else
  	java_writeLine('lp = lpGeneral(Aeq, beq, A, b, pos);'); 
  end
  % projection directions
  java_writeMatrix(x,'xx');
  java_writeMatrix(y,'yy');
  % project
  java_writeLine(sprintf('lpProj = lp_project(lp, xx, yy, %f);',tol));
  java_writeLine(sprintf('println(lp_point(lpProj),%s);',java_format('read')));
  java_writeDummy;
  java_writeComment('END lp_project'); % comment in matlab2java
end

function hull = java_lpsProject_get(x,y)
  % get the result
  hull = java_readMatrix; 
  if(size(hull,2)<3) 
  	hull = zeros(2,[]); 
  	return; 
  end; 
  
  % convert 2D
  hull = [x,y]\hull; 
end
