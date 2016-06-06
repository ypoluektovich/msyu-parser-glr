package org.msyu.parser.glr;

import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;

import static org.testng.Assert.assertEquals;

public class FillableComputationTest {

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

		assertEquals(grammar.fillableSymbols, new HashSet<>(Arrays.asList(a, A, B)));
	}

}
