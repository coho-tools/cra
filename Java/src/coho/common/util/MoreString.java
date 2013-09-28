package coho.common.util;

public class MoreString{
	/*
	 * return a string that has n s0
	 */
	public static String multiply(String s0, int n){
		char[] ac0 = s0.toCharArray();
		char[] ac = new char[ac0.length*n];
		for (int i = 0; i < n; i++){
			for (int j = 0; j < ac0.length; j++){
				ac[i*ac0.length+j] = ac0[j];
			}
		}
		String s = new String(ac);
		return s;
	}    
}