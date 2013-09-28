package coho.common.matrix;
import coho.common.number.*;
import coho.common.util.*;
/*
 * 
 * There are three ways to create a specified matrix (such as DoubleMatrix). 1) new 2) static create 3) object.convert
 * It's not recommend to create BasicMatrix<CohoNumber> then use static.typeCast to convert it.
 * Although it works. 
 * 
 * If the matrix is created from an 1-d array, a length*1 vector matrix is produced. It is the same
 * for every kind of Matrix. 
 */
public class BasicMatrix<V extends CohoNumber> implements Matrix{
	protected final V[][] data; //We can't assume data is nrow*ncol. e.g CohoMatrix
	protected final int nrows, ncols;
	protected final CohoType type;
	public int nrows(){
		return nrows;
	}
	public int ncols(){
		return ncols;
	}
	public CohoType elementType(){
		return type;
	}
	public V[][] data(){
		return data;
	}
	
	/*********************************
	 * Constructors 
	 *********************************/
	/*
	 * This constructor is used by subclass. We don't need to copy the data. 
	 * The subclass should make sure the data is never changed outside.
	 */
	protected BasicMatrix(CohoType type, int nrows, int ncols,V[][] data){
		this.data = data;
		this.nrows = nrows;
		this.ncols = ncols;
		//this.type = data[0][0].type();//XXX Danger
		this.type = type;
	}
	//force V and type to be consistent
	//myType should be the class of the CohoNumber, like CohoBoolean.class for BasicMatrix<CohoBoolean>.
	private BasicMatrix(CohoType type, int nrows, int ncols){
		this.type = type;
		this.nrows = nrows;
		this.ncols = ncols;
		data = createArray(nrows, ncols);
	}
	/*
	 * o should be a object of type V. (like a seed?) 
	 */
	public BasicMatrix(V o, int nrows, int ncols){
		this(o.type(),nrows,ncols);
	}
	public BasicMatrix(V o, int[] size){
		this(o.type(),size[0],size[1]);
	}
	public BasicMatrix(V[][] data){
		this(data[0][0].type(),data.length,data[0].length);
		assign(data);
	}
	/*
	 * Create a length*1 vector matrix
	 */
	public BasicMatrix(V[] data){
		this(data[0].type(),data.length,1);
		assign(data);		
	}
	//it's uselss to have BasicMatrix(Matrix m) or BasicMatrix(BasicMatrix<V> m).
	
	//create matrix with same type
	public BasicMatrix<V> convert(int nrows, int ncols){
		return new BasicMatrix<V>(type,nrows,ncols);
	}
	public BasicMatrix<V> convert(int[] size){
		return new BasicMatrix<V>(type,size[0],size[1]);
	}
	public BasicMatrix<V> convert(){
		return new BasicMatrix<V>(type,nrows,ncols);
	}
	//create matrix and also assign values. 
	//NOTE: DO NOT CLONE NOW. return m if it is BasicMatrix<V>. a method to clone this matrix
	public BasicMatrix<V>convert(Matrix m){
		//NOTE: for performance
		if(m instanceof BasicMatrix && m.elementType()==elementType()){
			return (BasicMatrix<V>)m;
		}
		BasicMatrix<V> r = convert(m.nrows(),m.ncols());
		return r.assign(m);
	}
	public BasicMatrix<V> convert(CohoNumber[][] data){
		BasicMatrix<V> r = convert(data.length,data[0].length);
		return r.assign(data);
	}
	public BasicMatrix<V> convert(Number[][] data){
		BasicMatrix<V> r = convert(data.length,data[0].length);
		return r.assign(data);		
	}
	/*
	 * Create a length*1 vector matrix
	 */
	public BasicMatrix<V> convert(CohoNumber[] data){
		BasicMatrix<V> r = convert(data.length,1);
		return r.assign(data);
	}
	/*
	 * Create a length*1 vector matrix
	 */
	public BasicMatrix<V> convert(Number[] data){
		BasicMatrix<V> r = convert(data.length,1);
		return r.assign(data);		
	}
	//NOTE create matrix depends on the type. Which should be changed if new subclass implemented
	public static BasicMatrix create(CohoType  type, int nrows, int ncols){
		if(type == CohoBoolean.type)
			return new BooleanMatrix(nrows,ncols);
		if(type == CohoInteger.type)
			return new IntegerMatrix(nrows,ncols);
		if(type == CohoDouble.type)
			return new DoubleMatrix(nrows,ncols);
		if(type == CohoAPR.type)
			return new APRMatrix(nrows,ncols);
		if(type == DoubleInterval.type)
			return new DoubleIntervalMatrix(nrows,ncols);
		return new BasicMatrix(type,nrows,ncols);	
	}
	public static BasicMatrix create(CohoNumber x, int nrows, int ncols){
		return create(x.type(),nrows,ncols).fill(x);
	}
	public static BasicMatrix create(Number x, int nrows, int ncols){
		return create(ScaleType.promote(x),nrows,ncols);
	}
	/*****************************
	 * Functions related to the type 
	 *****************************/
	public V zero(){//assume each CohoNumber has a static filed zero.
		return (V)type.zero();
	}
	public V one(){
		return (V)type.one();
	}
	public V random(){
		return (V)zero().random();
	}
	/*
	 * convert number n to the type V. We can't call static methods, because the CohoNumber 
	 * interface doesn't define it.
	 */
	protected V elementConvert(CohoNumber x){
		return (V)zero().convert(x);
	}
	protected V elementConvert(Number x){
		return (V)zero().convert(x);
	}
	protected V[][] createArray(int nrows, int ncols){
		return (V[][])zero().createArray(nrows, ncols);
	}
	protected V[] createVector(int length){
		return (V[])zero().createVector(length);
	}


