package org.msyu.parser.glr;

import org.testng.annotations.Test;

public class NoDuplicateProductionsTest {

	@Test(expectedExceptions = GrammarException.class)
	public void duplicateProductionsAreNotAllowed() {
		// given
		GrammarBuilder gb = new GrammarBuilder();
		Terminal t = gb.addTerminal("t");
		NonTerminal N = gb.addNonTerminal("N");

		Production p = gb.addProduction(N, t);

		// when
		gb.addProduction(p.lhs, p.rhs);

		// then must throw
	}

}
