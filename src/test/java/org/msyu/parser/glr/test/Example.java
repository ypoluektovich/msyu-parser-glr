package org.msyu.parser.glr.test;

import org.msyu.parser.glr.Grammar;
import org.msyu.parser.glr.GrammarBuilder;
import org.msyu.parser.glr.NonTerminal;
import org.msyu.parser.glr.Sapling;
import org.msyu.parser.glr.State;
import org.msyu.parser.glr.Terminal;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Example {

	@Test
	public void example() {
		GrammarBuilder gb = new GrammarBuilder();

		Terminal id = gb.addTerminal("id");
		Terminal num = gb.addTerminal("num");
		Terminal times = gb.addTerminal("*");
		Terminal plus = gb.addTerminal("+");
		Terminal eof = gb.addTerminal("eof");

		NonTerminal value = gb.addNonTerminal("Value");
		gb.addProduction(value, id);
		gb.addProduction(value, num);

		NonTerminal products = gb.addNonTerminal("Products");
		gb.addProduction(products, products, times, value);
		gb.addProduction(products, value);

		NonTerminal sums = gb.addNonTerminal("Sums");
		gb.addProduction(sums, sums, plus, products);
		gb.addProduction(sums, products);

		NonTerminal goal = gb.addNonTerminal("Goal");
		gb.addProduction(goal, sums, eof);

		Grammar grammar = gb.build();

		Sapling sapling = grammar.newSapling(goal);

		State state = State.initializeFrom(sapling);
		state = state.advance(id);
		state = state.advance(times);
		state = state.advance(num);
		state = state.advance(plus);
		state = state.advance(num);
		state = state.advance(eof);

		Assert.assertTrue(state.completedGoals.contains(goal));
	}

}
