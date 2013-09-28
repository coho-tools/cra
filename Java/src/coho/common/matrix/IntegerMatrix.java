package coho.common.matrix;
import coho.common.number.*;
/*
 * wrapper for BasicMatrix<CohoDouble> 1)type cast 2)static method.
 * The functions for BasicMatrix<CohoDouble> and DoubleMatrix are the same 
 */
public class IntegerMatrix extends BasicMatrix<CohoInteger> {
	//for type cast
	protected IntegerMatrix(CohoInteger[][] data, int nrows, int ncols){		
		super(CohoInteger.type,nrows,ncols,data);
	}
	public static IntegerMatrix typeCast(BasicMatrix<CohoInteger> m){
		return new IntegerMatrix(m.data,m.nrows,m.ncols);
	}
	
	//new functions
	public IntegerMatrix(int nrows, int ncols){
		super(CohoInteger.zero,nrows,ncols);
	}
	public IntegerMatrix(int[] size){
		this(size[0],size[1]);
	}
	public IntegerMatrix(Matrix m){
		this(m.nrows(),m.ncols());
		super.assign(m);
	}
	public IntegerMatrix(CohoNumber[][] data){
		this(data.length,data[0].length);
		super.assign(data);
	}
	public IntegerMatrix(CohoNumber[] data){
		this(data.length,1);
		super.assign(data);
	}
	public IntegerMatrix(Number[][] data){
		this(data.length,data[0].length);
		super.assign(data);
	}
	public IntegerMatrix(int[][] data){
		this(data.length,data[0].length);
		for(int i=0; i<nrows; i++){
			for(int j=0; j<ncols; j++){
				super.assign(data[i][j],i,j);
			}
		}
	}
	public IntegerMatrix(Number[] data){
		this(data.length,1);
		super.assign(data);
	}
//	public IntegerMatrix(int[] data){
//		this(data.length,1);
//		for(int i=0; i<length(); i++){
//			super.assign(data[i],i);
//		}
//	}
	
	public static IntegerMatrix create(int nrows, int ncols){
		return new IntegerMatrix(nrows,ncols);
	}
	public static IntegerMatrix create(int[] size){
		return new IntegerMatrix(size);
	}
	public static IntegerMatrix create(Matrix m){
		if( m instanceof IntegerMatrix)
			return (IntegerMatrix)m;
		return new IntegerMatrix(m);
	}
	public static IntegerMatrix create(CohoNumber[][] data){
		return new IntegerMatrix(data);
	}
	public static IntegerMatrix create(CohoNumber[] data){
		return new IntegerMatrix(data);
	}
	public static IntegerMatrix create(Number[][] data){
		return new IntegerMatrix(data);
	}
	public static IntegerMatrix create(Number[] data){
		return new IntegerMatrix(data);
	}
	public static IntegerMatrix create(int[][] data){
		return new IntegerMatrix(data);
	}
//	public static IntegerMatrix create(int[] data){
//		return new IntegerMatrix(data);
//	}
	
	public IntegerMatrix convert(int[][] data){
		return new IntegerMatrix(data);
	}
//	public IntegerMatrix convert(int[] data){
//		return new IntegerMatrix(data);
//	}
	

	
	@Override
	public IntegerMatrix convert(){
		return new IntegerMatrix(nrows,ncols);
		//return IntegerMatrix.typeCast(super.convert());
	}
	@Override
	public IntegerMatrix convert(int nrows, int ncols){
		return new IntegerMatrix(nrows,ncols);
		//return IntegerMatrix.typeCast(super.convert(nrows, ncols));
	}
	@Override
	public IntegerMatrix convert(int[] size){
		return new IntegerMatrix(size);
		//return IntegerMatrix.typeCast(super.convert(size));
	}
	@Override
	public IntegerMatrix convert(Matrix m){
		if(m instanceof IntegerMatrix)
			return (IntegerMatrix)m;
		return new IntegerMatrix(m);
		//return IntegerMatrix.typeCast(super.convert(m));
	}
	@Override
	public IntegerMatrix convert(CohoNumber[][] data){
		return new IntegerMatrix(data);
		//return IntegerMatrix.typeCast(super.convert(data));
	}
	@Override
	public IntegerMatrix convert(CohoNumber[] data){
		return new IntegerMatrix(data);
		//return IntegerMatrix.typeCast(super.convert(data));
	}
	@Override
	public IntegerMatrix convert(Number[][] data){
		return new IntegerMatrix(data);
		//return IntegerMatrix.typeCast(super.convert(data));
	}
	@Override
	public IntegerMatrix convert(Number[] data){
		return new IntegerMatrix(data);
		//return IntegerMatrix.typeCast(super.convert(data));
	}
	
	@Override
	public ScaleType elementType(){
		return CohoInteger.type;
	}
	
