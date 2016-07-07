package org.msyu.parser.glr.examples;

import org.msyu.parser.glr.GlrCallback;
import org.msyu.parser.glr.Terminal;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;

@Test(singleThreaded = true)
public class NaiveAst extends SimpleCalculatorBase<SimpleCalculatorBase.Token, GlrCallback<SimpleCalculatorBase.Token>> {

	private Object result;

	@BeforeMethod
	@Override
	public void beforeMethod() {
		callback = new NaiveAstCallback<SimpleCalculatorBase.Token>(
				(production, stuffToStack) -> {
					if (production == goalProduction) {
						result = stuffToStack;
					}
				}
		) {
			@Override
			public Terminal getSymbolOfToken(SimpleCalculatorBase.Token token) {
				return token.terminal;
			}

			@Override
			protected Object getStackableToken(SimpleCalculatorBase.Token token) {
				return token.terminal == num ? token.value : token.terminal;
			}
		};
		super.beforeMethod();
	}

	@Test
	public void example() {
		List<Token> tokens = asList(
				new Token(2),
				new Token(times),
				new Token(openParen),
				new Token(3),
				new Token(plus),
				new Token(4),
				new Token(times),
				new Token(5),
				new Token(closeParen),
				new Token(eof)
		);

		for (Token token : tokens) {
			state = state.advance(token, callback);
		}

		verify(callback).reduce(any(), eq(goalProduction));
		assertEquals(
				result,
				asList(
						goal,
						asList(
								products,
								asList(value, 2),
								times,
								asList(
										value,
										openParen,
										asList(
												sums,
												asList(value, 3),
												plus,
												asList(
														products,
														asList(value, 4),
														times,
														asList(value, 5)
												)
										),
										closeParen
								)
						),
						eof
				)
		);
	}

}
