package org.msyu.parser.glr.test;

import org.msyu.parser.glr.Grammar;
import org.msyu.parser.glr.GrammarBuilder;
import org.msyu.parser.glr.NonTerminal;
import org.msyu.parser.glr.Sapling;
import org.msyu.parser.glr.State;
import org.msyu.parser.glr.Terminal;
import org.testng.annotations.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class AmbiguousGrammar extends ReachTheGoalTestBase {

	GrammarBuilder gb = new GrammarBuilder();

	Terminal prefix = gb.addTerminal("prefix");
	Terminal middle = gb.addTerminal("middle");
	Terminal suffix = gb.addTerminal("suffix");

	NonTerminal m1 = gb.addNonTerminal("M1");
	NonTerminal m2 = gb.addNonTerminal("M2");
	NonTerminal m = gb.addNonTerminal("M");
	NonTerminal goal = gb.addNonTerminal("Goal");

	{
		gb.addProduction(m1, middle);

		gb.addProduction(m2, middle);

		gb.addProduction(m, m1);
		gb.addProduction(m, m2);

		goalProduction = gb.addProduction(goal, prefix, m, suffix);
	}

	Grammar grammar = gb.build();
	Sapling sapling = grammar.newSapling(goal);

	@Test
	public void run() {
		State state = State.initializeFrom(sapling, callback);
		callback.completeIteration(state);

		state = callback.advance(state, prefix);
		state = callback.advance(state, middle);
		state = callback.advance(state, suffix);

		verify(callback, times(2)).reduce(any(), eq(goalProduction));
	}

}
