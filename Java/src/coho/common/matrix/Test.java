package coho.common.matrix;
import coho.common.number.*;
public class Test {
	interface MatrixTest{
		public boolean test();
	}
	static MatrixTest subTest = new MatrixTest(){
		public boolean test(){
			BooleanMatrix b = new BooleanMatrix(2,3).randoms();
			IntegerMatrix i = new IntegerMatrix(2,2).ones();
			DoubleMatrix d = new DoubleMatrix(2,2).ones();
			DoubleIntervalMatrix di = new DoubleIntervalMatrix(2,2).ones();
			System.out.println(b);
			System.out.println(b.transpose());
			System.out.println(b.any());
			System.out.println(b.all());
			System.out.println(b.allInCol());
			System.out.println(b.allInRow());
			System.out.println(b.anyInRow());
			System.out.println(b.anyInCol());
			System.out.println(b.add(1));
			System.out.println(b.mult(b.transpose()));
			System.out.println(b.and(b));
			System.out.println(b.or(b));
			System.out.println(b.xor(b));
			System.out.println(b.not());
			
			d = DoubleMatrix.create(d.mult(i));
			System.out.println(d);
			System.out.println(d.add(0));
			System.out.println(d.sub(d));
			System.out.println(d.mult(d.add(1)));
			System.out.println(d.elDiv(d));
			System.out.println(d.dotProd(d));
			System.out.println(d.norm());
			System.out.println(d.max());
			System.out.println(d.min());
			System.out.println(d.sum());
			System.out.println(d.prod());
			
			di = DoubleIntervalMatrix.create(di.mult(d));
			System.out.println(di);
			System.out.println(di.add(0));
			System.out.println(di.sub(d));
			System.out.println(di.mult(d.add(1)));
			System.out.println(di.elDiv(d));
			System.out.println(di.dotProd(d));
			System.out.println(di.norm());
			System.out.println(di.max());
			System.out.println(di.min());
			System.out.println(di.sum());
			System.out.println(di.prod());
			return true;
		}
	};
	static MatrixTest valueTest = new MatrixTest(){
		public boolean test(){			
			BasicMatrix<CohoDouble> d = new BasicMatrix<CohoDouble>(CohoDouble.one,3,3).randoms();
			System.out.println(d);
			System.out.println(d.V(0,2));
			System.out.println(d.V(0,new Range(0,1)));
			System.out.println(d.V(new Range(2,2),new Range(0,1)));
			System.out.println(d.col(0));
			System.out.println(d.row(2));
			System.out.println(d.col(new Range(0,1)));
			System.out.println(d.col(new IntegerMatrix(new CohoInteger[]{CohoInteger.create(1),CohoInteger.create(2),CohoInteger.create(1),CohoInteger.create(2)})));
			System.out.println(d.diag().V(new IntegerMatrix(new CohoInteger[]{CohoInteger.create(1),CohoInteger.create(2),CohoInteger.create(1),CohoInteger.create(2)})));
			System.out.println(d.diag().V(1));
			System.out.println(d.diag().V(new Range(1,1)));
			
			System.out.println(d.assign(1,1,1));
			System.out.println(d.assign(CohoAPR.one.random(),1,1));
			System.out.println(d.assign(DoubleInterval.one,1,1));
			System.out.println(d.assign(d.add(1)));
			System.out.println(d.diag().assign(new CohoInteger[]{CohoInteger.one,CohoInteger.one,CohoInteger.one}));
			System.out.println(d.assign(d.add(1).V(new Range(0,1),new Range(0,1)),1,1));
			System.out.println(d.assign(d.add(1),
					new BooleanMatrix(new boolean[][]{{true,true,true},{false,false,false},{true,true,true}})));
			System.out.println(d.diag().assign(d.add(1).V(new Range(0,0),new Range(0,1)),
			new IntegerMatrix(new CohoInteger[]{CohoInteger.create(1),CohoInteger.create(2)})));
			
			d = new BasicMatrix<CohoDouble>(CohoDouble.one,0,0).randoms();
			//System.out.println(d.assign(0,1,1));
			return true;
		}
	};
	static MatrixTest numberTest = new MatrixTest(){
		public boolean test(){
			BasicMatrix<CohoBoolean> b = new BasicMatrix<CohoBoolean>(CohoBoolean.one,1,1).ones();
			BasicMatrix<CohoInteger> i = new BasicMatrix<CohoInteger>(CohoInteger.one,1,1).ones();
			BasicMatrix<CohoLong> l = new BasicMatrix<CohoLong>(CohoLong.one,1,1).ones();
			BasicMatrix<CohoDouble> d = new BasicMatrix<CohoDouble>(CohoDouble.one,1,1).ones();
			BasicMatrix<CohoAPR> a = new BasicMatrix<CohoAPR>(CohoAPR.one,1,1).ones();
			BasicMatrix<IntegerInterval> ii = new BasicMatrix<IntegerInterval>(IntegerInterval.one,1,1).ones();
			BasicMatrix<DoubleInterval> di = new BasicMatrix<DoubleInterval>(DoubleInterval.one,1,1).ones();
			BasicMatrix<APRInterval> ai = new BasicMatrix<APRInterval>(APRInterval.one,1,1).ones();
			APRInterval aa = APRInterval.one.random();
			System.out.println(aa);
			System.out.println(aa.doubleValue());
			System.out.println(b.add(aa));
			System.out.println(i.sub(aa));
			System.out.println(l.mult(aa));
			System.out.println(d.mult(aa));
			System.out.println(a.mult(aa));
			System.out.println(ii.mult(aa));
			System.out.println(di.add(aa));
			System.out.println(ai.mult(aa));
			return true;
		}
	};
	static MatrixTest promoteTest = new MatrixTest(){
		public boolean test(){
			BasicMatrix<CohoBoolean> b = new BasicMatrix<CohoBoolean>(CohoBoolean.one,1,1).randoms();
			BasicMatrix<CohoInteger> i = new BasicMatrix<CohoInteger>(CohoInteger.one,1,1).randoms();
			BasicMatrix<CohoLong> l = new BasicMatrix<CohoLong>(CohoLong.one,1,1).randoms();
			BasicMatrix<CohoDouble> d = new BasicMatrix<CohoDouble>(CohoDouble.one,1,1).randoms();
			BasicMatrix<CohoAPR> a = new BasicMatrix<CohoAPR>(CohoAPR.one,1,1).randoms();
			BasicMatrix<IntegerInterval> ii = new BasicMatrix<IntegerInterval>(IntegerInterval.one,1,1).randoms();
			BasicMatrix<DoubleInterval> di = new BasicMatrix<DoubleInterval>(DoubleInterval.one,1,1).randoms();
			BasicMatrix<APRInterval> ai = new BasicMatrix<APRInterval>(APRInterval.one,1,1).randoms();
			System.out.println(b);
			System.out.println(i);
			System.out.println(l);
			System.out.println(d);
			System.out.println(a);
			System.out.println(ii);
			System.out.println(di);
			System.out.println(ai);
			System.out.println(b.add(i));
			System.out.println(i.sub(l));
			System.out.println(l.mult(d));
			System.out.println(d.elDiv(a));
			System.out.println(a.dotProd(ii));
			System.out.println(ii.add(di));
			System.out.println(di.mult(ai));
			System.out.println(ai.sub(b));
			System.out.println(b.elMult(ai));
			return true;
		}
	};
	static MatrixTest doubleTest = new MatrixTest(){
		public boolean test(){
			BasicMatrix<CohoBoolean> b = new BasicMatrix<CohoBoolean>(CohoBoolean.one,3,3).randoms();
			BasicMatrix<CohoInteger> i = new BasicMatrix<CohoInteger>(CohoInteger.one,3,3).randoms();
			BasicMatrix<CohoLong> l = new BasicMatrix<CohoLong>(CohoLong.one,3,3).randoms();
			BasicMatrix<CohoDouble> d = new BasicMatrix<CohoDouble>(CohoDouble.one,3,3).randoms();
			BasicMatrix<CohoAPR> a = new BasicMatrix<CohoAPR>(CohoAPR.one,3,3).randoms();
			BasicMatrix<IntegerInterval> ii = new BasicMatrix<IntegerInterval>(IntegerInterval.one,3,3).randoms();
			BasicMatrix<DoubleInterval> di = new BasicMatrix<DoubleInterval>(DoubleInterval.one,3,3).randoms();
			BasicMatrix<APRInterval> ai = new BasicMatrix<APRInterval>(APRInterval.one,3,3).randoms();
			
			System.out.println(d);
			System.out.println(b);
			System.out.println(d.mult(b));
			System.out.println(i);
			System.out.println(d.elDiv(i));
			System.out.println(l);
			System.out.println(l.sub(d));
			System.out.println(a);
			System.out.println(d.add(a));
			System.out.println(ii);
			System.out.println(d.mult(ii));
			System.out.println(di);
			System.out.println(di.mult(d));
			System.out.println(ai);
			System.out.println(d.add(ai));
			return true;
		}
	};
	static MatrixTest intervalTest = new MatrixTest(){
		public boolean test(){
			BasicMatrix<APRInterval> ai = new BasicMatrix<APRInterval>(APRInterval.one,3,3).randoms();

			BasicMatrix<APRInterval> randoms = ai.randoms();
			APRInterval random = ai.random();
			System.out.println(ai);
			System.out.println(randoms);
			System.out.println(random);

			System.out.println(ai.nrows());
			System.out.println(ai.ncols());
			System.out.println(ai.size());
			System.out.println(ai.elementType());
			System.out.println(ai.isSquare());
			System.out.println(ai.isVector());
			System.out.println(ai.ones());
			System.out.println(ai.zeros());
			System.out.println(ai.ident());
			System.out.println(ai.ident(2));
			System.out.println(ai.fill(10));
			System.out.println(ai.abs());
			System.out.println(ai.negate());
			System.out.println(ai.max());
			System.out.println(ai.min());
			System.out.println(ai.prod());
			System.out.println(ai.sum());
			System.out.println(DoubleInterval.create(ai.dotProd(ai)).sqrt());

			System.out.println(ai.add(randoms));
			System.out.println(ai.add(random));
			System.out.println(ai.sub(randoms));
			System.out.println(ai.sub(random));
			System.out.println(ai.mult(randoms));
			System.out.println(ai.mult(randoms));
			System.out.println(ai.elMult(randoms));
			System.out.println(ai.elDiv(randoms));
			System.out.println(ai.less(randoms));
			System.out.println(ai.dotProd(randoms));
			
			System.out.println(ai.eq(randoms));
			System.out.println(ai.neq(randoms));
			System.out.println(ai.less(randoms));
			System.out.println(ai.leq(randoms));
			System.out.println(ai.greater(randoms));
			System.out.println(ai.geq(randoms));
			return true;
		}
	};
	public static void main(String[] args){
		//intervalTest.test();
		//doubleTest.test();
		//promoteTest.test();
		//numberTest.test();
		//valueTest.test();
		subTest.test();
	}
}
