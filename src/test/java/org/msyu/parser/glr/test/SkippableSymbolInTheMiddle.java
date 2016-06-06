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
import static org.mockito.Mockito.verify;

public class SkippableSymbolInTheMiddle extends ReachTheGoalTestBase {

	GrammarBuilder gb = new GrammarBuilder();

	Terminal prefix = gb.addTerminal("prefix");
	Terminal middle = gb.addTerminal("middle");
	Terminal suffix = gb.addTerminal("suffix");

	NonTerminal skippable = gb.addNonTerminal("Skippable");
	NonTerminal goal = gb.addNonTerminal("Goal");

	{
		gb.addProduction(skippable);
		gb.addProduction(skippable, middle);

		goalProduction = gb.addProduction(goal, prefix, skippable, suffix);
	}

	Grammar grammar = gb.build();
	Sapling sapling = grammar.newSapling(goal);

	@Test
	public void skipped() {
		State state = State.initializeFrom(sapling, callback);
		callback.completeIteration(state);

		state = callback.advance(state, prefix);
		state = callback.advance(state, suffix);

		verify(callback).reduce(any(), eq(goalProduction));
	}

	@Test
	public void filled() {
		State state = State.initializeFrom(sapling, callback);
		callback.completeIteration(state);

		state = callback.advance(state, prefix);
		state = callback.advance(state, middle);
		state = callback.advance(state, suffix);

		verify(callback).reduce(any(), eq(goalProduction));
	}

}
