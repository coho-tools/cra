package coho.common.matrix;
import coho.common.number.*;
/*
 * wrapper for BasicMatrix<CohoAPR> 1)type cast 2)static method.
 * The functions for BasicMatrix<CohoAPR> and DoubleMatrix are the same 
 */
public class APRMatrix extends BasicMatrix<CohoAPR> {
	//for type cast
	protected APRMatrix(CohoAPR[][] data, int nrows, int ncols){		
		super(CohoAPR.type,nrows,ncols,data);
	}
	public static APRMatrix typeCast(BasicMatrix<CohoAPR> m){
		return new APRMatrix(m.data,m.nrows,m.ncols);
	}
	
	//new functions
	public APRMatrix(int nrows, int ncols){
		super(CohoAPR.zero,nrows,ncols);
	}
	public APRMatrix(int[] size){
		this(size[0],size[1]);
	}
	public APRMatrix(Matrix m){
		this(m.nrows(),m.ncols());
		super.assign(m);
	}
	public APRMatrix(CohoNumber[][] data){
		this(data.length,data[0].length);
		super.assign(data);
	}
	public APRMatrix(CohoNumber[] data){
		this(data.length,1);
		super.assign(data);
	}
	public APRMatrix(Number[][] data){
		this(data.length,data[0].length);
		super.assign(data);
	}
	public APRMatrix(double[][] data){
		this(data.length,data[0].length);
		for(int i=0; i<nrows; i++){
			for(int j=0; j<ncols; j++){
				super.assign(data[i][j],i,j);
			}
		}
	}
	public APRMatrix(Number[] data){
		this(data.length,1);
		super.assign(data);
	}
	public APRMatrix(double[] data){
		this(data.length,1);
		for(int i=0; i<length(); i++){
			super.assign(data[i],i);
		}
	}
	
	public static APRMatrix create(int nrows, int ncols){
		return new APRMatrix(nrows,ncols);
	}
	public static APRMatrix create(int[] size){
		return new APRMatrix(size);
	}
	public static APRMatrix create(Matrix m){
		if(m instanceof APRMatrix)
			return (APRMatrix)m;
		return new APRMatrix(m);
	}
	public static APRMatrix create(CohoNumber[][] data){
		return new APRMatrix(data);
	}
	public static APRMatrix create(CohoNumber[] data){
		return new APRMatrix(data);
	}
	public static APRMatrix create(Number[][] data){
		return new APRMatrix(data);
	}
	public static APRMatrix create(Number[] data){
		return new APRMatrix(data);
	}
	public static APRMatrix create(double[][] data){
		return new APRMatrix(data);
	}
	public static APRMatrix create(double[] data){
		return new APRMatrix(data);
	}
	
	public APRMatrix convert(double[][] data){
		return new APRMatrix(data);
	}
	public APRMatrix convert(double[] data){
		return new APRMatrix(data);
	}
	

	
	@Override
	public APRMatrix convert(){
		return new APRMatrix(nrows,ncols);
		//return APRMatrix.typeCast(super.convert());
	}
	@Override
	public APRMatrix convert(int nrows, int ncols){
		return new APRMatrix(nrows,ncols);
		//return APRMatrix.typeCast(super.convert(nrows, ncols));
	}
	@Override
	public APRMatrix convert(int[] size){
		return new APRMatrix(size);
		//return APRMatrix.typeCast(super.convert(size));
	}
	@Override
	public APRMatrix convert(Matrix m){
		if(m instanceof APRMatrix)
			return (APRMatrix)m;
		return new APRMatrix(m);
		//return APRMatrix.typeCast(super.convert(m));
	}
	@Override
	public APRMatrix convert(CohoNumber[][] data){
		return new APRMatrix(data);
		//return APRMatrix.typeCast(super.convert(data));
	}
	@Override
	public APRMatrix convert(CohoNumber[] data){
		return new APRMatrix(data);
		//return APRMatrix.typeCast(super.convert(data));
	}
	@Override
	public APRMatrix convert(Number[][] data){
		return new APRMatrix(data);
		//return APRMatrix.typeCast(super.convert(data));
	}
	@Override
	public APRMatrix convert(Number[] data){
		return new APRMatrix(data);
		//return APRMatrix.typeCast(super.convert(data));
	}
	
	@Override
	public ScaleType elementType(){
		return CohoAPR.type;
	}
	
