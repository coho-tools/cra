package coho.common.matrix;
import coho.common.number.*;
/*
 * wrapper for BasicMatrix<CohoDouble> 1)type cast 2)static method.
 * The functions for BasicMatrix<CohoDouble> and DoubleMatrix are the same 
 */
public class DoubleIntervalMatrix extends BasicMatrix<DoubleInterval> {
	//for type cast
	protected DoubleIntervalMatrix(DoubleInterval[][] data, int nrows, int ncols){		
		super(DoubleInterval.type,nrows,ncols,data);
	}
	public static DoubleIntervalMatrix typeCast(BasicMatrix<DoubleInterval> m){
		return new DoubleIntervalMatrix(m.data,m.nrows,m.ncols);
	}
	
	//new functions
	public DoubleIntervalMatrix(int nrows, int ncols){
		super(DoubleInterval.zero,nrows,ncols);
	}
	public DoubleIntervalMatrix(int[] size){
		this(size[0],size[1]);
	}
	public DoubleIntervalMatrix(Matrix m){
		this(m.nrows(),m.ncols());
		super.assign(m);
	}
	public DoubleIntervalMatrix(CohoNumber[][] data){
		this(data.length,data[0].length);
		super.assign(data);
	}
	public DoubleIntervalMatrix(CohoNumber[] data){
		this(data.length,1);
		super.assign(data);
	}
	public DoubleIntervalMatrix(Number[][] data){
		this(data.length,data[0].length);
		super.assign(data);
	}
	public DoubleIntervalMatrix(double[][] data){
		this(data.length,data[0].length);
		for(int i=0; i<nrows; i++){
			for(int j=0; j<ncols; j++){
				super.assign(data[i][j],i,j);
			}
		}
	}
	public DoubleIntervalMatrix(Number[] data){
		this(data.length,1);
		super.assign(data);
	}
	public DoubleIntervalMatrix(double[] data){
		this(data.length,1);
		for(int i=0; i<length(); i++){
			super.assign(data[i],i);
		}
	}
	
	public static DoubleIntervalMatrix create(int nrows, int ncols){
		return new DoubleIntervalMatrix(nrows,ncols);
	}
	public static DoubleIntervalMatrix create(int[] size){
		return new DoubleIntervalMatrix(size);
	}
	public static DoubleIntervalMatrix create(Matrix m){
		if( m instanceof DoubleIntervalMatrix)
			return (DoubleIntervalMatrix)m;
		return new DoubleIntervalMatrix(m);
	}
	public static DoubleIntervalMatrix create(CohoNumber[][] data){
		return new DoubleIntervalMatrix(data);
	}
	public static DoubleIntervalMatrix create(CohoNumber[] data){
		return new DoubleIntervalMatrix(data);
	}
	public static DoubleIntervalMatrix create(Number[][] data){
		return new DoubleIntervalMatrix(data);
	}
	public static DoubleIntervalMatrix create(Number[] data){
		return new DoubleIntervalMatrix(data);
	}
	public static DoubleIntervalMatrix create(double[][] data){
		return new DoubleIntervalMatrix(data);
	}
	public static DoubleIntervalMatrix create(double[] data){
		return new DoubleIntervalMatrix(data);
	}
	
	public DoubleIntervalMatrix convert(double[][] data){
		return new DoubleIntervalMatrix(data);
	}
	public DoubleIntervalMatrix convert(double[] data){
		return new DoubleIntervalMatrix(data);
	}


