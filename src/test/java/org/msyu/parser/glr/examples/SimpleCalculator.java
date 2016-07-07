package org.msyu.parser.glr.examples;

import org.msyu.parser.glr.ASymbol;
import org.msyu.parser.glr.GlrCallback;
import org.msyu.parser.glr.ProductionHandle;
import org.msyu.parser.glr.Terminal;
import org.msyu.parser.glr.UnexpectedTokenException;
import org.msyu.parser.treestack.TreeStack;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.msyu.javautil.cf.Iterators.concat;
import static org.msyu.javautil.cf.Iterators.singletonIterator;
import static org.testng.Assert.assertEquals;

@Test(singleThreaded = true)
public class SimpleCalculator extends SimpleCalculatorBase<SimpleCalculatorBase.Token, GlrCallback<SimpleCalculatorBase.Token>> {

	private Integer result;

	@BeforeMethod
	@Override
	public void beforeMethod() {
		callback = new GlrCallback<SimpleCalculatorBase.Token>() {
			private final TreeStack<SimpleCalculatorBase.Token> stack = new TreeStack<>();

			@Override
			public Terminal getSymbolOfToken(SimpleCalculatorBase.Token token) {
				return token.terminal;
			}

			private Iterator<SimpleCalculatorBase.Token> emptySymbolsToTokens(List<ASymbol> prependedEmptySymbols) {
				assert prependedEmptySymbols.isEmpty() : "why are there empty symbols?";
				return Collections.nCopies(prependedEmptySymbols.size(), (SimpleCalculatorBase.Token) null).iterator();
			}

			@Override
			public Object shift(Object oldBranch, List<ASymbol> prependedEmptySymbols, SimpleCalculatorBase.Token token) {
				return stack.push(oldBranch, concat(emptySymbolsToTokens(prependedEmptySymbols), singletonIterator(token)));
			}

			@Override
			public Object skip(Object oldBranch, List<ASymbol> emptySymbols) {
				return stack.push(oldBranch, emptySymbolsToTokens(emptySymbols));
			}

			@Override
			public Object reduce(Object oldBranch, ProductionHandle production) {
				List<SimpleCalculatorBase.Token> tokens = new ArrayList<>();
				Object popped = stack.pop(oldBranch, production.getRHS().size(), tok -> tokens.add(0, tok));
				Integer result;
				if (production == valueIsNum || production == prodIsValue || production == sumIsProd) {
					result = tokens.get(0).value;
				} else if (production == valueIsExpr) {
					result = tokens.get(1).value;
				} else if (production == prodTimesValue) {
					result = tokens.get(0).value * tokens.get(2).value;
				} else if (production == sumPlusProd) {
					result = tokens.get(0).value + tokens.get(2).value;
				} else if (production == goalProduction) {
					result = SimpleCalculator.this.result = tokens.get(0).value;
				} else {
					throw new IllegalStateException("unknown production");
				}
				return stack.push(popped, singletonIterator(new Token(result)));
			}

			@Override
			public Object insert(Object oldBranch, List<ASymbol> emptySymbols) {
				if (emptySymbols.isEmpty()) {
					return oldBranch;
				}
				AtomicReference<SimpleCalculatorBase.Token> temp = new AtomicReference<>();
				Object popped = stack.pop(oldBranch, 1, temp::set);
				return stack.push(popped, concat(emptySymbolsToTokens(emptySymbols), singletonIterator(temp.get())));
			}
		};
		super.beforeMethod();
	}

	@Test
	public void example() throws UnexpectedTokenException {
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
		assertEquals(result.intValue(), 46);
	}

}
