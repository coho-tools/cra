package coho.debug;

import java.io.*;
import coho.common.matrix.*;
import coho.common.util.*;

public class DEBUG {
	// we add debug flag to control the assert. 
	// We only print the msg in the debug mode, otherwise, we do nothing.
	public static final boolean debug = false;
	/****************************
	 *Functions for trace the program
	 ****************************/
	static public String commentOut(String lines) {
		if (lines.length() == 0)
			return "";
		if (lines.charAt(lines.length() - 1) == '\n') {
			lines = lines.substring(0, lines.length() - 1);
		}
		lines = "% " + lines.replace("\n", "\n% ") + "\n";
		return lines;
	}

	public static void print(String msg) {
		msg = commentOut(msg);
		System.out.print(msg);

	}

	public static void print(boolean condition, String msg) {
		if (condition)
			print(msg);
	}

	public static void println(String msg) {
		print(msg + "\n");
	}
	public static void println(boolean condition, String msg){
		if(condition)
			println(msg);
	}
	public static void println(String s, String prefix) {
		s=s.replace("\n","\n"+prefix+" : ");
		println(""+prefix+" : " + s);
	}
	public static void println(boolean condition, String s, String prefix){
		if(condition){
			s=s.replace("\n","\n"+prefix+" : ");
			println(""+prefix+" : " + s);			
		}
	}
	
	public static String readLine() {
		String line = "";
		try {
			InputStreamReader isr = new InputStreamReader(System.in);
			BufferedReader br = new BufferedReader(isr);
			line = br.readLine();
		} catch (Exception e) {
		}
		return line;
	}

	public static int readInt(String prompt) {
		// one integer per line; but this is just a debugging aid;
		// for more items per line, use StringTokenizer;
		System.out.print(prompt);
		int reading = 0;
		try {
			String line = readLine();
			reading = Integer.parseInt(line);
		} catch (Exception e) {
		}
		return reading;
	}

	public static double readDouble(String prompt) {
		// one integer per line; but this is just a debugging aid;
		// for more items per line, use StringTokenizer;
		System.out.print(prompt);
		double reading = Double.NaN;
		try {
			String line = readLine();
			reading = Double.valueOf(line).doubleValue();
		} catch (Exception e) {
		}
		return reading;
	}

	public static String toString(double[] v) {
		StringBuffer buf = new StringBuffer();
		buf.append("[");
		for (int i = 0; i < v.length; i++) {
			if (i > 0)
				buf.append(", ");
			buf.append("" + v[i]);
		}
		buf.append("]");
		return (buf.toString());
	}

	public static String toHexString(double[] v) {
		if (v == null)
			return "<NULL>";
		return (DoubleMatrix.create(v)).toString().replace('\n', ' ');
	}

	public static String toHexString(double d) {
		return "$" + MoreLong.toHexString(Double.doubleToLongBits(d));
	}

	public static String toString(int[] v) {
		StringBuffer buf = new StringBuffer();
		buf.append("[");
		for (int i = 0; i < v.length; i++) {
			if (i > 0)
				buf.append(", ");
			buf.append("" + v[i]);
		}
		buf.append("]");
		return (buf.toString());
	}

}
