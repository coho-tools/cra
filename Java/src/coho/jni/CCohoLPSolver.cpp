#include "coho_jni_CCohoLPSolver.h"
#include "matrix/DenseVector.h"
#include "matrix/CohoMatrix.h"
#include "solver/RDCohoSolver.h"
#include "solver/LPBasis.h"
#include "matrix/MatrixCheck.h"
#include "matrix/MatrixConvert.h"
#include "number/Rational.h"
#include <exception>
using namespace std;
class OutOfMemoryErr:public exception{};
struct JAVAREF{
	jclass cl_CCohoLPSolver;
	jclass cl_DoubleCohoMatrix;
	jclass cl_DoubleMatrix;
	jclass cl_IntegerMatrix;
	jclass cl_RationalMatrix;
	jclass cl_CohoDouble;
	jclass cl_CohoInteger;
	jclass cl_CohoRational;
	jclass cl_BigInteger;
	
	jfieldID vID_DCM_nrows;
	jfieldID vID_DCM_ncols;
	jfieldID vID_DCM_pos;
	jfieldID vID_DCM_data;
	jfieldID vID_DM_data;
	jfieldID vID_IM_data;
	jfieldID vID_RM_data;
	jfieldID vID_CD_v;
	jfieldID vID_CI_v;
	jfieldID vID_CR_nume;
	jfieldID vID_CR_deno;
	
	jmethodID mID_CCLPS_setOptResult;
	jmethodID mID_BI_toString;
}ref;

JNIEXPORT void JNICALL Java_coho_jni_CCohoLPSolver_initCSolver
  (JNIEnv *env, jclass jc){
  	//initialize for interval
  	//Interval::init();
  	
  	ref.cl_CCohoLPSolver = jc;
  	ref.cl_DoubleCohoMatrix = env->FindClass("coho/lp/solver/DoubleCohoMatrix");
  	ref.cl_DoubleMatrix = env->FindClass("coho/common/matrix/DoubleMatrix");
  	ref.cl_IntegerMatrix = env->FindClass("coho/common/matrix/IntegerMatrix");
  	ref.cl_RationalMatrix = env->FindClass("coho/common/matrix/APRMatrix");
  	ref.cl_CohoDouble =  env->FindClass("coho/common/number/CohoDouble");
  	ref.cl_CohoInteger = env->FindClass("coho/common/number/CohoInteger");
  	ref.cl_CohoRational = env->FindClass("coho/common/number/CohoAPR");
  	ref.cl_BigInteger =env->FindClass("java/math/BigInteger");
  	

  	ref.vID_DCM_nrows = env->GetFieldID(ref.cl_DoubleCohoMatrix,"nrows","I");
  	ref.vID_DCM_ncols = env->GetFieldID(ref.cl_DoubleCohoMatrix,"ncols","I");
  	ref.vID_DCM_pos = env->GetFieldID(ref.cl_DoubleCohoMatrix,"pos","[[I");
  	ref.vID_DCM_data = env->GetFieldID(ref.cl_DoubleCohoMatrix,"data","[[Lcoho/common/number/CohoNumber;");
  	ref.vID_DM_data = env->GetFieldID(ref.cl_DoubleMatrix,"data","[[Lcoho/common/number/CohoNumber;");
  	ref.vID_IM_data = env->GetFieldID(ref.cl_IntegerMatrix,"data","[[Lcoho/common/number/CohoNumber;");
  	ref.vID_RM_data = env->GetFieldID(ref.cl_RationalMatrix,"data","[[Lcoho/common/number/CohoNumber;");
  	ref.vID_CD_v = env->GetFieldID(ref.cl_CohoDouble,"v","D");
  	ref.vID_CI_v = env->GetFieldID(ref.cl_CohoInteger,"v","I");
  	ref.vID_CR_nume = env->GetFieldID(ref.cl_CohoRational,"numerator","Ljava/math/BigInteger;");
  	ref.vID_CR_deno = env->GetFieldID(ref.cl_CohoRational,"denominator","Ljava/math/BigInteger;");
  	
  	ref.mID_CCLPS_setOptResult = env->GetMethodID(ref.cl_CCohoLPSolver,"setOptResult","(D[I[D)V");
  	ref.mID_BI_toString = env->GetMethodID(ref.cl_BigInteger,"toString","()Ljava/lang/String;");
  	
  	if(env->ExceptionCheck()){
  		env->ExceptionDescribe();
  		return;
  	}
}
/*
 * Read a coho matrix (not dual) from java
 */
