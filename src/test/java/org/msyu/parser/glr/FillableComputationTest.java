package org.msyu.parser.glr;

import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

public class FillableComputationTest {

	@Test
	public void unendingRecursionIsNotFillable() {
		GrammarBuilder gb = new GrammarBuilder();
		Terminal a = gb.addTerminal("a");
		NonTerminal A = gb.addNonTerminal("A");
		NonTerminal B = gb.addNonTerminal("B");
		gb.addProduction(A, B, a);
		gb.addProduction(B, A);
		Grammar grammar = gb.build();

		assertThat(grammar.fillableSymbols, not(hasItem(A)));
	}

	@Test
	public void test() {
		GrammarBuilder gb = new GrammarBuilder();
		Terminal a = gb.addTerminal("a");
		NonTerminal A = gb.addNonTerminal("A");
		NonTerminal B = gb.addNonTerminal("B");
		gb.addProduction(A, a, a, a, a, a, a, a, a, a, a);
		gb.addProduction(A);
		gb.addProduction(B, A, A);
		Grammar grammar = gb.build();

		assertThat(grammar.fillableSymbols, hasItem(A));
		assertThat(grammar.fillableSymbols, hasItem(B));
	}

}
