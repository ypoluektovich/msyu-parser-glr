package org.msyu.parser.glr.grammartest;

import org.msyu.parser.glr.NonTerminal;
import org.msyu.parser.glr.Terminal;
import org.msyu.parser.glr.UnexpectedTokenException;
import org.testng.annotations.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

public class NonStartBlindReduction extends ReachTheGoalTestBase<Terminal, LoggingCallback> {

	Terminal a = gb.addTerminal("a");

	NonTerminal H = gb.addNonTerminal("H");
	NonTerminal T11 = gb.addNonTerminal("T11");
	NonTerminal T12 = gb.addNonTerminal("T12");
	NonTerminal T21 = gb.addNonTerminal("T21");
	NonTerminal T22 = gb.addNonTerminal("T22");

	{
		goal = gb.addNonTerminal("Goal");

		gb.addProduction(H, a);
		gb.addProduction(T11, T12);
		gb.addProduction(T12, T21, T22);
		gb.addProduction(T21);
		gb.addProduction(T22, a);

		goalProduction = gb.addProduction(goal, H, T11);

		callback = new LoggingCallback();
	}

	@Test
	public void run() throws UnexpectedTokenException {
		state = callback.advance(state, a);
		state = callback.advance(state, a);

		verify(callback).reduce(any(), eq(goalProduction), any());
	}

}
