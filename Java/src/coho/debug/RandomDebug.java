package coho.debug;
/**
 * This class is to debug random problem.
 * @author chaoyan
 *
 */
public class RandomDebug {
	public static boolean debug = false;
	public static int counter = 0;
	public static void println(String s){
		if(debug){
			System.out.println(""+counter+" "+s);
				StackTraceElement[] stes = Thread.currentThread().getStackTrace();
				for(int i=0;i<stes.length;i++){
					StackTraceElement ste = stes[i];
					System.out.println(ste.toString());
				}		
			counter++;
		}		
	}
	public static void main(String[] args){
		debug = true;
		println("test");
	}
}