inline CohoMatrix<double> JNU_ParseCohoMatrix(JNIEnv* env, jobject m){
	if(env->PushLocalFrame(10)<0){
		cout<<"Out of memory"<<endl;
		throw OutOfMemoryErr();
	}	
  	jint nrows = env->GetIntField(m,ref.vID_DCM_nrows);
  	jint ncols = env->GetIntField(m,ref.vID_DCM_ncols);
  	CohoMatrix<double> result(nrows,ncols,true);//it's coho matrix

  	jobjectArray var_pos = (jobjectArray)env->GetObjectField(m,ref.vID_DCM_pos);//int[][]
  	jobjectArray var_data = (jobjectArray)env->GetObjectField(m,ref.vID_DCM_data);//CohoDouble[][]
  	for(int i=0; i<nrows; i++){
  		if(env->PushLocalFrame(10)<0){
  			cout<<"Out of memory"<<endl;
  			throw OutOfMemoryErr();
  		}
  		jintArray var_pos_row = (jintArray)env->GetObjectArrayElement(var_pos,i);//int[]
  		jobjectArray var_data_row = (jobjectArray)env->GetObjectArrayElement(var_data,i);//CohoDouble[]
  		jint* pos = env->GetIntArrayElements(var_pos_row,0);//int*
  		jobject element = env->GetObjectArrayElement(var_data_row,0);//CohoDouble
  		jdouble v1 = env->GetDoubleField(element,ref.vID_CD_v);//double
  		result.assign(i,pos[0],v1);//first element on this row
  		
  		element = env->GetObjectArrayElement(var_data_row,1);
  		if(element!=NULL){//second element on this row
  			jdouble v2 = env->GetDoubleField(element,ref.vID_CD_v);
  			result.assign(i,pos[1],v2);
  		}
  		env->PopLocalFrame(NULL);  		
  	}
	env->PopLocalFrame(NULL);  		
  	return result;
}
/*
 * Read a double vector (n*1 not 1*n) from java
 */
inline DenseVector<double> JNU_ParseDoubleVector(JNIEnv* env, jobject v){
	if(env->PushLocalFrame(10)<0){
		cout<<"Out of memory"<<endl;
		throw OutOfMemoryErr();
	}
	jobjectArray var_data = (jobjectArray)env->GetObjectField(v,ref.vID_DM_data);//[][]
	jint nrows = env->GetArrayLength(var_data);
	DenseVector<double> result(nrows);	
	for(int i=0; i<nrows; i++){
  		if(env->PushLocalFrame(10)<0){
  			cout<<"Out of memory"<<endl;
  			throw OutOfMemoryErr();
  		}
		jobjectArray var_data_row = (jobjectArray)env->GetObjectArrayElement(var_data,i);//[]
		jobject element = (jobject)env->GetObjectArrayElement(var_data_row,0);//CohoDouble
		jdouble v = env->GetDoubleField(element,ref.vID_CD_v);//double
		result.assign(i,v);
		env->PopLocalFrame(NULL);
	}
	env->PopLocalFrame(NULL);
	return result;
}
/*
 * Read a integer matrix
 */
inline DenseVector<unsigned> JNU_ParseIntegerVector(JNIEnv* env, jobject v){
	if(env->PushLocalFrame(10)<0){
		cout<<"Out of memory"<<endl;
		throw OutOfMemoryErr();
	}
	jobjectArray var_data = (jobjectArray)env->GetObjectField(v,ref.vID_IM_data);//[][]
	jint nrows = env->GetArrayLength(var_data);
	DenseVector<unsigned> result(nrows);	
	for(int i=0; i<nrows; i++){
		if(env->PushLocalFrame(10)<0){
			cout<<"Out of memory"<<endl;
			throw OutOfMemoryErr();
		}
		jobjectArray var_data_row = (jobjectArray)env->GetObjectArrayElement(var_data,i);//[]
		jobject element = (jobject)env->GetObjectArrayElement(var_data_row,0);//CohoInteger
		jint v = env->GetIntField(element,ref.vID_CI_v);//double
		result.assign(i,v);
		env->PopLocalFrame(NULL);
	}
	env->PopLocalFrame(NULL);
	return result;
}
/*
 * Parse a big integer
 */