	/***************************
	 * Special Matrix
	 **************************/
	/*
	 * fill the matrix will x
	 * NOTE: the matrix is changed
	 * @see coho.common.matrix.Matrix#fill(coho.common.number.CohoNumber)
	 */
	public BasicMatrix<V> fill(CohoNumber x){
		//BasicMatrix<V> r = convert();
		for(int i=0; i<nrows; i++){
			for(int j=0; j<ncols; j++){
				//r.assign(x,i,j);
				assign(x,i,j);
			}
		}
		return this;
	}
	public BasicMatrix<V> fill(Number x){
		return fill(elementConvert(x));
	}	
	
	public BasicMatrix<V> ones(){
		return convert().fill(one());
	}
	public BasicMatrix<V> zeros(){
		return convert().fill(zero());
	}
	public BasicMatrix<V> randoms(){
		BasicMatrix<V> r = convert();
		for(int i=0;i<nrows;i++){
			for(int j=0;j<ncols;j++){
				r.assign(random(),i,j);
			}
		}
		return r;		
	}
	
	/*
	 * vector <-> square matrix
	 * @see coho.common.matrix.Matrix#diag()
	 */
	public BasicMatrix<V> diag(){
		BasicMatrix<V> r;
		if(isVector()){
			r = convert(length(),length()).zeros();
			for(int i=0;i<length();i++)
				r.assign(V(i),i,i);
			
		}else{
			if(!isSquare())
				throw new MatrixError("The matrix should be square");
			r = convert(nrows,1);
			for(int i=0; i<nrows; i++){
				r.assign(V(i,i),i);
			}
		}
		return r;
	}
	/*
	 * Create an ident matrix from vector. length()*length() 
	 * @see coho.common.matrix.Matrix#ident()
	 */
	public BasicMatrix<V> ident(){
		if(isVector()){
			return ones().diag();
		}else if(isSquare()){
			return diag().ones().diag();
		}else{
			throw new MatrixError("The function ident() is for vector only");
		}
	}
	public BasicMatrix<V> ident(int length){
		BasicMatrix<V> r = convert(length,1);
		return r.ones().diag();		
	}

	/*******************************
	 *Dimention related functions 
	 ******************************/
	//nrows when i=0; ncols when i=1
	public int size(int i){
		if(i==0)
			return nrows;
		else
			return ncols;
	}
	public int[] size(){
		return new int[]{nrows,ncols};
	}
	public int length(){
		if(isVector())
			return nrows==1?ncols:nrows;
		throw new MatrixError("This matrix is not a vector");
	}	
	public boolean isVector(){
		return (nrows==1 || ncols==1);
	}
	public boolean isSquare(){
		return (nrows == ncols);
	}
	/*
	 * Check if the two matrices has the same dimension
	 */
	protected void sameDims(Matrix that){
		if(isVector()){
			if(!that.isVector()||length()!=that.length())
				throw new MatrixError("Dimension error");
		}else if(nrows!=that.nrows()||ncols!=that.ncols())
			throw new MatrixError("Dimension error");
	}

