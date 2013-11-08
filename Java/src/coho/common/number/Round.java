package coho.common.number;

public interface Round {
	public static enum ROUNDMODE{CEIL,FLOOR,NEAR,ZERO};
	public double doubleValue(ROUNDMODE mode);
}
