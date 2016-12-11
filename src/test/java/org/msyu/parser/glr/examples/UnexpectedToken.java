package org.msyu.parser.glr.examples;

import org.msyu.parser.glr.GrammarBuilder;
import org.msyu.parser.glr.NonTerminal;
import org.msyu.parser.glr.ScannerlessState;
import org.msyu.parser.glr.SingleGrammarTestBase;
import org.msyu.parser.glr.Terminal;
import org.msyu.parser.glr.UnexpectedTokenException;
import org.msyu.parser.glr.grammartest.LoggingCallback;
import org.testng.annotations.Test;

import java.util.Collections;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

public class UnexpectedToken extends SingleGrammarTestBase {

	Terminal a;
	Terminal b;

	NonTerminal A;
	NonTerminal B;

	LoggingCallback callback;

	{
		GrammarBuilder gb = new GrammarBuilder();

		a = gb.addTerminal("a");
		b = gb.addTerminal("b");

		A = gb.addNonTerminal("A");
		B = gb.addNonTerminal("B");

		gb.addProduction(A, a, a);
		gb.addProduction(B, b, b);

		grammar = gb.build();
		callback = new LoggingCallback();
	}

	@Test(expectedExceptions = UnexpectedTokenException.class)
	public void start() throws UnexpectedTokenException {
		ScannerlessState state = ScannerlessState.initializeFrom(grammar.newSapling(A));

		state = advanceAndExpectFailure(state);
	}

	@Test(expectedExceptions = UnexpectedTokenException.class)
	public void middle() throws UnexpectedTokenException {
		ScannerlessState state = ScannerlessState.initializeFrom(grammar.newSapling(A, B));

		try {
			state = callback.advance(state, a);
		} catch (UnexpectedTokenException e) {
			fail("should not have died here", e);
			throw e;
		}
		state = advanceAndExpectFailure(state);
	}

	private ScannerlessState advanceAndExpectFailure(ScannerlessState state) throws UnexpectedTokenException {
		try {
			return callback.advance(state, b);
		} catch (UnexpectedTokenException e) {
			assertEquals(e.getExpected(), Collections.singleton(a));
			assertEquals(e.getActual(), b);
			assertEquals(e.getToken(), b);
			throw e;
		}
	}

}
