CCohoLPSolver.so: CCohoLPSolver.o 
	g++ -o libCCohoLPSolver.so -shared -I ../../../C -I ../../../C/gnu/profil-2.0/include/ -I ~/gnu/gmp-4.2.1/inclde -L ../../../C/gnu/profil-2.0/lib -L ../../../C/gnu/gmp-4.2.1/lib ../../../C/solver/CohoSolver.o ../../../C/solver/RDCohoSolver.o CCohoLPSolver.o -lgmp -lgmpxx -lBias
CCohoLPSolver.o: coho_jni_CCohoLPSolver.h CCohoLPSolver.cpp 
	g++ -c -I ../../../C -I ../../../C/gnu/profil-2.0/include/ -I ~/gnu/gmp-4.2.1/inclde -L ../../../C/gnu/profil-2.0/lib -L ../../../C/gnu/gmp-4.2.1/lib -o CCohoLPSolver.o CCohoLPSolver.cpp 
.PHONY: clean
clean :
	-rm CCohoLPSolver.o libCCohoLPSolver.so

