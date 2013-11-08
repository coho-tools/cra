package coho.common.matrix;

public class Range {
	private int lo; public int lo() { return(lo); }
	private int hi; public int hi() { return(hi); }
	public Range(int _lo, int _hi) {
		lo = Math.min(_lo,_hi);
		hi = Math.max(_lo, _hi);
	}
	public int length(){
		return hi-lo+1;
	}
	public String toString() {
		return("range(" + lo + ", " + hi + ")");
	}
}
