package org.msyu.parser.glr.grammartest;

import org.msyu.parser.glr.NonTerminal;
import org.msyu.parser.glr.Terminal;
import org.testng.annotations.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

public class SkippableSymbolInTheMiddle extends ReachTheGoalTestBase<Terminal> {

	Terminal prefix = gb.addTerminal("prefix");
	Terminal middle = gb.addTerminal("middle");
	Terminal suffix = gb.addTerminal("suffix");

	NonTerminal skippable = gb.addNonTerminal("Skippable");

	{
		goal = gb.addNonTerminal("Goal");

		gb.addProduction(skippable);
		gb.addProduction(skippable, middle);

		goalProduction = gb.addProduction(goal, prefix, skippable, suffix);

		callback = new LoggingCallback();
	}

	@Test
	public void skipped() {
		state = callback.advance(state, prefix);
		state = callback.advance(state, suffix);

		verify(callback).reduce(any(), eq(goalProduction));
	}

	@Test
	public void filled() {
		state = callback.advance(state, prefix);
		state = callback.advance(state, middle);
		state = callback.advance(state, suffix);

		verify(callback).reduce(any(), eq(goalProduction));
	}

}