	/********************************
	 * Assign and get Value
	 * Assign is the only function that changes the value of a matrix
	 * Assume dimension is right, throw ArrayOutOfIndexException by JVM. 
	 ********************************/
	public BasicMatrix<V> assign(CohoNumber v, int row, int col){
		data[row][col] = elementConvert(v);
		return this;
	}
	public BasicMatrix<V> assign(Number v, int row, int col){
		return assign(elementConvert(v),row,col);
		//data[row][col] = elementConvert(v);
		//return this;
	}
	/*
	 * For vectors
	 * @see coho.common.matrix.Matrix#assign(coho.common.number.CohoNumber, int)
	 */
	public BasicMatrix<V> assign(CohoNumber v, int n){
		if(nrows==1)
			return assign(v,0,n);
		else if(ncols==1)
			return assign(v,n,0);
		else
			throw new MatrixError("The function is only for vector");
	}
	public BasicMatrix<V> assign(Number v, int n){
		return assign(elementConvert(v),n);
	}
	public BasicMatrix<V> assign(Matrix m){
		sameDims(m);
		if(isVector()){//for vectors
			for(int i=0; i<length(); i++){
				assign(m.V(i),i);
			}
		}else{// for matrics	
			for(int i=0;i<nrows;i++){
				for(int j=0;j<ncols;j++){
					assign(m.V(i,j),i,j);
				}
			}
		}
		return this;
	}
	public BasicMatrix<V> assign(CohoNumber[][] v){
		if(v==null || v.length!=nrows || v[0].length!=ncols)
			throw new MatrixError("Dimension error");
		for(int i=0; i<nrows; i++){
			for(int j=0; j<ncols; j++){
				assign(v[i][j],i,j);
			}
		}
		return this;
	}
	public BasicMatrix<V> assign(Number[][] v){
		if(v==null || v.length!=nrows ||v[0].length!=ncols)
			throw new MatrixError("Dimension error");
		for(int i=0; i<nrows; i++){
			for(int j=0; j<ncols; j++){
				assign(v[i][j],i,j);
			}
		}
		return this;		
	}
	public BasicMatrix<V> assign(CohoNumber[] v){
		if(v==null || !isVector()||v.length!=length())
			throw new MatrixError("Dimension error, for vector only");
		for(int i=0; i<length(); i++){
			assign(v[i],i);
		}
		return this;
	}
	public BasicMatrix<V> assign(Number[] v){
		if(v==null || !isVector()||v.length!=length())
			throw new MatrixError("Dimension error, for vector only");
		for(int i=0; i<length(); i++){
			assign(v[i],i);
		}
		return this;
	}

	//assign m to the matrix, beginning with the v_row's row and v_col's column
	public BasicMatrix<V> assign(Matrix m, int v_row, int v_col){
		for(int i=0; i<m.nrows(); i++){
			for(int j=0; j<m.ncols(); j++){
				assign(m.V(i,j),v_row+i,v_col+j);
			}
		}
		return this;
	}
	public BasicMatrix<V> assign(Matrix m, int n){
		if(!isVector()||!m.isVector()){
			throw new MatrixError("The function is only for vector");
		}
		for(int i=0; i<m.length(); i++){
			assign(m.V(i),i+n);
		}
		return this;
	}
	public BasicMatrix<V> assign(Matrix m, BooleanMatrix mask){
		sameDims(m); sameDims(mask);
		if(isVector()){//m should be vector here
			for(int i=0; i<length(); i++){
				if(mask.V(i).booleanValue())
					assign(m.V(i),i);
			}
		}else{
			for(int i=0; i<nrows; i++){
				for(int j=0; j<ncols; j++){
					if(mask.V(i, j).booleanValue())
						assign(m.V(i,j),i,j);
				}
			}
		}
		return this;
	}
	/*
	 * For vectors
	 * @see coho.common.matrix.Matrix#assign(coho.common.matrix.Matrix, coho.common.matrix.IntegerMatrix)
	 */
	public BasicMatrix<V> assign(Matrix m, IntegerMatrix mask){
		if(isVector() && m.isVector()){
			mask.sameDims(m);
			for(int i=0; i<mask.length(); i++){
					assign(m.V(i),mask.V(i).intValue());
			}
			return this;
		}
		throw new MatrixError("The function is only for vector");
	}
	
