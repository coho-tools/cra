package coho.common.matrix;

import coho.common.number.CohoNumber;

public interface Reduce {
	CohoNumber first(BasicMatrix args[]);
	CohoNumber middle(CohoNumber partialResult, CohoNumber arg[]);
	CohoNumber last(CohoNumber finalResult);
}