    /**
     * Get the low value of all elements of this matrix
     * @return DoubleMatrix.
     */
    public DoubleMatrix lo(){
        CohoDouble[][] lo = new CohoDouble[nrows()][ncols()];
        for(int i =0;i<nrows();i++){
            for(int j=0;j<ncols();j++){
                lo[i][j]=V(i,j).lo();
            }
        }
        return DoubleMatrix.create(lo);
    }
    /**
     * Get the high value of all elements of this matrix
     * @return DoubleMatrix.
     */
    public DoubleMatrix hi(){
        CohoDouble[][] hi = new CohoDouble[nrows()][ncols()];
        for(int i=0;i<nrows();i++){
            for(int j=0;j<ncols();j++){//FIXED: nrows->ncols
                hi[i][j]=V(i,j).hi();
            }
        }
        return DoubleMatrix.create(hi);
    }
    /**
     * Get the middle value: x() of all elements of this matrix
     * @return DoubleMatrix.
     */
    public DoubleMatrix x(){
        CohoDouble[][] x = new CohoDouble[nrows()][ncols()];
        for(int i=0;i<nrows();i++){
            for(int j=0;j<ncols();j++){
                x[i][j]=V(i,j).x();
            }
        }
        return DoubleMatrix.create(x);
    }
    /**
     * Get the error range: E() of all elements of this matrix
     * @return DoubleMatrix.
     */
    public DoubleMatrix e(){
        CohoDouble[][] E = new CohoDouble[nrows()][ncols()];
        for(int i=0;i<nrows();i++){
            for(int j=0;j<ncols();j++){
                E[i][j]=V(i,j).e();
            }
        }
        return DoubleMatrix.create(E);
    }
    

	@Override
	public DoubleIntervalMatrix convert(){
		return new DoubleIntervalMatrix(nrows,ncols);
		//return DoubleIntervalMatrix.typeCast(super.convert());
	}
	@Override
	public DoubleIntervalMatrix convert(int nrows, int ncols){
		return new DoubleIntervalMatrix(nrows,ncols);
		//return DoubleIntervalMatrix.typeCast(super.convert(nrows, ncols));
	}
	@Override
	public DoubleIntervalMatrix convert(int[] size){
		return new DoubleIntervalMatrix(size);
		//return DoubleIntervalMatrix.typeCast(super.convert(size));
	}
	@Override
	public DoubleIntervalMatrix convert(Matrix m){
		if( m instanceof DoubleIntervalMatrix)
			return (DoubleIntervalMatrix)m;
		return new DoubleIntervalMatrix(m);
		//return DoubleIntervalMatrix.typeCast(super.convert(m));
	}
	@Override
	public DoubleIntervalMatrix convert(CohoNumber[][] data){
		return new DoubleIntervalMatrix(data);
		//return DoubleIntervalMatrix.typeCast(super.convert(data));
	}
	@Override
	public DoubleIntervalMatrix convert(CohoNumber[] data){
		return new DoubleIntervalMatrix(data);
		//return DoubleIntervalMatrix.typeCast(super.convert(data));
	}
	@Override
	public DoubleIntervalMatrix convert(Number[][] data){
		return new DoubleIntervalMatrix(data);
		//return DoubleIntervalMatrix.typeCast(super.convert(data));
	}
	@Override
	public DoubleIntervalMatrix convert(Number[] data){
		return new DoubleIntervalMatrix(data);
		//return DoubleIntervalMatrix.typeCast(super.convert(data));
	}
	
	@Override
	public IntervalType elementType(){
		return DoubleInterval.type;
	}
	