	public V V(int row, int col){
		return data[row][col];
	}
	/*
	 * For vectors
	 * @see coho.common.matrix.Matrix#V(int)
	 */
	public V V(int n){
		if(nrows==1)
			return V(0,n);
		else if(ncols==1)
			return V(n,0);
		else
 			throw new MatrixError("The function is only for vector");
	}
	//XXX I have changed the definition of range which includes both lo and hi
	public BasicMatrix<V> V(Range row, Range col){
		BasicMatrix<V> r = convert(row.length(),col.length());
		for(int i=0; i<row.length(); i++){
			for(int j=0; j<col.length(); j++){
				r.assign(V(row.lo()+i,col.lo()+j),i,j);
			}
		}
		return r;
	}
	/*
	 * For vectors
	 * @see coho.common.matrix.Matrix#V(coho.common.matrix.Range)
	 */
	public BasicMatrix<V> V(Range n){
		BasicMatrix<V> r = convert(Math.min(nrows,n.length()), Math.min(ncols, n.length()));//FIXED see binaryMap
		for(int i=0; i<n.length(); i++){
			r.assign(V(n.lo()+i),i);
		}
		return r;
	}
	public BasicMatrix<V> V(Range row, int col){
		return V(row, new Range(col,col));
	}
	public BasicMatrix<V> V(int row, Range col){
		return V(new Range(row,row),col);
	}
	public BasicMatrix<V> V(IntegerMatrix pos){
		if(!isVector()||!pos.isVector())
			throw new MatrixError("The function is only for vector");
		BasicMatrix<V> r = convert(Math.min(nrows,pos.length()),Math.min(ncols,pos.length()));
		for(int i=0; i<pos.length();i++){
			r.assign(V(pos.V(i).intValue()),i);
		}
		return r;
	}
	public BasicMatrix<V> row(int row){
		return V(row, new Range(0,ncols-1));
//		BasicMatrix<V> r = convert(1,ncols);
//		for(int j=0; j<ncols; j++){
//			r.assign(V(row,j),j);
//		}
//		return r;
	}
	public BasicMatrix<V> col(int col){
		return V(new Range(0,nrows-1),col);
//		BasicMatrix<V> r = convert(nrows,1);
//		for(int i=0; i<nrows; i++){
//			r.assign(V(i,col),i);
//		}
//		return r;
	}
	public BasicMatrix<V> row(Range row){
		return V(row, new Range(0,ncols-1));
//		BasicMatrix<V> r = convert(row.length(),ncols);
//		for(int i=0; i<row.length(); i++){
//			r.assign(row(row.lo()+i),i,0);
//		}
//		return r;
	}
	public BasicMatrix<V> col(Range col){
		return V(new Range(0,nrows-1),col);
//		BasicMatrix<V> r = convert(nrows,col.length());
//		for(int j=0; j<col.length(); j++){
//			r.assign(col(col.lo()+j),0,j);
//		}
//		return r;
	}
	/*
	 * row must be vector
	 * @see coho.common.matrix.Matrix#row(coho.common.matrix.IntegerMatrix)
	 */
	public BasicMatrix<V> row(IntegerMatrix row){
		if(!row.isVector())
			throw new MatrixError("The input matrix must be a vector");
		BasicMatrix<V> r = convert(row.length(),ncols);
		for(int i=0; i<row.length(); i++){
			r.assign(row(row.V(i).intValue()),i,0);
		}
		return r;
	}
	public BasicMatrix<V> row(BooleanMatrix mask){
		return row(mask.find());
	}
	/*
	 * col must be vector
	 * @see coho.common.matrix.Matrix#col(coho.common.matrix.IntegerMatrix)
	 */
	public BasicMatrix<V> col(IntegerMatrix col){
		if(!col.isVector())
			throw new MatrixError("The input matrix must be a vector");
		BasicMatrix<V> r = convert(nrows,col.length());
		for(int j=0; j<col.length(); j++){
			r.assign(col(col.V(j).intValue()),0,j);
		}
		return r;
	}
	public BasicMatrix<V> col(BooleanMatrix mask){		
		return col(mask.find());
	}

