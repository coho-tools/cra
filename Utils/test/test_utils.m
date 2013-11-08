function test_utils
  test_log;
	test_struct;

function test_log
	disp('  Open the stdout as the log');
  log_open;

	disp('  Write Hello World to the log');
  log_write('Hello Word!');

	disp('  Save a random value by log_save');
  a = rand(10,1);
  log_save('data1','a');

	disp('  Save all variabls by log_save');
  log_save('data2');

	disp('  Close the log');
  log_close;

function test_struct
  S = struct('a','str','b',10);
  
  % disp
  str = utils_struct(S,'2str');
  disp('The struct is');
  fprintf(str);
  
  % list of names
  names = utils_struct(S,'names');
  disp('The struct has the following fields');
  for i=1:length(names)
  	disp(names{i});
  end;
  
  % check
	disp('Check the has op');
  valids = utils_struct(S,'has','a','b','c');
  if(~all(valids==[1;1;0]))
  	error('check function incorrect');
  end;
  
  % get
	disp('Check the get op');
  value = utils_struct(S,'get','a');
  if(~strcmp(value,'str')) 
  	error('get function incorrect');
  end;
  values = utils_struct(S,'get','a','b');
  if(values{2}~=10)
  	error('get function incorrect');
  end;
  
  % set, add and remove
	disp('Check the set op');
  S = utils_struct(S,'set','a',1);
  if(S.a~=1)
  	error('set function incorrect');
  end;

	disp('Check the add op');
  S = utils_struct(S,'add','c',S);
  fprintf(utils_struct(S,'2str'));
	
	disp('Check the remove op');
  S = utils_struct(S,'remove','c');
  fprintf(utils_struct(S,'disp'));
  
  % check error 
	disp('Check the errors ');
  [values,status] = utils_struct(S,'get','c');% expect an error
  if(status==0)
  	error('error check not work');
  end
  [values,status] = utils_struct(S,'set','c','error');% expect an error
  if(status==0)
		% Add the fields directly for easy use, so no error any more
  	% error('error check not work');
  end
  [values,status] = utils_struct(S,'rm','c','error');% expect an error
  if(status==0)
  	error('error check not work');
  end
  [values,status] = utils_struct(S,'add','a','error');% expect an error
  if(status==0)
  	error('error check not work');
  end