	@Override
	public DoubleIntervalMatrix assign(CohoNumber v, int row, int col){
		super.assign(v,row,col);
		return this;
	}
	@Override
	public DoubleIntervalMatrix assign(Number v, int row, int col){
		super.assign(v,row,col);
		return this;
	}
	@Override
	public DoubleIntervalMatrix assign(CohoNumber v, int n){
		super.assign(v,n);//for vector
		return this;
	}
	@Override
	public DoubleIntervalMatrix assign(Number v, int n){
		super.assign(v,n);//for vector
		return this;
	}
	@Override
	public DoubleIntervalMatrix assign(Matrix m){
		super.assign(m);
		return this;
	}
	@Override
	public DoubleIntervalMatrix assign(CohoNumber[][] v){
		super.assign(v);
		return this;
	}
	@Override
	public DoubleIntervalMatrix assign(Number[][] v){
		super.assign(v);
		return this;
	}
	@Override
	public DoubleIntervalMatrix assign(CohoNumber[] v){
		super.assign(v);
		return this;
	}
	@Override
	public DoubleIntervalMatrix assign(Number[] v){
		super.assign(v);
		return this;
	}
	@Override
	public DoubleIntervalMatrix assign(Matrix m, int v_row, int v_col){
		super.assign(m,v_row,v_col);
		return this;
	}
	@Override
	public DoubleIntervalMatrix assign(Matrix m, int n){
		super.assign(m,n);
		return this;
	}
	@Override
	public DoubleIntervalMatrix assign(Matrix m, BooleanMatrix mask){
		super.assign(m,mask);
		return this;
	}
	@Override
	public DoubleIntervalMatrix assign(Matrix m, IntegerMatrix pos){
		super.assign(m,pos);//for vector
		return this;
	}
	@Override
	public DoubleIntervalMatrix V(Range row, Range col){
		return DoubleIntervalMatrix.typeCast(super.V(row,col));
	}
	@Override
	public DoubleIntervalMatrix V(Range n){
		return DoubleIntervalMatrix.typeCast(super.V(n));//for vector
	}
	@Override
	public DoubleIntervalMatrix V(Range row, int col){
		return DoubleIntervalMatrix.typeCast(super.V(row,col));
	}
	@Override
	public DoubleIntervalMatrix V(int row, Range col){
		return DoubleIntervalMatrix.typeCast(super.V(row,col));
	}
	@Override
	public DoubleIntervalMatrix V(IntegerMatrix pos){
		return DoubleIntervalMatrix.typeCast(super.V(pos));//for vector
	}
	@Override
	public DoubleIntervalMatrix row(int row){
		return DoubleIntervalMatrix.typeCast(super.row(row));
	}
	@Override
	public DoubleIntervalMatrix row(Range row){
		return DoubleIntervalMatrix.typeCast(super.row(row));
	}
	@Override
	public DoubleIntervalMatrix row(IntegerMatrix pos){
		return DoubleIntervalMatrix.typeCast(super.row(pos));
	}
	@Override
	public DoubleIntervalMatrix row(BooleanMatrix pos){
		return DoubleIntervalMatrix.typeCast(super.row(pos));
	}
	@Override
	public DoubleIntervalMatrix col(int col){
		return DoubleIntervalMatrix.typeCast(super.col(col));
	}
	@Override
	public DoubleIntervalMatrix col(Range col){
		return DoubleIntervalMatrix.typeCast(super.col(col));
	}
	@Override
	public DoubleIntervalMatrix col(IntegerMatrix pos){
		return DoubleIntervalMatrix.typeCast(super.col(pos));
	}
	@Override
	public DoubleIntervalMatrix col(BooleanMatrix pos){
		return DoubleIntervalMatrix.typeCast(super.col(pos));
	}

	@Override
	public DoubleIntervalMatrix ones(){
		return DoubleIntervalMatrix.typeCast(super.ones());
	}
	@Override
	public DoubleIntervalMatrix zeros(){
		return DoubleIntervalMatrix.typeCast(super.zeros());
	}
	@Override
	public DoubleIntervalMatrix randoms(){
		return DoubleIntervalMatrix.typeCast(super.randoms());
	}
	@Override
	public DoubleIntervalMatrix ident(){
		return DoubleIntervalMatrix.typeCast(super.ident());
	}
	@Override
	public DoubleIntervalMatrix ident(int n){
		return DoubleIntervalMatrix.typeCast(super.ident(n));
	}
	@Override
	public DoubleIntervalMatrix diag(){
		return DoubleIntervalMatrix.typeCast(super.diag());
	}
	@Override
	public DoubleIntervalMatrix fill(CohoNumber v){
		return DoubleIntervalMatrix.typeCast(super.fill(v));
	}
	@Override
	public DoubleIntervalMatrix fill(Number v){
		return DoubleIntervalMatrix.typeCast(super.fill(v));
	}