	/*************************
	 * Binary Operations using Map
	 *************************/
	public BasicMatrix binaryMap(Matrix that, BinaryMapOp op){
		sameDims(that);
		BasicMatrix m;
		if(isVector()){
			//FIXED: the operation of two row vectors produces a column vectors.
			m = new BasicMatrix(op.t(this, that),nrows,ncols);//XXX don't need <V>?
			for(int i=0; i<length(); i++){
				m.assign(op.v(V(i),that.V(i)),i);
			}
		}else{
			m = new BasicMatrix(op.t(this,that),nrows,ncols);
			for(int i=0; i<nrows; i++){
				for(int j=0; j<ncols; j++){
					m.assign(op.v(V(i,j),that.V(i,j)), i, j);
				}
			}
		}
		return m;
	}
	protected static abstract class BinaryMapOp {
		public CohoType t(Matrix arg1, Matrix arg2){
			CohoType t1 = arg1.elementType();
			CohoType t2 = arg2.elementType();
			return t1.promote(t2);			
		}
		public abstract CohoNumber v(CohoNumber v1, CohoNumber v2);
	}
	protected static final BinaryMapOp addOp = new BinaryMapOp(){
		public CohoNumber v(CohoNumber v1, CohoNumber v2){
			return v1.add(v2);
		}
	};
	protected static final BinaryMapOp subOp = new BinaryMapOp(){
		public CohoNumber v(CohoNumber v1, CohoNumber v2){
			return v1.sub(v2);
		}		
	};
	protected static final BinaryMapOp elMultOp = new BinaryMapOp(){
		public CohoNumber v(CohoNumber v1, CohoNumber v2){
			return v1.mult(v2);
		}		
	};
	protected static final BinaryMapOp elDivOp = new BinaryMapOp(){
		public CohoNumber v(CohoNumber v1, CohoNumber v2){
			return v1.div(v2);
		}		
	};
	protected static final BinaryMapOp eqOp = new BinaryMapOp(){
		public ScaleType t(Matrix arg1, Matrix arg2){
			return CohoBoolean.type;
		}
		public CohoBoolean v(CohoNumber v1, CohoNumber v2){
			return CohoBoolean.create(v1.eq(v2));
		}		
	};
	protected static final BinaryMapOp neqOp = new BinaryMapOp(){
		public ScaleType t(Matrix arg1, Matrix arg2){
			return CohoBoolean.type;
		}
		public CohoBoolean v(CohoNumber v1, CohoNumber v2){
			return CohoBoolean.create(v1.neq(v2));
		}		
	};
	protected static final BinaryMapOp lessOp = new BinaryMapOp(){
		public ScaleType t(Matrix arg1, Matrix arg2){
			return CohoBoolean.type;
		}
		public CohoBoolean v(CohoNumber v1, CohoNumber v2){
			return CohoBoolean.create(v1.less(v2));
		}		
	};
	protected static final BinaryMapOp leqOp = new BinaryMapOp(){
		public ScaleType t(Matrix arg1, Matrix arg2){
			return CohoBoolean.type;
		}
		public CohoBoolean v(CohoNumber v1, CohoNumber v2){
			return CohoBoolean.create(v1.leq(v2));
		}		
	};
	protected static final BinaryMapOp greaterOp = new BinaryMapOp(){
		public ScaleType t(Matrix arg1, Matrix arg2){
			return CohoBoolean.type;
		}
		public CohoBoolean v(CohoNumber v1, CohoNumber v2){
			return CohoBoolean.create(v1.greater(v2));
		}		
	};
	protected static final BinaryMapOp geqOp = new BinaryMapOp(){
		public ScaleType t(Matrix arg1, Matrix arg2){
			return CohoBoolean.type;
		}
		public CohoBoolean v(CohoNumber v1, CohoNumber v2){
			return CohoBoolean.create(v1.geq(v2));
		}		
	};