	@Override
	public APRMatrix assign(CohoNumber v, int row, int col){
		super.assign(v,row,col);
		return this;
	}
	@Override
	public APRMatrix assign(Number v, int row, int col){
		super.assign(v,row,col);
		return this;
	}
	@Override
	public APRMatrix assign(CohoNumber v, int n){
		super.assign(v,n);//for vector
		return this;
	}
	@Override
	public APRMatrix assign(Number v, int n){
		super.assign(v,n);//for vector
		return this;
	}
	@Override
	public APRMatrix assign(Matrix m){
		super.assign(m);
		return this;
	}
	@Override
	public APRMatrix assign(CohoNumber[][] v){
		super.assign(v);
		return this;
	}
	@Override
	public APRMatrix assign(Number[][] v){
		super.assign(v);
		return this;
	}
	@Override
	public APRMatrix assign(CohoNumber[] v){
		super.assign(v);
		return this;
	}
	@Override
	public APRMatrix assign(Number[] v){
		super.assign(v);
		return this;
	}
	@Override
	public APRMatrix assign(Matrix m, int v_row, int v_col){
		super.assign(m,v_row,v_col);
		return this;
	}
	@Override
	public APRMatrix assign(Matrix m, int n){
		super.assign(m,n);
		return this;
	}
	@Override
	public APRMatrix assign(Matrix m, BooleanMatrix mask){
		super.assign(m,mask);
		return this;
	}
	@Override
	public APRMatrix assign(Matrix m, IntegerMatrix pos){
		super.assign(m,pos);//for vector
		return this;
	}
	@Override
	public APRMatrix V(Range row, Range col){
		return APRMatrix.typeCast(super.V(row,col));
	}
	@Override
	public APRMatrix V(Range n){
		return APRMatrix.typeCast(super.V(n));//for vector
	}
	@Override
	public APRMatrix V(Range row, int col){
		return APRMatrix.typeCast(super.V(row,col));
	}
	@Override
	public APRMatrix V(int row, Range col){
		return APRMatrix.typeCast(super.V(row,col));
	}
	@Override
	public APRMatrix V(IntegerMatrix pos){
		return APRMatrix.typeCast(super.V(pos));//for vector
	}
	@Override
	public APRMatrix row(int row){
		return APRMatrix.typeCast(super.row(row));
	}
	@Override
	public APRMatrix row(Range row){
		return APRMatrix.typeCast(super.row(row));
	}
	@Override
	public APRMatrix row(IntegerMatrix pos){
		return APRMatrix.typeCast(super.row(pos));
	}
	@Override
	public APRMatrix row(BooleanMatrix pos){
		return APRMatrix.typeCast(super.row(pos));
	}
	@Override
	public APRMatrix col(int col){
		return APRMatrix.typeCast(super.col(col));
	}
	@Override
	public APRMatrix col(Range col){
		return APRMatrix.typeCast(super.col(col));
	}
	@Override
	public APRMatrix col(IntegerMatrix pos){
		return APRMatrix.typeCast(super.col(pos));
	}
	@Override
	public APRMatrix col(BooleanMatrix pos){
		return APRMatrix.typeCast(super.col(pos));
	}

	@Override
	public APRMatrix ones(){
		return APRMatrix.typeCast(super.ones());
	}
	@Override
	public APRMatrix zeros(){
		return APRMatrix.typeCast(super.zeros());
	}
	@Override
	public APRMatrix randoms(){
		return APRMatrix.typeCast(super.randoms());
	}
	@Override
	public APRMatrix ident(){
		return APRMatrix.typeCast(super.ident());
	}
	@Override
	public APRMatrix ident(int n){
		return APRMatrix.typeCast(super.ident(n));
	}
	@Override
	public APRMatrix diag(){
		return APRMatrix.typeCast(super.diag());
	}
	@Override
	public APRMatrix fill(CohoNumber v){
		return APRMatrix.typeCast(super.fill(v));
	}
	@Override
	public APRMatrix fill(Number v){
		return APRMatrix.typeCast(super.fill(v));
	}

	@Override
	public APRMatrix abs(){
		return APRMatrix.typeCast(super.abs());
	}
	@Override
	public APRMatrix negate(){
		return APRMatrix.typeCast(super.negate());
	}
	@Override
	public APRMatrix transpose(){
		return APRMatrix.typeCast(super.transpose());
	}
	@Override
	public APRMatrix inv() throws SingularMatrixException{
		return APRMatrix.typeCast(super.inv());
	}
	
	public APRMatrix add(ScaleNumber x){
		return APRMatrix.typeCast(super.add(x));
	}
	public APRMatrix add(Number x){
		return APRMatrix.typeCast(super.add(x));
	}
	public APRMatrix sub(ScaleNumber x){
		return APRMatrix.typeCast(super.sub(x));
	}
	public APRMatrix sub(Number x){
		return APRMatrix.typeCast(super.sub(x));
	}
	public APRMatrix mult(ScaleNumber x){
		return APRMatrix.typeCast(super.mult(x));
	}
	public APRMatrix mult(Number x){
		return APRMatrix.typeCast(super.mult(x));
	}
	public APRMatrix div(ScaleNumber x){
		return APRMatrix.typeCast(super.div(x));
	}
	public APRMatrix div(Number x){
		return APRMatrix.typeCast(super.div(x));
	}
	
	public APRMatrix add(APRMatrix that){
		return APRMatrix.typeCast(super.add(that));
	}
	public APRMatrix sub(APRMatrix that){
		return APRMatrix.typeCast(super.sub(that));
	}
	public APRMatrix mult(APRMatrix that){
		return APRMatrix.typeCast(super.mult(that));
	}
	public APRMatrix div(APRMatrix that)throws SingularMatrixException{
		return APRMatrix.typeCast(super.div(that));
	}
	public APRMatrix elMult(APRMatrix that){
		return APRMatrix.typeCast(super.elMult(that));
	}
	public APRMatrix elDiv(APRMatrix that){
		return APRMatrix.typeCast(super.elDiv(that));
	}
	public APRMatrix leftDiv(APRMatrix that)throws SingularMatrixException{
		return APRMatrix.typeCast(super.leftDiv(that));
	}
	
	public static void main(String[] args){
		double[][] d ={{1, 2}, {2,3}};
		APRMatrix a = new APRMatrix(d);
//		System.out.println(a);
//		System.out.println(a.add(1));
		a = a.add(a);
		//a = a.add(new BasicMatrix<CohoInteger>(a.size()));
		System.out.println(a.add(a));
	}
}
