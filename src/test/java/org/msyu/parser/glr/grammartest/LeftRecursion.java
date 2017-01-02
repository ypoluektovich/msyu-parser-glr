package org.msyu.parser.glr.grammartest;

import org.msyu.parser.glr.NonTerminal;
import org.msyu.parser.glr.Terminal;
import org.msyu.parser.glr.UnexpectedTokenException;
import org.testng.annotations.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

public class LeftRecursion extends ReachTheGoalTestBase<Terminal, LoggingCallback> {

	Terminal prefix = gb.addTerminal("prefix");
	Terminal suffix = gb.addTerminal("suffix");

	NonTerminal recursive = gb.addNonTerminal("Recursive");

	{
		goal = gb.addNonTerminal("Goal");

		gb.addProduction(recursive);
		gb.addProduction(recursive, recursive, prefix);

		goalProduction = gb.addProduction(goal, recursive, suffix);

		callback = new LoggingCallback();
	}

	@Test
	public void zero() throws UnexpectedTokenException {
		state = callback.advance(state, suffix);

		verify(callback).reduce(any(), eq(goalProduction), any());
	}

	@Test
	public void one() throws UnexpectedTokenException {
		state = callback.advance(state, prefix);
		state = callback.advance(state, suffix);

		verify(callback).reduce(any(), eq(goalProduction), any());
	}

	@Test
	public void two() throws UnexpectedTokenException {
		state = callback.advance(state, prefix);
		state = callback.advance(state, prefix);
		state = callback.advance(state, suffix);

		verify(callback).reduce(any(), eq(goalProduction), any());
	}

}