	public BasicMatrix add(Matrix that){
		return binaryMap(that,addOp);
	}
	public BasicMatrix sub(Matrix that){
		return binaryMap(that,subOp);
	}
	public BasicMatrix elMult(Matrix that){
		return binaryMap(that,elMultOp);
	}
	public BasicMatrix elDiv(Matrix that){
		return binaryMap(that,elDivOp);
	}
	public BooleanMatrix eq(Matrix that){
		return BooleanMatrix.typeCast(binaryMap(that,eqOp));
	}
	public BooleanMatrix neq(Matrix that){
		return BooleanMatrix.typeCast(binaryMap(that,neqOp));
	}
	public BooleanMatrix less(Matrix that){
		return BooleanMatrix.typeCast(binaryMap(that,lessOp));
	}
	public BooleanMatrix leq(Matrix that){
		return BooleanMatrix.typeCast(binaryMap(that,leqOp));
	}
	public BooleanMatrix greater(Matrix that){
		return BooleanMatrix.typeCast(binaryMap(that, greaterOp));
	}
	public BooleanMatrix geq(Matrix that){
		return BooleanMatrix.typeCast(binaryMap(that,geqOp));
	}
	// We don't want to use map here because the function is easy, and always return 
	// BasicMatrix<V> rather than BasicMatrix
	public BasicMatrix<V> abs(){
		BasicMatrix<V> r = convert();
		for(int i=0; i<nrows; i++){
			for(int j=0; j<ncols; j++){
				r.assign(V(i,j).abs(), i, j);
			}
		}
		return r;
	}
	public BasicMatrix<V> negate(){
		BasicMatrix<V> r = convert();
		for(int i=0; i<nrows; i++){
			for(int j=0; j<ncols; j++){
				r.assign(V(i,j).negate(), i, j);
			}
		}
		return r;		
	}
	public BasicMatrix<V> transpose(){
		BasicMatrix<V> r = convert(ncols, nrows);
		for(int i=0; i<nrows; i++){
			for(int j=0; j<ncols; j++){
				r.assign(V(i,j), j, i);
			}
		}
		return r;		
	}

	
	/*********************
	 * Operations using reduce
	 *********************/
	public CohoNumber binaryReduce(Matrix that, ReduceOp op){
		sameDims(that);
		if(isVector()){
			return reduceVector(new Matrix[]{that},op);
		}else{
			return reduce(new Matrix[]{that},op);
		}
	}
	public V unaryReduce(ReduceOp op){
		return (V)reduce(new Matrix[]{},op);
	}
	private CohoNumber reduceVector(Matrix[] that, ReduceOp op){
		CohoNumber u = op.first();
		for(int i=0; i<length(); i++){
			CohoNumber[] x = new CohoNumber[that.length+1];
			x[0] = V(i);
			for(int k=0; k<that.length; k++){
				x[k+1] = that[k].V(i);
			}
			u = op.middle(u, x);				
		}
		return u;
	}
	private CohoNumber reduce(Matrix[] that, ReduceOp op){
		CohoNumber u = op.first();
		for(int i=0;i<nrows;i++){
			for(int j=0;j<ncols;j++){
				CohoNumber[] x = new CohoNumber[that.length+1];
				x[0] = V(i,j);
				for(int k=0; k<that.length; k++){
					x[k+1] = that[k].V(i, j);
				}
				u = op.middle(u, x);
			}
		}
		return u;
	}	
	protected static abstract class  ReduceOp{
		public CohoNumber first(){
			return null;
		}
		public abstract CohoNumber middle(CohoNumber partialNumber, CohoNumber[] x);
	}
	protected static final ReduceOp dotProdOp = new ReduceOp(){
		public CohoNumber middle(CohoNumber partialNumber, CohoNumber[] x){
			if(partialNumber==null){
				return x[0].mult(x[1]);
			}else
				return partialNumber.add(x[0].mult(x[1]));
		}
	};
	protected static final ReduceOp maxOp = new ReduceOp(){
		public CohoNumber middle(CohoNumber partialNumber, CohoNumber[] x){
			if(partialNumber==null)
				return x[0];
			else
				return partialNumber.max(x[0]);
		}		
	};
	protected static final ReduceOp minOp = new ReduceOp(){
		public CohoNumber middle(CohoNumber partialNumber, CohoNumber[] x){
			if(partialNumber==null)
				return x[0];
			else
				return partialNumber.min(x[0]);
		}		
	};
	protected static final ReduceOp sumOp = new ReduceOp(){
		public CohoNumber middle(CohoNumber partialNumber, CohoNumber[] x){
			if(partialNumber == null)
				return x[0];
			else
				return partialNumber.add(x[0]);
		}
	};
	protected static final ReduceOp prodOp = new ReduceOp(){
		public CohoNumber middle(CohoNumber partialNumber, CohoNumber[] x){
			if(partialNumber == null)
				return x[0];
			else
				return partialNumber.mult(x[0]);
		}		
	};

