package coho.interp;
/**
 * Some methods to operate on the args.
 * @author chaoyan
 *
 */

public class BasicArgs
{
	public static double double_arg(RCvalue args, int i, String who) throws EvalException{
		if (i > args.size())
			throw new EvalException(who + ":  not enough parameters.");
		Value v = args.value(i);
		if (!(v instanceof DoubleValue))
			throw new EvalException(who + ":  parameter " + i + " must be a double -- got a " + v.getClass().getName());
		return (((DoubleValue) (v)).value());
	}

	public static int int_arg(RCvalue args, int i, String who) throws EvalException{
		return ((int) (Math.round(double_arg(args, i, who))));
	}

	public static RCvalue rc_arg(RCvalue args, int i, String who) throws EvalException{
		if (i > args.size())
			throw new EvalException(who + ":  not enough parameters.");
		Value v = args.value(i);
		if (!(v instanceof RCvalue))
			throw new EvalException(who + ":  parameter " + i + " must be a row or column -- got a " + v.getClass().getName());
		return ((RCvalue) (v));
	}

	public static String string_arg(RCvalue args, int i, String who) throws EvalException{
		if (i > args.size())
			throw new EvalException(who + ":  not enough parameters.");
		Value v = args.value(i);
		if (!(v instanceof StringValue))
			throw new EvalException(who + ":  parameter " + i + " must be a string -- got a " + v.getClass().getName());
		return (((StringValue) (v)).value());
	}
}
