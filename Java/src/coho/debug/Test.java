package coho.debug;

public class Test {
	public int a = 10;
//	public Test(int b){
//	System.out.println(a);
//	a = 0;
//	}
	public Test(){
		System.out.println(a);
		a =0;
	}
	public static void main(String[] args){
//		Test a = new Test();
//		System.out.println(a.a);
		String aa = "";
		System.out.println(aa.length());
//		a = new Test();
//		System.out.println(a.a);
		System.out.println(-10%3);
		System.out.println(Double.POSITIVE_INFINITY>Double.MAX_VALUE);
		System.out.println(Double.POSITIVE_INFINITY*1.2==Double.POSITIVE_INFINITY);
	}
}

