package org.msyu.parser.glr.test;

import org.msyu.parser.glr.NonTerminal;
import org.msyu.parser.glr.State;
import org.msyu.parser.glr.Terminal;
import org.testng.annotations.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

public class LeftRecursion extends ReachTheGoalTestBase {

	Terminal prefix = gb.addTerminal("prefix");
	Terminal suffix = gb.addTerminal("suffix");

	NonTerminal recursive = gb.addNonTerminal("Recursive");

	{
		goal = gb.addNonTerminal("Goal");

		gb.addProduction(recursive);
		gb.addProduction(recursive, recursive, prefix);

		goalProduction = gb.addProduction(goal, recursive, suffix);
	}

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
