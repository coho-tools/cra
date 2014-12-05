% This script is used to plot performance data

grids = 2:2:10;
close all

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
disp('Working on data for ph method ...');
load results/perf/ph/times.mat
Tph = times;
figure; clf; hold on; 
xlabel('N'); ylabel('T (seconds)'); 
title('Running time for N-stage inverter oscillator');
plot(grids,times(grids),'r-*');
print -depsc results/perf_plot/ph_time.eps

figure; clf; hold on; 
xlabel('N'); ylabel('T^{(1/2)}');
title('Running time for N-stage inverter oscillator');
plot(grids,times(grids).^(1/2),'r-*');
print -depsc results/perf_plot/ph_time_2.eps

figure; clf; hold on; 
xlabel('N'); ylabel('T^{(1/3)}'); 
title('Running time for N-stage inverter oscillator');
plot(grids,times(grids).^(1/3),'r-*');
print -depsc results/perf_plot/ph_time_3.eps


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
disp('Working on data for face method ...');
load results/perf/face/times.mat
Tface = times;
figure; clf; hold on; 
xlabel('N'); ylabel('T (seconds)'); 
title('Running time for N-stage inverter oscillator');
plot(grids,times(grids),'r-*');
print -depsc results/perf_plot/face_time.eps

figure; clf; hold on; 
xlabel('N'); ylabel('T^{(1/2)}');
title('Running time for N-stage inverter oscillator');
plot(grids,times(grids).^(1/2),'r-*');
print -depsc results/perf_plot/face_time_2.eps

figure; clf; hold on; 
xlabel('N'); ylabel('T^{(1/3)}'); 
title('Running time for N-stage inverter oscillator');
plot(grids,times(grids).^(1/3),'r-*');
print -depsc results/perf_plot/face_time_3.eps


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
disp('Working on data for non-convex method ...');
load results/perf/noncov/times.mat
Tnocov = times;
figure; clf; hold on; 
xlabel('N'); ylabel('T (seconds)'); 
title('Running time for N-stage inverter oscillator');
plot(grids,times(grids),'r-*');
print -depsc results/perf_plot/noncov_time.eps

figure; clf; hold on; 
xlabel('N'); ylabel('T^{(1/2)}');
title('Running time for N-stage inverter oscillator');
plot(grids,times(grids).^(1/2),'r-*');
print -depsc results/perf_plot/noncov_time_2.eps

figure; clf; hold on; 
xlabel('N'); ylabel('T^{(1/3)}'); 
title('Running time for N-stage inverter oscillator');
plot(grids,times(grids).^(1/3),'r-*');
print -depsc results/perf_plot/noncov_time_3.eps

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
disp('Compare the results ...'); 
figure; clf; hold on; 
xlabel('N'); ylabel('ratio'); 
title('Relative peformance of different configurations'); 
plot(grids,Tface(grids)./Tph(grids),'g-d');
plot(grids,Tnocov(grids)./Tph(grids),'r-*');
print -depsc results/perf_plot/cmp_0.eps 

figure; clf; hold on; 
xlabel('N'); ylabel('ratio'); 
title('Relative peformance of different configurations'); 
plot(grids,Tnocov(grids)./Tface(grids),'r-*');
print -depsc results/perf_plot/cmp_1.eps 
