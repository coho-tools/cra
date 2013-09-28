/*
 * Created on 25-Jun-2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package coho.interp;

import java.io.Reader;
import java_cup.runtime.Symbol;

/**
 * @author Mark Greenstreet (mrg@cs.ubc.ca)
 *
 *The interface of Parser
 * 
 */
public interface Parse {
	Symbol parse() throws Exception;
	PTfactory pf();
	SymbolName sname();
	Parse create(Reader rd);
}