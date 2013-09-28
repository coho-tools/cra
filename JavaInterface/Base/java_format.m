function nf = java_format(where)
% nf = java_format('write');
% nf = java_format('read');

fmt = cra_cfg('get','javaFormat');
switch(lower(where))
	case 'write'
		switch(fmt)
			case 'hex'
				nf = '$%bx';
			case 'dec'
				nf = '%e';
			otherwise
				error('does not support');
		end;
	case 'read'
		nf = ['''',fmt,''''];
	otherwise
		error('does not support');
end;
