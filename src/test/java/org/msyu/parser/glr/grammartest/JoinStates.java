package org.msyu.parser.glr.grammartest;

import org.mockito.MockitoAnnotations;
import org.msyu.parser.glr.NonTerminal;
import org.msyu.parser.glr.State;
import org.msyu.parser.glr.Terminal;
import org.msyu.parser.glr.UnexpectedTokenException;
import org.msyu.parser.glr.examples.NaiveAstCallback;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class JoinStates extends ReachTheGoalTestBase<Terminal, NaiveAstCallback<Terminal>> {

	Terminal prefix = gb.addTerminal("prefix");
	Terminal m11 = gb.addTerminal("m11");
	Terminal m12 = gb.addTerminal("m12");
	Terminal m2 = gb.addTerminal("m2");
	Terminal suffix = gb.addTerminal("suffix");

	NonTerminal M1 = gb.addNonTerminal("M1");
	NonTerminal M2 = gb.addNonTerminal("M2");
	NonTerminal M = gb.addNonTerminal("M");

	{
		goal = gb.addNonTerminal("Goal");

		gb.addProduction(M1, m11, m12);
		gb.addProduction(M2, m2);

		gb.addProduction(M, M1);
		gb.addProduction(M, M2);

		goalProduction = gb.addProduction(goal, prefix, M, suffix);

		callback = new NaiveAstCallback<Terminal>(null) {
			@Override
			protected Object getStackableToken(Terminal token) {
				return new NoEqualsRef(token);
			}

			@Override
			public Terminal getSymbolOfToken(Terminal token) {
				return token;
			}
		};
	}

	@BeforeMethod
	@Override
	public void beforeMethod() {
		super.beforeMethod();
		MockitoAnnotations.initMocks(callback);
	}

	@Test
	public void oneBranch() throws UnexpectedTokenException {
		state = state.advance(prefix, callback);
		State state1 = state.advance(m11, callback);
		State state2 = state.advance(m2, callback);
		state = State.join(asList(state1, state2));
		state = state.advance(suffix, callback);

		verify(callback.reductionCallback, times(1)).accept(eq(goalProduction), any());
	}

	@Test
	public void twoBranches() throws UnexpectedTokenException {
		state = state.advance(prefix, callback);
		State state1 = state.advance(m11, callback);
		state1 = state1.advance(m12, callback);
		State state2 = state.advance(m2, callback);
		state = State.join(asList(state1, state2));
		state = state.advance(suffix, callback);

		verify(callback.reductionCallback, times(2)).accept(eq(goalProduction), any());
	}

}