	@Override
	public DoubleIntervalMatrix abs(){
		return DoubleIntervalMatrix.typeCast(super.abs());
	}
	@Override
	public DoubleIntervalMatrix negate(){
		return DoubleIntervalMatrix.typeCast(super.negate());
	}
	@Override
	public DoubleIntervalMatrix transpose(){
		return DoubleIntervalMatrix.typeCast(super.transpose());
	}
	@Override
	public DoubleIntervalMatrix inv()throws SingularMatrixException{
		return DoubleIntervalMatrix.typeCast(super.inv());
	}
	
	public DoubleIntervalMatrix add(DoubleInterval x){
		return DoubleIntervalMatrix.typeCast(super.add(x));
	}
	public DoubleIntervalMatrix add(ScaleNumber x){
		return DoubleIntervalMatrix.typeCast(super.add(x));
	}
	public DoubleIntervalMatrix add(Number x){
		return DoubleIntervalMatrix.typeCast(super.add(x));
	}
	public DoubleIntervalMatrix sub(DoubleInterval x){
		return DoubleIntervalMatrix.typeCast(super.sub(x));
	}
	public DoubleIntervalMatrix sub(ScaleNumber x){
		return DoubleIntervalMatrix.typeCast(super.sub(x));
	}
	public DoubleIntervalMatrix sub(Number x){
		return DoubleIntervalMatrix.typeCast(super.sub(x));
	}
	public DoubleIntervalMatrix mult(DoubleInterval x){
		return DoubleIntervalMatrix.typeCast(super.mult(x));
	}
	public DoubleIntervalMatrix mult(ScaleNumber x){
		return DoubleIntervalMatrix.typeCast(super.mult(x));
	}
	public DoubleIntervalMatrix mult(Number x){
		return DoubleIntervalMatrix.typeCast(super.mult(x));
	}
	public DoubleIntervalMatrix div(DoubleInterval x){
		return DoubleIntervalMatrix.typeCast(super.div(x));
	}
	public DoubleIntervalMatrix div(ScaleNumber x){
		return DoubleIntervalMatrix.typeCast(super.div(x));
	}
	public DoubleIntervalMatrix div(Number x){
		return DoubleIntervalMatrix.typeCast(super.div(x));
	}
	
	public DoubleIntervalMatrix add(DoubleIntervalMatrix that){
		return DoubleIntervalMatrix.typeCast(super.add(that));
	}
	public DoubleIntervalMatrix sub(DoubleIntervalMatrix that){
		return DoubleIntervalMatrix.typeCast(super.sub(that));
	}
	public DoubleIntervalMatrix mult(DoubleIntervalMatrix that){
		return DoubleIntervalMatrix.typeCast(super.mult(that));
	}
	public DoubleIntervalMatrix div(DoubleIntervalMatrix that)throws SingularMatrixException{
		return DoubleIntervalMatrix.typeCast(super.div(that));
	}
	public DoubleIntervalMatrix elMult(DoubleIntervalMatrix that){
		return DoubleIntervalMatrix.typeCast(super.elMult(that));
	}
	public DoubleIntervalMatrix elDiv(DoubleIntervalMatrix that){
		return DoubleIntervalMatrix.typeCast(super.elDiv(that));
	}
	public DoubleIntervalMatrix leftDiv(DoubleIntervalMatrix that)throws SingularMatrixException{
		return DoubleIntervalMatrix.typeCast(super.leftDiv(that));
	}
	
	public static void main(String[] args){
		double[][] d ={{1, 2}, {2,3}};
		DoubleIntervalMatrix a = new DoubleIntervalMatrix(d);
//		System.out.println(a);
//		System.out.println(a.add(1));
		a = a.add(a);
		//a = a.add(new BasicMatrix<CohoInteger>(a.size()));
		System.out.println(a.add(a));
	}
}
