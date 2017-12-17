package org.msyu.parser.glr.examples;

import org.mockito.MockitoAnnotations;
import org.msyu.parser.glr.Production;
import org.msyu.parser.glr.State;
import org.msyu.parser.glr.Terminal;
import org.msyu.parser.glr.UnexpectedTokensException;
import org.msyu.parser.glr.grammartest.NoEqualsRef;
import org.msyu.parser.glr.grammartest.ReachTheGoalTestBase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class MultiStateExamples extends ReachTheGoalTestBase<Terminal, NaiveAstCallback<Terminal>> {

	Terminal aaa = gb.addTerminal("aaa");
	Terminal aa = gb.addTerminal("aa");
	Terminal b = gb.addTerminal("b");

	{
		goal = gb.addNonTerminal("Goal");
	}

	Production p_aaa = gb.addProduction(goal, aaa);
	Production p_aa_b = gb.addProduction(goal, aa, b);
	Production p_aa_aa = gb.addProduction(goal, aa, aa);

	{
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
	public void example1() throws UnexpectedTokensException {
		State state = State.initializeFrom(sapling, 0);
		state = state.advance(
				Collections.singletonMap(aa, 0),
				callback,
				2,
				asList(0, 2)
		);
		state = state.advance(
				Collections.singletonMap(b, 2),
				callback,
				3,
				singleton(3)
		);

		verify(callback.reductionCallback, times(1)).accept(eq(p_aa_b), any());
	}

	@Test
	public void dropWithoutAdvance() throws UnexpectedTokensException {
		State state = State.initializeFrom(sapling, 0);
		state = state.advance(
				Collections.singletonMap(aa, 0),
				callback,
				2,
				asList(0, 2)
		);
		state = state.advance(
				Collections.emptyMap(),
				callback,
				3,
				singleton(2)
		);
		state = state.advance(
				Collections.singletonMap(aa, 2),
				callback,
				4,
				singleton(4)
		);

		verify(callback.reductionCallback, times(1)).accept(eq(p_aa_aa), any());
	}

	@Test(expectedExceptions = UnexpectedTokensException.class)
	public void exception() throws UnexpectedTokensException {
		State state = State.initializeFrom(sapling, 0);
		state = state.advance(
				Collections.singletonMap(aa, 0),
				callback,
				2,
				asList(0, 2)
		);
		state = state.advance(
				Collections.emptyMap(),
				callback,
				3,
				singleton(2)
		);
		state = state.advance(
				Collections.singletonMap(aaa, 2),
				callback,
				5,
				singleton(5)
		);
	}

}
