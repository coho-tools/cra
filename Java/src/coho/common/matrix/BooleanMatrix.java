package coho.common.matrix;
import coho.common.number.*;
/*
 * wrapper for BasicMatrix<CohoDouble> 1)type cast 2)static method.
 * The functions for BasicMatrix<CohoDouble> and DoubleMatrix are the same 
 */
public class BooleanMatrix extends BasicMatrix<CohoBoolean> {
	//for type cast
	protected BooleanMatrix(CohoBoolean[][] data, int nrows, int ncols){		
		super(CohoBoolean.type,nrows,ncols,data);
	}
	public static BooleanMatrix typeCast(BasicMatrix<CohoBoolean> m){
		return new BooleanMatrix(m.data,m.nrows,m.ncols);
	}
	
	//new functions
	public BooleanMatrix(int nrows, int ncols){
		super(CohoBoolean.zero,nrows,ncols);
	}
	public BooleanMatrix(int[] size){
		this(size[0],size[1]);
	}
	public BooleanMatrix(Matrix m){
		this(m.nrows(),m.ncols());
		super.assign(m);
	}
	public BooleanMatrix(CohoNumber[][] data){
		this(data.length,data[0].length);
		super.assign(data);
	}
	public BooleanMatrix(CohoNumber[] data){
		this(data.length,1);
		super.assign(data);
	}
	public BooleanMatrix(Number[][] data){
		this(data.length,data[0].length);
		super.assign(data);
	}
	public BooleanMatrix(boolean[][] data){
		this(data.length,data[0].length);
		for(int i=0; i<nrows; i++){
			for(int j=0; j<ncols; j++){
				super.assign(CohoBoolean.create(data[i][j]),i,j);
			}
		}
	}
	public BooleanMatrix(Number[] data){
		this(data.length,1);
		super.assign(data);
	}	
	public BooleanMatrix(boolean[] data){
		this(data.length,1);
		for(int i=0; i<length(); i++){
			super.assign(CohoBoolean.create(data[i]),i);
		}
	}
	
	public static BooleanMatrix create(int nrows, int ncols){
		return new BooleanMatrix(nrows,ncols);
	}
	public static BooleanMatrix create(int[] size){
		return new BooleanMatrix(size);
	}
	public static BooleanMatrix create(Matrix m){
		if(m instanceof BooleanMatrix)
			return (BooleanMatrix)m;
		return new BooleanMatrix(m);
	}
	public static BooleanMatrix create(CohoNumber[][] data){
		return new BooleanMatrix(data);
	}
	public static BooleanMatrix create(CohoNumber[] data){
		return new BooleanMatrix(data);
	}
	public static BooleanMatrix create(Number[][] data){
		return new BooleanMatrix(data);
	}
	public static BooleanMatrix create(Number[] data){
		return new BooleanMatrix(data);
	}
	public static BooleanMatrix create(boolean[][] data){
		return new BooleanMatrix(data);
	}
	public static BooleanMatrix create(boolean[] data){
		return new BooleanMatrix(data);
	}
	
	public BooleanMatrix convert(boolean[][] data){
		return new BooleanMatrix(data);
	}
	public BooleanMatrix convert(boolean[] data){
		return new BooleanMatrix(data);
	}
	
