package org.msyu.parser.glr.examples;

import org.msyu.parser.glr.GlrCallback;
import org.msyu.parser.glr.NonTerminal;
import org.msyu.parser.glr.Production;
import org.msyu.parser.glr.Terminal;
import org.msyu.parser.glr.grammartest.ReachTheGoalTestBase;

public abstract class SimpleCalculatorBase<T, C extends GlrCallback<T>> extends ReachTheGoalTestBase<T, C> {

	protected Terminal num = gb.addTerminal("num");
	protected Terminal times = gb.addTerminal("*");
	protected Terminal plus = gb.addTerminal("+");
	protected Terminal openParen = gb.addTerminal("(");
	protected Terminal closeParen = gb.addTerminal(")");
	protected Terminal eof = gb.addTerminal("eof");

	protected NonTerminal value = gb.addNonTerminal("Value");
	protected NonTerminal products = gb.addNonTerminal("Products");
	protected NonTerminal sums = gb.addNonTerminal("Sums");
	{
		goal = gb.addNonTerminal("Expr");
	}

	protected Production valueIsNum = gb.addProduction(value, num);
	protected Production valueIsExpr = gb.addProduction(value, openParen, sums, closeParen);
	protected Production prodTimesValue = gb.addProduction(products, products, times, value);
	protected Production prodIsValue = gb.addProduction(products, value);
	protected Production sumPlusProd = gb.addProduction(sums, sums, plus, products);
	protected Production sumIsProd = gb.addProduction(sums, products);
	{
		goalProduction = gb.addProduction(goal, sums, eof);
	}

	public final class Token {
		public final Terminal terminal;
		public final Integer value;

		public Token(Integer value) {
			this.terminal = num;
			this.value = value;
		}

		public Token(Terminal terminal) {
			this.terminal = terminal;
			this.value = null;
		}
	}

}