inline BigInteger JNU_ParseBigInteger(JNIEnv* env, jobject jbi){
	if(env->PushLocalFrame(10)<0){
		cout<<"Out of memory"<<endl;
		throw OutOfMemoryErr();
	}
	//call toString function of BigInteger
	jstring str = (jstring)env->CallObjectMethod(jbi,ref.mID_BI_toString);//TODO is it safe?
	int len = env->GetStringLength(str);
	char* outbuf = new char[env->GetStringUTFLength(str)+1];
	env->GetStringUTFRegion(str,0,len,outbuf);
	BigInteger result(outbuf);
	delete outbuf;
	env->PopLocalFrame(NULL);
	return result;
} 
/*
 * Parse a rational 
 */
inline Rational JNU_ParseRational(JNIEnv* env, jobject jr){
	if(env->PushLocalFrame(10)<0){
		cout<<"Out of memory"<<endl;
		throw OutOfMemoryErr();
	}
	jobject nume = (jobject)env->GetObjectField(jr,ref.vID_CR_nume);
	jobject deno = (jobject)env->GetObjectField(jr,ref.vID_CR_deno);
	Rational result(JNU_ParseBigInteger(env,nume),JNU_ParseBigInteger(env,deno));
	env->PopLocalFrame(NULL);
	return result;
} 
/*
 *Read a rational matrix
 */ 
inline DenseVector<Rational> JNU_ParseRationalVector(JNIEnv* env, jobject v){
	if(env->PushLocalFrame(10)<0){
		cout<<"Out of memory"<<endl;
		throw OutOfMemoryErr();
	}
	jobjectArray var_data = (jobjectArray)env->GetObjectField(v,ref.vID_RM_data);//[][]
	jint nrows = env->GetArrayLength(var_data);
	DenseVector<Rational> result(nrows);
	for(int i=0; i<nrows; i++){
		if(env->PushLocalFrame(10)<0){
			cout<<"Out of memory"<<endl;
			throw OutOfMemoryErr();
		}
		jobjectArray var_data_row = (jobjectArray)env->GetObjectArrayElement(var_data,i);//[]
		jobject element = (jobject)env->GetObjectArrayElement(var_data_row,0);//CohoRational
		result.assign(i,JNU_ParseRational(env,element));
		env->PopLocalFrame(NULL);
	}
	env->PopLocalFrame(NULL);
	return result;
}

/*
 * Write the result to the java side
 */