	/**
	 * Additional functions for booleanMatrix
	 */
	public BooleanMatrix assign(boolean v,int nrow, int ncol){
		return assign(CohoBoolean.create(v),nrow,ncol);
	}
	public BooleanMatrix assign(boolean v, int pos){
		return assign(CohoBoolean.create(v),pos);
	}
    public IntegerMatrix find() {
    	int n=0;
    	for(int i=0; i<nrows; i++){
    		for(int j=0; j<ncols; j++){
    			if(V(i,j).booleanValue())
    				n++;
    		}
    	}
    	IntegerMatrix m;
        if(isVector()) { // return a row or colum, according to this.
            m = IntegerMatrix.create(n,1);
            int ii = 0;
            for(int i = 0; i < length(); i++){
                if(V(i).booleanValue()){
                    m.assign(i, ii++);
                }
            }
            
        } else {  // return a n*2 matrix of indices
            m = new IntegerMatrix(n, 2);
            int ii = 0;
            for(int i = 0; i < nrows(); i++) {
                for(int j = 0; j < ncols(); j++) {
                    if(V(i, j).booleanValue()) {
                        m.assign(i, ii, 0);
                        m.assign(j, ii, 1);
                        ii++;
                    }
                }
            }
        }
        return m;
    }
	public boolean all(){
		return prod().booleanValue();
	}
	//based on the definition of add()
	public boolean any(){
		return sum().booleanValue();
	}
	public BooleanMatrix allInRow(){
		BooleanMatrix result = new BooleanMatrix(nrows,1);
		for(int i=0; i<result.length();i++){
			result.assign(row(i).all(),i);
		}
		return result;
	}
	public BooleanMatrix allInCol(){
		BooleanMatrix result = new BooleanMatrix(1,ncols);
		for(int i=0; i<result.length();i++){
			result.assign(col(i).all(),i);
		}
		return result;		
	}
	public BooleanMatrix anyInRow(){
		BooleanMatrix result = new BooleanMatrix(nrows,1);
		for(int i=0; i<result.length();i++){
			result.assign(row(i).any(),i);
		}
		return result;
	}
	public BooleanMatrix anyInCol(){
		BooleanMatrix result = new BooleanMatrix(1,ncols);
		for(int i=0; i<result.length();i++){
			result.assign(col(i).any(),i);
		}
		return result;		
	}
	public BooleanMatrix and(BooleanMatrix that){
		return elMult(that);
	}
	public BooleanMatrix or(BooleanMatrix that){
		return add(that);
	}
	public BooleanMatrix not(){
		return negate();
	}
	public BooleanMatrix xor(BasicMatrix<CohoBoolean> that){
		sameDims(that);
		if(isVector()){
			boolean[] result = new boolean[length()];
			for(int i=0; i<length(); i++){
				result[i]=V(i).xor(that.V(i)).booleanValue();
			}
			return create(result);
		}else{
			boolean[][]result = new boolean[nrows][ncols];
			for(int i=0; i<nrows; i++){
				for(int j=0; j<ncols; j++){
					result[i][j] = V(i,j).xor(that.V(i,j)).booleanValue(); 
				}
			}
			return create(result);
		}
	}


	
	@Override
	public BooleanMatrix convert(){
		return new BooleanMatrix(nrows,ncols);
		//return BooleanMatrix.typeCast(super.convert());
	}
	@Override
	public BooleanMatrix convert(int nrows, int ncols){
		return new BooleanMatrix(nrows,ncols);
		//return BooleanMatrix.typeCast(super.convert(nrows, ncols));
	}
	@Override
	public BooleanMatrix convert(int[] size){
		return new BooleanMatrix(size);
		//return BooleanMatrix.typeCast(super.convert(size));
	}
	@Override
	public BooleanMatrix convert(Matrix m){
		if(m instanceof BooleanMatrix)
			return (BooleanMatrix)m;
		return new BooleanMatrix(m);
		//return BooleanMatrix.typeCast(super.convert(m));
	}
	@Override
	public BooleanMatrix convert(CohoNumber[][] data){
		return new BooleanMatrix(data);
		//return BooleanMatrix.typeCast(super.convert(data));
	}
	@Override
	public BooleanMatrix convert(CohoNumber[] data){
		return new BooleanMatrix(data);
		//return BooleanMatrix.typeCast(super.convert(data));
	}
	@Override
	public BooleanMatrix convert(Number[][] data){
		return new BooleanMatrix(data);
		//return BooleanMatrix.typeCast(super.convert(data));
	}
	@Override
	public BooleanMatrix convert(Number[] data){
		return new BooleanMatrix(data);
		//return BooleanMatrix.typeCast(super.convert(data));
	}
	
	@Override
	public ScaleType elementType(){
		return CohoBoolean.type;
	}
	