	public CohoNumber dotProd(Matrix that){
		return binaryReduce(that,dotProdOp);
	}
	public V max(){
		return unaryReduce(maxOp);
	}
	public V min(){
		return unaryReduce(minOp);
	}
	/*
	 * Defined as sqrt of A_ij^2. 
	 * Normally, it's for vector
	 */
	public V norm(){
		return (V)dotProd(this).abs().sqrt();//CONSIDER: use abs for interval?
	}
	public V sum(){
		return unaryReduce(sumOp);
	}
	public V prod(){
		return unaryReduce(prodOp);
	}
	/*********************************
	 *Other operations 
	 *********************************/
	public BasicMatrix mult(Matrix that){
		if(ncols!=that.nrows())
			throw new MatrixError("Dimension error for multiply of matrix");
		BasicMatrix r = new BasicMatrix(elementType().promote(that.elementType()),nrows,that.ncols());
		for(int i=0; i<r.nrows; i++){
			for(int j=0; j<r.ncols; j++){
				r.assign(row(i).dotProd(that.col(j)),i,j);
			}
		}
		return r;
	}
	//BUG: what the addition of a intMatrix and a double value should be?
	//NO promotion here!
	//operations for CohoNumber and Number
	public BasicMatrix add(CohoNumber x){
		return add(create(x,nrows,ncols));
	}
	public BasicMatrix add(Number x){
		return add(ScaleType.promote(x));
	}
	public BasicMatrix sub(CohoNumber x){
		return add(create(x,nrows,ncols));
	}
	public BasicMatrix sub(Number x){
		return sub(ScaleType.promote(x));
	}
	public BasicMatrix mult(CohoNumber x){
		return elMult(create(x,nrows,ncols));
	}
	public BasicMatrix mult(Number x){
		return mult(ScaleType.promote(x));
	}
	public BasicMatrix div(CohoNumber x){
		return elDiv(create(x,nrows,ncols));
	}
	public BasicMatrix div(Number x){
		return div(ScaleType.promote(x));
	}
	
	//operations for same type
	public BasicMatrix<V> add(BasicMatrix<V> that){
		return (BasicMatrix<V>)add((Matrix)that);
	}
	public BasicMatrix<V> sub(BasicMatrix<V> that){
		return (BasicMatrix<V>)sub((Matrix)that);
	}
	public BasicMatrix<V> mult(BasicMatrix<V> that){
		return (BasicMatrix<V>)mult((Matrix)that);
	}
	//not supported yet
	public BasicMatrix<V> div(BasicMatrix<V> that) throws SingularMatrixException{
		return (BasicMatrix<V>)div((Matrix)that);
	}
	public BasicMatrix<V> elMult(BasicMatrix<V> that){
		return (BasicMatrix<V>)elMult((Matrix)that);
	}
	public BasicMatrix<V> elDiv(BasicMatrix<V> that){
		return (BasicMatrix<V>)elDiv((Matrix)that);
	}
	public BasicMatrix<V> leftDiv(BasicMatrix<V> that)throws SingularMatrixException{
		return (BasicMatrix<V>)leftDiv((Matrix)that);
	}
	public V dotProd(BasicMatrix<V> that){
		return (V)dotProd((Matrix)that);
	}
	/***********************
	 * Not supported operation 
	 ***********************/
	public BasicMatrix div(Matrix m)throws SingularMatrixException{
		throw new UnsupportedOperationException();
	}	
	public BasicMatrix leftDiv(Matrix m) throws SingularMatrixException{
		throw new UnsupportedOperationException();
	}
	public BasicMatrix<V> inv()throws SingularMatrixException{
		throw new UnsupportedOperationException();
	}
	
	/***************************
	 * Array and Vector 
	 ***************************/
	public V[][] toArray(){
		V[][] r = createArray(nrows,ncols);
		for(int i=0; i<nrows; i++){
			for(int j=0; j<ncols; j++){
				r[i][j] = V(i,j);
			}				
		}
		return r;
	}
	public V[] toVector(){
		if(!isVector())
			throw new MatrixError("The function is for vector only");
		V[] r = createVector(length());
		for(int i=0; i<length(); i++)
			r[i] = V(i);
		return r;
	}

