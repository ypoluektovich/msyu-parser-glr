package org.msyu.parser.glr.test;

import org.msyu.parser.glr.GrammarBuilder;
import org.msyu.parser.glr.GrammarException;
import org.msyu.parser.glr.NonTerminal;
import org.msyu.parser.glr.Terminal;
import org.testng.annotations.Test;

public class InfiniteReduction {

	@Test(expectedExceptions = GrammarException.class)
	public void direct() {
		GrammarBuilder gb = new GrammarBuilder();

		Terminal a = gb.addTerminal("a");

		NonTerminal P = gb.addNonTerminal("P");
		NonTerminal S = gb.addNonTerminal("S");
		NonTerminal G = gb.addNonTerminal("G");

		gb.addProduction(P, a);
		gb.addProduction(P);

		gb.addProduction(S);

		gb.addProduction(G, P);
		gb.addProduction(G, P, G, S);

		gb.build();
	}

	@Test(expectedExceptions = GrammarException.class)
	public void indirect() {
		GrammarBuilder gb = new GrammarBuilder();

		Terminal a = gb.addTerminal("a");

		NonTerminal P = gb.addNonTerminal("P");
		NonTerminal S = gb.addNonTerminal("S");
		NonTerminal I = gb.addNonTerminal("I");
		NonTerminal G = gb.addNonTerminal("G");

		gb.addProduction(P, a);
		gb.addProduction(P);

		gb.addProduction(S);

		gb.addProduction(G, P);
		gb.addProduction(G, P, I, S);

		gb.addProduction(I, G);

		gb.build();
	}

	@Test(expectedExceptions = GrammarException.class)
	public void indirect2() {
		GrammarBuilder gb = new GrammarBuilder();

		Terminal a = gb.addTerminal("a");

		NonTerminal P = gb.addNonTerminal("P");
		NonTerminal S = gb.addNonTerminal("S");
		NonTerminal I1 = gb.addNonTerminal("I1");
		NonTerminal I2 = gb.addNonTerminal("I2");
		NonTerminal G = gb.addNonTerminal("G");

		gb.addProduction(P, a);
		gb.addProduction(P);

		gb.addProduction(S);

		gb.addProduction(G, P);
		gb.addProduction(G, P, I1, S);

		gb.addProduction(I1, I2);

		gb.addProduction(I2, G);

		gb.build();
	}

}
