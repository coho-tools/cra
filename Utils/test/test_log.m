addpath('../..');
coho_addpath('Utils');

log_open;


log_write('Hello Word!');
a = rand(10,1);
log_save('data1','a');
log_save('data2');

log = log_close;