	@Override
	public BooleanMatrix assign(CohoNumber v, int row, int col){
		super.assign(v,row,col);
		return this;
	}
	@Override
	public BooleanMatrix assign(Number v, int row, int col){
		super.assign(v,row,col);
		return this;
	}
	@Override
	public BooleanMatrix assign(CohoNumber v, int n){
		super.assign(v,n);//for vector
		return this;
	}
	@Override
	public BooleanMatrix assign(Number v, int n){
		super.assign(v,n);//for vector
		return this;
	}
	@Override
	public BooleanMatrix assign(Matrix m){
		super.assign(m);
		return this;
	}
	@Override
	public BooleanMatrix assign(CohoNumber[][] v){
		super.assign(v);
		return this;
	}
	@Override
	public BooleanMatrix assign(Number[][] v){
		super.assign(v);
		return this;
	}
	@Override
	public BooleanMatrix assign(CohoNumber[] v){
		super.assign(v);
		return this;
	}
	@Override
	public BooleanMatrix assign(Number[] v){
		super.assign(v);
		return this;
	}
	@Override
	public BooleanMatrix assign(Matrix m, int v_row, int v_col){
		super.assign(m,v_row,v_col);
		return this;
	}
	@Override
	public BooleanMatrix assign(Matrix m, int n){
		super.assign(m,n);
		return this;
	}
	@Override
	public BooleanMatrix assign(Matrix m, BooleanMatrix mask){
		super.assign(m,mask);
		return this;
	}
	@Override
	public BooleanMatrix assign(Matrix m, IntegerMatrix pos){
		super.assign(m,pos);//for vector
		return this;
	}
	@Override
	public BooleanMatrix V(Range row, Range col){
		return BooleanMatrix.typeCast(super.V(row,col));
	}
	@Override
	public BooleanMatrix V(Range n){
		return BooleanMatrix.typeCast(super.V(n));//for vector
	}
	@Override
	public BooleanMatrix V(Range row, int col){
		return BooleanMatrix.typeCast(super.V(row,col));
	}
	@Override
	public BooleanMatrix V(int row, Range col){
		return BooleanMatrix.typeCast(super.V(row,col));
	}
	@Override
	public BooleanMatrix V(IntegerMatrix pos){
		return BooleanMatrix.typeCast(super.V(pos));//for vector
	}
	@Override
	public BooleanMatrix row(int row){
		return BooleanMatrix.typeCast(super.row(row));
	}
	@Override
	public BooleanMatrix row(Range row){
		return BooleanMatrix.typeCast(super.row(row));
	}
	@Override
	public BooleanMatrix row(IntegerMatrix pos){
		return BooleanMatrix.typeCast(super.row(pos));
	}
	@Override
	public BooleanMatrix row(BooleanMatrix mask){
		return BooleanMatrix.typeCast(super.row(mask));
	}
	@Override
	public BooleanMatrix col(int col){
		return BooleanMatrix.typeCast(super.col(col));
	}
	@Override
	public BooleanMatrix col(Range col){
		return BooleanMatrix.typeCast(super.col(col));
	}
	@Override
	public BooleanMatrix col(IntegerMatrix pos){
		return BooleanMatrix.typeCast(super.col(pos));
	}
	@Override
	public BooleanMatrix col(BooleanMatrix mask){
		return BooleanMatrix.typeCast(super.col(mask));
	}

	@Override
	public BooleanMatrix ones(){
		return BooleanMatrix.typeCast(super.ones());
	}
	@Override
	public BooleanMatrix zeros(){
		return BooleanMatrix.typeCast(super.zeros());
	}
	@Override
	public BooleanMatrix randoms(){
		return BooleanMatrix.typeCast(super.randoms());
	}
	@Override
	public BooleanMatrix ident(){
		return BooleanMatrix.typeCast(super.ident());
	}
	@Override
	public BooleanMatrix ident(int n){
		return BooleanMatrix.typeCast(super.ident(n));
	}
	@Override
	public BooleanMatrix diag(){
		return BooleanMatrix.typeCast(super.diag());
	}
	@Override
	public BooleanMatrix fill(CohoNumber v){
		return BooleanMatrix.typeCast(super.fill(v));
	}
	@Override
	public BooleanMatrix fill(Number v){
		return BooleanMatrix.typeCast(super.fill(v));
	}

	@Override
	public BooleanMatrix abs(){
		return BooleanMatrix.typeCast(super.abs());
	}
	@Override
	public BooleanMatrix negate(){
		return BooleanMatrix.typeCast(super.negate());
	}
	@Override
	public BooleanMatrix transpose(){
		return BooleanMatrix.typeCast(super.transpose());
	}
	@Override
	public BooleanMatrix inv()throws SingularMatrixException{
		return BooleanMatrix.typeCast(super.inv());
	}
		
	public BooleanMatrix add(BooleanMatrix that){
		return BooleanMatrix.typeCast(super.add(that));
	}
	public BooleanMatrix sub(BooleanMatrix that){
		return BooleanMatrix.typeCast(super.sub(that));
	}
	public BooleanMatrix mult(BooleanMatrix that){
		return BooleanMatrix.typeCast(super.mult(that));
	}
	public BooleanMatrix div(BooleanMatrix that)throws SingularMatrixException{
		return BooleanMatrix.typeCast(super.div(that));
	}
	public BooleanMatrix elMult(BooleanMatrix that){
		return BooleanMatrix.typeCast(super.elMult(that));
	}
	public BooleanMatrix elDiv(BooleanMatrix that){
		return BooleanMatrix.typeCast(super.elDiv(that));
	}
	public BooleanMatrix leftDiv(BooleanMatrix that)throws SingularMatrixException{
		return BooleanMatrix.typeCast(super.leftDiv(that));
	}
	
	public static void main(String[] args){
		boolean[][] d ={{true, false}, {true,false}};
		BooleanMatrix a = new BooleanMatrix(d);
//		System.out.println(a);
//		System.out.println(a.add(1));
		a = a.add(a);
		//a = a.add(new BasicMatrix<CohoInteger>(a.size()));
		System.out.println(a.add(a));
		System.out.println(BooleanMatrix.create(new boolean[10][1]));
	}
}