	@Override
	public IntegerMatrix assign(CohoNumber v, int row, int col){
		super.assign(v,row,col);
		return this;
	}
	@Override
	public IntegerMatrix assign(Number v, int row, int col){
		super.assign(v,row,col);
		return this;
	}
	@Override
	public IntegerMatrix assign(CohoNumber v, int n){
		super.assign(v,n);//for vector
		return this;
	}
	@Override
	public IntegerMatrix assign(Number v, int n){
		super.assign(v,n);//for vector
		return this;
	}
	@Override
	public IntegerMatrix assign(Matrix m){
		super.assign(m);
		return this;
	}
	@Override
	public IntegerMatrix assign(CohoNumber[][] v){
		super.assign(v);
		return this;
	}
	@Override
	public IntegerMatrix assign(Number[][] v){
		super.assign(v);
		return this;
	}
	@Override
	public IntegerMatrix assign(CohoNumber[] v){
		super.assign(v);
		return this;
	}
	@Override
	public IntegerMatrix assign(Number[] v){
		super.assign(v);
		return this;
	}
	@Override
	public IntegerMatrix assign(Matrix m, int v_row, int v_col){
		super.assign(m,v_row,v_col);
		return this;
	}
	@Override
	public IntegerMatrix assign(Matrix m, int n){
		super.assign(m,n);
		return this;
	}
	@Override
	public IntegerMatrix assign(Matrix m, BooleanMatrix mask){
		super.assign(m,mask);
		return this;
	}
	@Override
	public IntegerMatrix assign(Matrix m, IntegerMatrix pos){
		super.assign(m,pos);//for vector
		return this;
	}
	@Override
	public IntegerMatrix V(Range row, Range col){
		return IntegerMatrix.typeCast(super.V(row,col));
	}
	@Override
	public IntegerMatrix V(Range n){
		return IntegerMatrix.typeCast(super.V(n));//for vector
	}
	@Override
	public IntegerMatrix V(Range row, int col){
		return IntegerMatrix.typeCast(super.V(row,col));
	}
	@Override
	public IntegerMatrix V(int row, Range col){
		return IntegerMatrix.typeCast(super.V(row,col));
	}
	@Override
	public IntegerMatrix V(IntegerMatrix pos){
		return IntegerMatrix.typeCast(super.V(pos));//for vector
	}
	@Override
	public IntegerMatrix row(int row){
		return IntegerMatrix.typeCast(super.row(row));
	}
	@Override
	public IntegerMatrix row(Range row){
		return IntegerMatrix.typeCast(super.row(row));
	}
	@Override
	public IntegerMatrix row(IntegerMatrix pos){
		return IntegerMatrix.typeCast(super.row(pos));
	}
	@Override
	public IntegerMatrix row(BooleanMatrix pos){
		return IntegerMatrix.typeCast(super.row(pos));
	}
	@Override
	public IntegerMatrix col(int col){
		return IntegerMatrix.typeCast(super.col(col));
	}
	@Override
	public IntegerMatrix col(Range col){
		return IntegerMatrix.typeCast(super.col(col));
	}
	@Override
	public IntegerMatrix col(IntegerMatrix pos){
		return IntegerMatrix.typeCast(super.col(pos));
	}
	@Override
	public IntegerMatrix col(BooleanMatrix pos){
		return IntegerMatrix.typeCast(super.col(pos));
	}

	@Override
	public IntegerMatrix ones(){
		return IntegerMatrix.typeCast(super.ones());
	}
	@Override
	public IntegerMatrix zeros(){
		return IntegerMatrix.typeCast(super.zeros());
	}
	@Override
	public IntegerMatrix randoms(){
		return IntegerMatrix.typeCast(super.randoms());
	}
	@Override
	public IntegerMatrix ident(){
		return IntegerMatrix.typeCast(super.ident());
	}
	@Override
	public IntegerMatrix ident(int n){
		return IntegerMatrix.typeCast(super.ident(n));
	}
	@Override
	public IntegerMatrix diag(){
		return IntegerMatrix.typeCast(super.diag());
	}
	@Override
	public IntegerMatrix fill(CohoNumber v){
		return IntegerMatrix.typeCast(super.fill(v));
	}
	@Override
	public IntegerMatrix fill(Number v){
		return IntegerMatrix.typeCast(super.fill(v));
	}

	@Override
	public IntegerMatrix abs(){
		return IntegerMatrix.typeCast(super.abs());
	}
	@Override
	public IntegerMatrix negate(){
		return IntegerMatrix.typeCast(super.negate());
	}
	@Override
	public IntegerMatrix transpose(){
		return IntegerMatrix.typeCast(super.transpose());
	}
	@Override
	public IntegerMatrix inv()throws SingularMatrixException{
		return IntegerMatrix.typeCast(super.inv());
	}
	
	public IntegerMatrix add(IntegerMatrix that){
		return IntegerMatrix.typeCast(super.add(that));
	}
	public IntegerMatrix sub(IntegerMatrix that){
		return IntegerMatrix.typeCast(super.sub(that));
	}
	public IntegerMatrix mult(IntegerMatrix that){
		return IntegerMatrix.typeCast(super.mult(that));
	}
	public IntegerMatrix div(IntegerMatrix that)throws SingularMatrixException{
		return IntegerMatrix.typeCast(super.div(that));
	}
	public IntegerMatrix elMult(IntegerMatrix that){
		return IntegerMatrix.typeCast(super.elMult(that));
	}
	public IntegerMatrix elDiv(IntegerMatrix that){
		return IntegerMatrix.typeCast(super.elDiv(that));
	}
	public IntegerMatrix leftDiv(IntegerMatrix that)throws SingularMatrixException{
		return IntegerMatrix.typeCast(super.leftDiv(that));
	}
	
	public static void main(String[] args){
		int[][] d ={{1, 2}, {2,3}};
		IntegerMatrix a = new IntegerMatrix(d);
//		System.out.println(a);
//		System.out.println(a.add(1));
		a = a.add(a);
		//a = a.add(new BasicMatrix<CohoInteger>(a.size()));
		System.out.println(a.add(a));
	}
}
