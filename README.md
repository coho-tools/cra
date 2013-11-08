CRA: Coho Reachability Analysis Tool

FAQ: 
=====================
1. How to install CRA
   sh install.sh
=====================
2. How to use CRA, small example
   cra_open;
   % Tell CRA what's the dynamics 
   % The function must accept a LP input and returns a LDI models
   cra_cfg('set','modelFunc' <function handle>);
   % Create the init projectagon. 
   ph = ph_create(dim,planes,hulls....); % see Projectagon packages
   % Advance the projectagon
   fwdPh = ph_advance(ph); 
   % Show the results
   ph_display(fwdPh);
   cra_close;   
=====================
3. Any more examples ?
   ./example/
   ./Projectagon/test/
=====================
4. What's LDI models
   Linear differential inclusion models, i.e. 
   xdot \in Ax+b+/-u; 
=====================
5. What's a projectagon 
   It's a data structure to represent and operate high-dim objects.  
   It represents the object by projecting it onto two-dim subspaces. 
=====================
6. What's the Java/Matlab threads
   The CRA is mainly implemented in Matlab. It requires arbitrary precesion computations, which are implmented in Java. Mainly LP solvers and polygon operations. 

