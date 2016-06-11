package org.msyu.parser.glr.grammartest;

import org.msyu.parser.glr.NonTerminal;
import org.msyu.parser.glr.Terminal;
import org.testng.annotations.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class SkippableSymbolAtTheEnd extends ReachTheGoalTestBase {

	Terminal prefix = gb.addTerminal("prefix");
	Terminal suffix = gb.addTerminal("suffix");

	NonTerminal skippable = gb.addNonTerminal("Skippable");

	{
		goal = gb.addNonTerminal("Goal");

		gb.addProduction(skippable);
		gb.addProduction(skippable, suffix);

		goalProduction = gb.addProduction(goal, prefix, skippable);
	}

	@Test
	public void skipped() {
		state = callback.advance(state, prefix);

		verify(callback).reduce(any(), eq(goalProduction));
	}

	@Test
	public void filled() {
		state = callback.advance(state, prefix);
		state = callback.advance(state, suffix);

		verify(callback, times(2)).reduce(any(), eq(goalProduction));
	}

}