inline void JNU_SetOptResult(JNIEnv* env, jobject cl, const LPResult<Rational>& result){
	if(env->PushLocalFrame(10)<0){
		cout<<"Out of memory"<<endl;
		throw OutOfMemoryErr();
	}
  	DenseVector<unsigned> basis = result.getOptBasis().basis();
  	int n = basis.length();
	jint* tbasis = new jint[n];
	for(int i=0; i<n; i++){
		tbasis[i] = basis[i];
	}	
	jintArray jbasis = env->NewIntArray(n);
	env->SetIntArrayRegion(jbasis,0,n,tbasis);
	delete tbasis;
	
	DenseVector<double> point = MatrixConvert::convert(result.getOptPoint());
	jdouble* tpoint = new jdouble[n];
	for(int i=0; i<n; i++){
		tpoint[i] = point[i];
	}
	jdoubleArray jpoint = env->NewDoubleArray(n);
	env->SetDoubleArrayRegion(jpoint,0,n,tpoint);
	delete tpoint;
 	env->CallVoidMethod(cl,ref.mID_CCLPS_setOptResult,
  		result.getOptCost().get_d(),jbasis,jpoint);
  	env->PopLocalFrame(NULL);  	
}
//NOTE can not use interval solver now
//RDCohoSolver *solver=NULL;
CohoSolver *solver=NULL;
JNIEXPORT void JNICALL Java_coho_jni_CCohoLPSolver_createCSolver
  (JNIEnv *env, jclass cl, jobject A, jobject b){
  	CohoMatrix<double> localA = JNU_ParseCohoMatrix(env,A);
  	DenseVector<double> localB = JNU_ParseDoubleVector(env,b);
  	if(solver!=NULL){
  		delete solver;
  	}
  	try{  		
  		//solver = new RDCohoSolver(CohoLP<double>(localA,localB));
  		//solver = new RDCohoSolver(localA,localB);
  		solver = new CohoSolver(localA,localB);
  	}catch(...){
  		cout<<"Exception happens in the solver"<<endl;
  	}
  	return;
}
JNIEXPORT void JNICALL Java_coho_jni_CCohoLPSolver_cOpt__Lcoho_common_matrix_DoubleMatrix_2
  (JNIEnv *env, jobject cl, jobject c){  	
  	DenseVector<double> localC = JNU_ParseDoubleVector(env,c);
  	LPResult<Rational> result = solver->opt(localC);
  	JNU_SetOptResult(env,cl,result);
}
JNIEXPORT void JNICALL Java_coho_jni_CCohoLPSolver_cOpt__Lcoho_common_matrix_DoubleMatrix_2Lcoho_common_matrix_IntegerMatrix_2I
  (JNIEnv *env, jobject cl, jobject c, jobject basis, jint evict){
  	DenseVector<double> localC = JNU_ParseDoubleVector(env,c);
  	DenseVector<unsigned> localBasis = JNU_ParseIntegerVector(env,basis);
  	LPBasis b(localBasis);
  	LPResult<Rational> result = solver->opt(localC,b,evict);
 	JNU_SetOptResult(env,cl,result);
}
JNIEXPORT void JNICALL Java_coho_jni_CCohoLPSolver_cOpt__Lcoho_common_matrix_APRMatrix_2
  (JNIEnv *env, jobject cl, jobject c){
  	DenseVector<Rational> localC = JNU_ParseRationalVector(env,c);  
  	LPResult<Rational> result = solver->opt(localC);
  	JNU_SetOptResult(env,cl,result);
}
JNIEXPORT void JNICALL Java_coho_jni_CCohoLPSolver_cOpt__Lcoho_common_matrix_APRMatrix_2Lcoho_common_matrix_IntegerMatrix_2I
  (JNIEnv *env, jobject cl, jobject c, jobject basis, jint evict){
  	DenseVector<Rational> localC = JNU_ParseRationalVector(env,c);
  	DenseVector<unsigned> localBasis = JNU_ParseIntegerVector(env,basis);
  	LPBasis b(localBasis);
  	LPResult<Rational> result = solver->opt(localC,b,evict);
 	JNU_SetOptResult(env,cl,result);
}
///*
// * Conver from a DenseVector<double> to a java DoubleMatrix
// */
// jobject JNU_CreateDoubleVector(JNIEnv* env, const DenseVector<double>& point){
//	int n = point.length();
//
////	jint* tmp1 = new jint[n];
////	for(int i=0; i<n; i++){
////		tmp1[i] = i;
////	}	
////	jintArray value1 = env->NewIntArray(n);
////	env->SetIntArrayRegion(value1,0,n,tmp1);
////	delete tmp1;
//	
//	jdouble* tmp = new jdouble[n];
//	for(int i=0; i<n; i++){
//		tmp[i] = point[i];
//	}
//	cout<<1<<endl;
//	jdoubleArray value = env->NewDoubleArray(n);
//	cout<<1.5<<endl;
//	env->SetDoubleArrayRegion(value,0,n,tmp);
//	cout<<2<<endl;
//	delete tmp;
//	//TODO I don't know why it crashs
//	jobjectArray matrix = env->NewObjectArray(1,env->FindClass("[D"),NULL);//can not save it, not constant
//	env->SetObjectArrayElement(matrix,0,value);
//	//jobject jresult = env->NewObject(ref.cl_DoubleMatrix,ref.mID_DM_c,value);
//	cout<<3.5<<endl;
//	jobject jresult = env->NewObject(ref.cl_DoubleMatrix,ref.mID_DM_c,matrix);
//	cout<<3<<endl;
//	env->DeleteLocalRef(value);	
//	return jresult;
//}
///*
// * Conver from a DenseVector<unsigned> to a java IntegerMatrix
// */
//inline jobject JNU_CreateIntVector(JNIEnv* env, const DenseVector<unsigned>& basis){
//	int n = basis.length();
//	jint* tmp = new jint[n];
//	for(int i=0; i<n; i++){
//		tmp[i] = basis[i];
//	}	
//	jintArray value = env->NewIntArray(n);
//	env->SetIntArrayRegion(value,0,n,tmp);
//	delete tmp;
//	
//	jobjectArray matrix = env->NewObjectArray(1,env->FindClass("[I"),NULL);//can not save it, not constant
//	env->SetObjectArrayElement(matrix,0,value);
//	jobject jresult = env->NewObject(ref.cl_IntegerMatrix,ref.mID_IM_c,matrix);
//	env->DeleteLocalRef(value);
//	env->DeleteLocalRef(matrix);
//	return jresult;
//}

