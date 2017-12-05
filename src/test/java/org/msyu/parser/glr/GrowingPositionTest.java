package org.msyu.parser.glr;

import org.testng.annotations.Test;

import java.util.HashSet;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonMap;
import static org.testng.Assert.assertEquals;

public class GrowingPositionTest {

	GrammarBuilder gb = new GrammarBuilder();
	Terminal a = gb.addTerminal("a");
	Terminal b = gb.addTerminal("b");
	NonTerminal X = gb.addNonTerminal("X");
	Production Xaa = gb.addProduction(X, a, a);
	Production Xbb = gb.addProduction(X, b);
	Grammar g = gb.build();
	Sapling sapling = g.newSapling(X);

	@Test
	public void initialSet() {
		Object initialPosition = new Object();
		State s0 = State.initializeFrom(sapling, initialPosition);
		assertEquals(s0.getGrowingPositions(), singleton(initialPosition));
	}

	@Test
	public void retainGrowingPositionAfterOneAdvance() throws UnexpectedTokensException {
		NodeAstCallback<Terminal> callback = new NodeAstCallback<Terminal>() {
			@Override
			public Terminal getSymbolOfToken(Terminal token) {
				return token;
			}

			@Override
			protected Object getStackableToken(Terminal token) {
				return token;
			}
		};

		State s0 = State.initializeFrom(sapling, 0);
		State s1 = s0.advance(singletonMap(a, 0), callback, 1, asList(0, 1));
		assertEquals(s1.getGrowingPositions(), new HashSet<>(asList(0, 1)));
	}

	@Test
	public void pruningOfOneBranchLeavesOthersIntact() throws UnexpectedTokensException {
		NodeAstCallback<Terminal> callback = new NodeAstCallback<Terminal>() {
			@Override
			public Terminal getSymbolOfToken(Terminal token) {
				return token;
			}

			@Override
			protected Object getStackableToken(Terminal token) {
				return token;
			}
		};

		State s0 = State.initializeFrom(sapling, 0);
		State s1 = s0.advance(singletonMap(a, 0), callback, 1, asList(0, 1));
		State s2 = s1.advance(singletonMap(b, 1), callback, 2, asList(0, 2));
		assertEquals(s2.getGrowingPositions(), singleton(0));
	}

	@Test
	public void exhaustionOfOneBranchLeavesOthersIntact() throws UnexpectedTokensException {
		NodeAstCallback<Terminal> callback = new NodeAstCallback<Terminal>() {
			@Override
			public Terminal getSymbolOfToken(Terminal token) {
				return token;
			}

			@Override
			protected Object getStackableToken(Terminal token) {
				return token;
			}
		};

		State s0 = State.initializeFrom(sapling, 0);
		State s1 = s0.advance(singletonMap(a, 0), callback, 1, asList(0, 1));
		State s2 = s1.advance(singletonMap(a, 1), callback, 2, asList(0, 2)); // difference from previous is token
		assertEquals(s2.getGrowingPositions(), singleton(0));
		// despite 2 still being able to grow in lexer, there's no future for in in parser,
	}

}
