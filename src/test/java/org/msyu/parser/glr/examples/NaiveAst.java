package org.msyu.parser.glr.examples;

import org.msyu.parser.glr.ASymbol;
import org.msyu.parser.glr.GlrCallback;
import org.msyu.parser.glr.ProductionHandle;
import org.msyu.parser.glr.Terminal;
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
public class NaiveAst extends SimpleCalculatorBase<SimpleCalculatorBase.Token, GlrCallback<SimpleCalculatorBase.Token>> {

	private Object result;

	@BeforeMethod
	@Override
	public void beforeMethod() {
		callback = new GlrCallback<SimpleCalculatorBase.Token>() {
			private final TreeStack<Object> stack = new TreeStack<>();

			@Override
			public Terminal getSymbolOfToken(SimpleCalculatorBase.Token token) {
				return token.terminal;
			}

			private Iterator<Object> emptySymbolsToTokens(List<ASymbol> prependedEmptySymbols) {
				assert prependedEmptySymbols.isEmpty() : "why are there empty symbols?";
				return Collections.nCopies(prependedEmptySymbols.size(), null).iterator();
			}

			@Override
			public Object shift(Object oldBranch, List<ASymbol> prependedEmptySymbols, SimpleCalculatorBase.Token token) {
				return stack.push(
						oldBranch,
						concat(
								emptySymbolsToTokens(prependedEmptySymbols),
								singletonIterator(token.terminal == num ? token.value : token.terminal)
						)
				);
			}

			@Override
			public Object skip(Object oldBranch, List<ASymbol> emptySymbols) {
				return stack.push(oldBranch, emptySymbolsToTokens(emptySymbols));
			}

			@Override
			public Object reduce(Object oldBranch, ProductionHandle production) {
				List<Object> stuff = new ArrayList<>();
				Object popped = stack.pop(oldBranch, production.getRHS().size(), thing -> stuff.add(0, thing));
				if (production == goalProduction) {
					result = stuff;
				}
				return stack.push(popped, singletonIterator(stuff.size() == 1 ? stuff.get(0) : stuff));
			}

			@Override
			public Object insert(Object oldBranch, List<ASymbol> emptySymbols) {
				if (emptySymbols.isEmpty()) {
					return oldBranch;
				}
				AtomicReference<Object> temp = new AtomicReference<>();
				Object popped = stack.pop(oldBranch, 1, temp::set);
				return stack.push(popped, concat(emptySymbolsToTokens(emptySymbols), singletonIterator(temp.get())));
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
						asList(
								2,
								times,
								asList(
										openParen,
										asList(
												3,
												plus,
												asList(
														4,
														times,
														5
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