    public final String toString() {
        return ("matrix(" + stringify(null) + ")");
    }
    public final String toString(Object fmt) {
        return ("matrix(" + stringify(fmt) + ")");
    }
    static final String matOpener = "[\n";
    static final String matCloser = "]";
    static final String rowOpener = " [ ";
    static final String rowCloser = " ];\n";
    static final String colOpener = "";
    static final String colCloser = ", ";
    static final boolean bCloseLastCol = false;
    static final int DDWidth = 23;
    
    /**
     * Format this matrix to a string
     */
    public static final int nStandardLength = 16;
    public String stringify(Object fmt) {
    	StringBuffer buf = new StringBuffer();
    	buf.append(matOpener);
    	for (int i = 0; i < nrows(); i++) {
    		buf.append(rowOpener);
    		for (int j = 0; j < ncols(); j++) {
    			buf.append(colOpener);
    			CohoNumber e = V(i, j);
    			String s = e.toString();
    			if (e instanceof CohoDouble) {
    				double d = e.doubleValue();
    				if ((fmt != null) && (fmt instanceof String) && (((String) (fmt)).compareTo("hex") == 0)) {
    					s = "$"+ MoreLong.toHexString(Double.doubleToLongBits(d));
//    					s = "$";
//    					String l = Long.toHexString(Double.doubleToLongBits(d));
//    					for(int k=0; k<nStandardLength-l.length();k++)
//    						s+='0';
//    					s+=l;
    				}else{
    					// force all doubles printed as decimal to be same width
    					// by prepending blanks; this makes reading easier;
    					int nBlanks = DDWidth - s.length();
                        s = MoreString.multiply(" ", nBlanks) + s;
//    					for(int k=0; k<nBlanks; k++)
//    						s = " "+s;
    				}
    			}
    			buf.append(s);
    			if (j < ncols() - 1 || bCloseLastCol)
    				buf.append(colCloser);
    		}
    		buf.append(rowCloser);
    	}
    	buf.append(matCloser);
    	return buf.toString();
    }

    public String toMatlab(){
		String matlab = "A=[\n";
		for (int row=0; row<nrows();row++){		
			for(int col=0; col<ncols(); col++){				
				matlab += V(row,col).toString();
				if(col!=ncols-1)
					matlab +=",";
			}
			if(row!=nrows-1)
				matlab +=";\n"; 
		}
		matlab += "\n];";
		return matlab;
    }
    public static void main(String[] argv){
		BasicMatrix<CohoInteger> a = new BasicMatrix<CohoInteger>(CohoInteger.type, 1, 1);
//		System.out.println(a = a.fill(5));
//		CohoInteger v = a.V(1,2);
//		System.out.println(v);
//		a.assign(10,1,2);
//		v = a.V(1,2);
//		System.out.println(v);
//		System.out.println(a.add(a));
//		System.out.println(a.mult(a));
//		System.out.println(a.min());
//		System.out.println(a.row(3));
//		System.out.println(a.col(1));
//		System.out.println(a.col(new Range(0,3)));
//		BasicMatrix<CohoDouble> b = new BasicMatrix<CohoDouble>(CohoDouble.type, 5, 5);
//		b = b.fill(2);
//		System.out.println(a.add(b));
//		System.out.println(a.mult(b));
//		//System.out.println(b.add(1));
//		BasicMatrix<CohoBoolean> c = new BasicMatrix<CohoBoolean>(CohoBoolean.zero,5,5);
		CohoDouble[] array = new CohoDouble[]{CohoDouble.one};
		BasicMatrix<CohoDouble> b = new BasicMatrix<CohoDouble>(array);
//		System.out.println(b);
//		CohoNumber[] cc = (CohoNumber[])(array);
//		array = (CohoDouble[])cc;
//		System.out.println(array);
		a.assign(1,0);
		System.out.println(b.add(a));
		CohoNumber[][] array3 = b.toArray();
		System.out.println(array3[0][0].type());
		CohoDouble[][] array2 = (CohoDouble[][])array3;
		for(int i=0; i<array2.length; i++){
			for(int j=0; j<array2[0].length; j++){
				System.out.println(array2[i][j]);
			}
		}	
	}
}
