package coho.common.util;

public class MoreLong
{
	public static String toHexString(long l){
		final int nStandardLength = 16;
		String sHex = Long.toHexString(l);
		int nActualLength = sHex.length();
		if (nActualLength < nStandardLength){
			// pad with '0's to the left
			sHex = MoreString.multiply("0", nStandardLength-nActualLength)+sHex;
		}
		return sHex;
	}

}