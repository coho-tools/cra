package coho.interp;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
/**
 * This class defines a series of factories. It's for the evaluation process.
 * @author chaoyan
 *
 */
public class ValueFactory  {
	private static Hashtable<Class, ValueCreate> factories = null;
	public static void init(Enumeration f) { addFactory(f); }
	//set a series of factories
	public static void init() {
		factories = new Hashtable<Class, ValueCreate>();
		init(new Enumeration() {
			ValueCreate f[] = new ValueCreate[] {
					DoubleIntervalValue.factory(),	//added for double interval	  
					DoubleValue.factory(),
					MatrixValue.factory(),
					RangeValue.factory(),
					RCvalue.factory(),
					StringValue.factory(),
					VoidValue.factory()
			};
			int i = 0;
			public boolean hasMoreElements() { return(i < f.length); }
			public Object nextElement() { return(f[i++]); }
		});
		addFactory(PolygonValue.factory());	//added for polygon value?
	}

	public static void noDefaultFactories() {
		factories = new Hashtable<Class, ValueCreate>();
	}

	// find approperate value factory based on val.
	public static Value create(Object val, Object args)
	throws NoSuchElementException, EvalException {
		if(factories == null) init();
		Class c;
		if(val instanceof Class) c = (Class)(val);
		else c = val.getClass();
		//System.err.println(c.toString());
		ValueCreate f = (ValueCreate)(factories.get(c));
		if(f == null) {
			f = (new TryHarder(c)).match();
			if(f == null)
				throw new NoSuchElementException(
						"Coho.geom.interp.ValueFactory.create: no factory for class " +
						val.getClass().getName());
		}
		return(f.create(val, args));
	}

	public static Value create(Object val)
	throws NoSuchElementException, EvalException
	{ return(create(val, new Object[0])); }

	/**
	 * This class is used to find the best value create factory for each object.
	 * @author chaoyan
	 *
	 */
	protected static class TryHarder {
		private LinkedList pairs;
		private Class c;

		public TryHarder(Class _c) {
			c = _c;
			pairs = new LinkedList();
			grind(c);
		}
		// find the factory from the pair.The pair should only contain one
		// valueCreate for each class.
		public ValueCreate match() {
			if(pairs.size() == 1) return(((Pair)(pairs.get(0))).f());
			else if(pairs.size() == 0) {
				throw new IllegalArgumentException(
						"ValueFactory.create(): no factory for -- " + c.getName());
			} else {
				StringBuffer buf = new StringBuffer(
						"ValueFactory.create(val, args): ambiguous val -- ");
				buf.append(c.getName());
				buf.append("matches ");
				Iterator i = pairs.iterator();
				boolean first = true;
				while(i.hasNext()) {
					Pair p = (Pair)(i.next());
					if(first) first = false;
					else if(i.hasNext()) buf.append(", ");
					else buf.append(" and ");
					buf.append(p.c().getName());
				}
				buf.append(".");
				throw new IllegalArgumentException(buf.toString());
			}
		}

		protected void grind(Class c) {
			if(c != null) {
				check(c);
				Class[] x = c.getInterfaces();
				for(int i = 0; i < x.length; i++) check(x[i]);
				grind(c.getSuperclass());
			}
		}


		protected void check(Class c) {
			ValueCreate f = (ValueCreate)(factories.get(c));
			if(f != null) {
				Pair p = new Pair(f, c);
				Iterator i = pairs.iterator();
				while(i.hasNext()) {
					Pair p0 = (Pair)i.next();
					if(p0.c().isAssignableFrom(c)) i.remove(); // c is more specific
					else if(c.isAssignableFrom(p0.c())) return;  // p0 is more specific
				}
				pairs.add(p);
			}
		}

		//the pair of class and its corresponding factory    
		protected class Pair {
			private ValueCreate f;  public ValueCreate f() { return(f); }
			public Class c;         public Class c() { return(c); }
			public Pair(ValueCreate _f, Class _c) { f = _f; c = _c; }
		}
	}

	// add value factory to the hash table.
	public static void addFactory(ValueCreate f)
	throws IllegalArgumentException {
		if(factories == null) init();
		Class c;
		try { c = f.foo().getClass().getField("x").getType(); }
		catch (NoSuchFieldException e) {
			throw new IllegalArgumentException(
					"ValueFactory.addFactory:  the Object returned by f.foo() does not" +
			" have a field named \"x\" as required.");
		}
		ValueCreate f0 = (ValueCreate)(factories.get(c));
		if((f0 != null) && (f0 != f))
			throw new IllegalArgumentException(
					"Coho.geom.interp.ValueFactory:  inconsistent factories for " +
					c.getName());
		factories.put(c, f);
	}

	public static void addFactory(ValueCreate[] a) {
		for(int i = 0; i < a.length; i++) addFactory(a[i]);
	}

	public static void addFactory(Enumeration e) {
		while(e.hasMoreElements()) {
			Object obj = e.nextElement();
			if(obj instanceof ValueCreate) addFactory((ValueCreate)(obj));
			else addFactory((ValueCreate[])(obj));
		}
	}


	// the following methods effect automatic argument conversions for create()
	//   If val is of type byte, Byte, int, Integer, long, short, or Short,
	//     it gets converted to Long.
	//   If val is of type double, float or Float, it gets converted to Double.

	public static Value create(boolean b, Object args)
	throws NoSuchElementException, EvalException
	{ return(create(new Boolean(b), args)); }
	public static Value create(boolean b)
	throws NoSuchElementException, EvalException
	{ return(create(new Boolean(b))); }

	public static Value create(Byte b, Object args)
	throws NoSuchElementException, EvalException
	{ return(create(new Long(b.longValue()), args)); }
	public static Value create(Byte b) throws NoSuchElementException, EvalException
	{ return(create(new Long(b.longValue()))); }

	public static Value create(char c, Object args)
	throws NoSuchElementException, EvalException
	{ return(create(new Character(c), args)); }
	public static Value create(char c) throws NoSuchElementException, EvalException
	{ return(create(new Character(c))); }

	public static Value create(double d, Object args)
	throws NoSuchElementException, EvalException
	{ return(create(new Double(d), args)); }
	public static Value create(double d)
	throws NoSuchElementException, EvalException
	{ return(create(new Double(d))); }

	public static Value create(Float f, Object args)
	throws NoSuchElementException, EvalException
	{ return(create(new Double(f.doubleValue()), args)); }
	public static Value create(Float f)
	throws NoSuchElementException, EvalException
	{ return(create(new Double(f.doubleValue()))); }

	public static Value create(Integer n, Object args)
	throws NoSuchElementException, EvalException
	{ return(create(new Long(n.longValue()), args)); }
	public static Value create(Integer n)
	throws NoSuchElementException, EvalException
	{ return(create(new Long(n.longValue()))); }

	public static Value create(long n, Object args)
	throws NoSuchElementException, EvalException
	{ return(create(new Long(n), args)); }
	public static Value create(long n)
	throws NoSuchElementException, EvalException
	{ return(create(new Long(n))); }

	public static Value create(Short s, Object args)
	throws NoSuchElementException, EvalException
	{ return(create(new Long(s.longValue()), args)); }
	public static Value create(Short s)
	throws NoSuchElementException, EvalException
	{ return(create(new Long(s.longValue()))); }


}
