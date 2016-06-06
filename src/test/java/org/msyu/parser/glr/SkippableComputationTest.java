package org.msyu.parser.glr;

import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

public class SkippableComputationTest {

	@Test
	public void unendingRecursionIsNotSkippable() {
		GrammarBuilder gb = new GrammarBuilder();
		Terminal a = gb.addTerminal("a");
		NonTerminal A = gb.addNonTerminal("A");
		NonTerminal B = gb.addNonTerminal("B");
		gb.addProduction(A, B);
		gb.addProduction(B, A);
		Grammar grammar = gb.build();

		assertThat(grammar.skippableSymbols, not(hasItem(A)));
		assertThat(grammar.skippableSymbols, not(hasItem(B)));
	}

}
