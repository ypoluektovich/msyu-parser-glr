package org.msyu.parser.glr.grammartest;

import org.msyu.parser.glr.NonTerminal;
import org.msyu.parser.glr.Terminal;
import org.testng.annotations.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class AmbiguousGrammar extends ReachTheGoalTestBase<Terminal> {

	Terminal prefix = gb.addTerminal("prefix");
	Terminal middle = gb.addTerminal("middle");
	Terminal suffix = gb.addTerminal("suffix");

	NonTerminal m1 = gb.addNonTerminal("M1");
	NonTerminal m2 = gb.addNonTerminal("M2");
	NonTerminal m = gb.addNonTerminal("M");

	{
		goal = gb.addNonTerminal("Goal");

		gb.addProduction(m1, middle);

		gb.addProduction(m2, middle);

		gb.addProduction(m, m1);
		gb.addProduction(m, m2);

		goalProduction = gb.addProduction(goal, prefix, m, suffix);

		callback = new LoggingCallback();
	}

	@Test
	public void run() {
		state = state.advance(prefix, callback);
		state = state.advance(middle, callback);
		state = state.advance(suffix, callback);

		verify(callback, times(2)).reduce(any(), eq(goalProduction));
	}

}
