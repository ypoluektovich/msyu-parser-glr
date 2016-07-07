package org.msyu.parser.glr.grammartest;

import org.mockito.ArgumentCaptor;
import org.mockito.MockitoAnnotations;
import org.msyu.javautil.cf.CopyList;
import org.msyu.parser.glr.NonTerminal;
import org.msyu.parser.glr.Terminal;
import org.msyu.parser.glr.UnexpectedTokenException;
import org.msyu.parser.glr.examples.NaiveAstCallback;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class AmbiguousGrammarWithAST extends ReachTheGoalTestBase<Terminal, NaiveAstCallback<Terminal>> {

	private final static class NoEqualsRef {
		final Object ref;

		private NoEqualsRef(Object ref) {
			this.ref = ref;
		}

		static Object unwrap(Object listOfRefs) {
			if (listOfRefs instanceof List) {
				return CopyList.immutable((List) listOfRefs, NoEqualsRef::unwrap);
			} else if (listOfRefs instanceof NoEqualsRef) {
				return ((NoEqualsRef) listOfRefs).ref;
			} else {
				return listOfRefs;
			}
		}
	}

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
	public void run() throws UnexpectedTokenException {
		state = state.advance(prefix, callback);
		state = state.advance(middle, callback);
		state = state.advance(suffix, callback);

		ArgumentCaptor<Object> astCaptor = ArgumentCaptor.forClass(Object.class);
		verify(callback.reductionCallback, times(2)).accept(eq(goalProduction), astCaptor.capture());
		assertThat(
				(List<?>) NoEqualsRef.unwrap(astCaptor.getAllValues()),
				containsInAnyOrder(
						asList(
								goal,
								prefix,
								asList(m1, middle),
								suffix
						),
						asList(
								goal,
								prefix,
								asList(m2, middle),
								suffix
						)
				)
		);
	}

}
