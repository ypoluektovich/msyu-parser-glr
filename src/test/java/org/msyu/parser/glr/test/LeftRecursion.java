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

public class LeftRecursion extends ReachTheGoalTestBase {

	GrammarBuilder gb = new GrammarBuilder();

	Terminal prefix = gb.addTerminal("prefix");
	Terminal suffix = gb.addTerminal("suffix");

	NonTerminal recursive = gb.addNonTerminal("Recursive");
	NonTerminal goal = gb.addNonTerminal("Goal");

	{
		gb.addProduction(recursive);
		gb.addProduction(recursive, recursive, prefix);

		goalProduction = gb.addProduction(goal, recursive, suffix);
	}

	Grammar grammar = gb.build();
	Sapling sapling = grammar.newSapling(goal);

	@Test
	public void zero() {
		State state = State.initializeFrom(sapling, callback);
		callback.completeIteration(state);

		state = callback.advance(state, suffix);

		verify(callback).reduce(any(), eq(goalProduction));
	}

	@Test
	public void one() {
		State state = State.initializeFrom(sapling, callback);
		callback.completeIteration(state);

		state = callback.advance(state, prefix);
		state = callback.advance(state, suffix);

		verify(callback).reduce(any(), eq(goalProduction));
	}

	@Test
	public void two() {
		State state = State.initializeFrom(sapling, callback);
		callback.completeIteration(state);

		state = callback.advance(state, prefix);
		state = callback.advance(state, prefix);
		state = callback.advance(state, suffix);

		verify(callback).reduce(any(), eq(goalProduction));
	}

}
