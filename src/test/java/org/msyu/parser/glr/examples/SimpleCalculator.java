package org.msyu.parser.glr.examples;

import org.msyu.parser.glr.ASymbol;
import org.msyu.parser.glr.GlrCallback;
import org.msyu.parser.glr.NonTerminal;
import org.msyu.parser.glr.ProductionHandle;
import org.msyu.parser.glr.Terminal;
import org.msyu.parser.glr.grammartest.ReachTheGoalTestBase;
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
public class SimpleCalculator extends ReachTheGoalTestBase<SimpleCalculator.Token> {

	Terminal num = gb.addTerminal("num");
	Terminal times = gb.addTerminal("*");
	Terminal plus = gb.addTerminal("+");
	Terminal openParen = gb.addTerminal("(");
	Terminal closeParen = gb.addTerminal(")");
	Terminal eof = gb.addTerminal("eof");

	NonTerminal value = gb.addNonTerminal("Value");
	NonTerminal products = gb.addNonTerminal("Products");
	NonTerminal sums = gb.addNonTerminal("Sums");


	{
		goal = gb.addNonTerminal("Expr");
	}

	ProductionHandle valueIsNum = gb.addProduction(value, num);
	ProductionHandle valueIsExpr = gb.addProduction(value, openParen, sums, closeParen);

	ProductionHandle prodTimesValue = gb.addProduction(products, products, times, value);
	ProductionHandle prodIsValue = gb.addProduction(products, value);

	ProductionHandle sumPlusProd = gb.addProduction(sums, sums, plus, products);
	ProductionHandle sumIsProd = gb.addProduction(sums, products);

	{
		goalProduction = gb.addProduction(goal, sums, eof);
	}

	private Integer result;

	final class Token {
		final Terminal terminal;
		final Integer value;

		Token(Integer value) {
			this.terminal = num;
			this.value = value;
		}

		Token(Terminal terminal) {
			this.terminal = terminal;
			this.value = null;
		}
	}

	@BeforeMethod
	@Override
	public void beforeMethod() {
		callback = new GlrCallback<Token>() {
			private final TreeStack<Token> stack = new TreeStack<>();

			@Override
			public Terminal getSymbolOfToken(Token token) {
				return token.terminal;
			}

			private Iterator<Token> emptySymbolsToTokens(List<ASymbol> prependedEmptySymbols) {
				// should never happen in this grammar
				return Collections.nCopies(prependedEmptySymbols.size(), (Token) null).iterator();
			}

			@Override
			public Object shift(Object oldBranch, List<ASymbol> prependedEmptySymbols, Token token) {
				return stack.push(oldBranch, concat(emptySymbolsToTokens(prependedEmptySymbols), singletonIterator(token)));
			}

			@Override
			public Object skip(Object oldBranch, List<ASymbol> emptySymbols) {
				return stack.push(oldBranch, emptySymbolsToTokens(emptySymbols));
			}

			@Override
			public Object reduce(Object oldBranch, ProductionHandle production) {
				List<Token> tokens = new ArrayList<>();
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
				AtomicReference<Token> temp = new AtomicReference<>();
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
		assertEquals(result.intValue(), 46);
	}

}