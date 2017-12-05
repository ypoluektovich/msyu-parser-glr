package org.msyu.parser.glr.examples;

import org.msyu.parser.glr.Grammar;
import org.msyu.parser.glr.GrammarBuilder;
import org.msyu.parser.glr.Node;
import org.msyu.parser.glr.NodeAstCallback;
import org.msyu.parser.glr.NonTerminal;
import org.msyu.parser.glr.Production;
import org.msyu.parser.glr.Ref;
import org.msyu.parser.glr.Sapling;
import org.msyu.parser.glr.State;
import org.msyu.parser.glr.Terminal;
import org.msyu.parser.glr.UnexpectedTokensException;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonMap;
import static org.testng.Assert.assertEquals;

/**
 * These tests are inspired by the same problem: parsing a string with escape sequences in it.
 */
public class Culling {

	GrammarBuilder gb = new GrammarBuilder();
	Terminal c = gb.addTerminal("c"); // string of normal characters
	Terminal e = gb.addTerminal("e"); // one escape sequence
	NonTerminal C = gb.addNonTerminal("C"); // character type choice
	Production C_c = gb.addProduction(C, c);
	Production C_e = gb.addProduction(C, e);
	NonTerminal S = gb.addNonTerminal("S"); // string (non-empty left-recursion based string of Cs)
	Production S_C = gb.addProduction(S, C);
	Production S_SC = gb.addProduction(S, S, C);
	Grammar g = gb.build();
	Sapling sapling = g.newSapling(S);

	/**
	 * No two 'c' tokens are allowed in a row.
	 * This version checks the condition immediately after a shift.
	 */
	@Test
	public void noSequentialCs_checkAfterShift() throws UnexpectedTokensException {
		NodeAstCallback<Terminal> callback = new NodeAstCallback<Terminal>() {
			@Override
			public Terminal getSymbolOfToken(Terminal token) {
				return token;
			}

			@Override
			protected Object getStackableToken(Terminal token) {
				return token;
			}

			@Override
			public Predicate<Object> cull(Object branch) {
				Object[] buf = new Object[2];
				Ref<Integer> count = new Ref<>(0);
				stack.enumerate(branch, e -> {
					buf[0] = buf[1];
					buf[1] = e;
					count.accept(count.get() + 1);
				});
				if (count.get() < 2) {
					return null;
				}
				if (buf[1] != c) {
					return null;
				}
				Node prev = (Node) buf[0]; // either S_C or S_SC
				prev = (Node) prev.elements.get(prev.elements.size() - 1); // C_*
				if (prev.production == C_c) {
					return b -> b == branch;
				}
				return null;
			}
		};

		State state = State.initializeFrom(sapling, 0);
		state = state.advance(Collections.singletonMap(c, 0), callback, 1, singleton(1));
		Set<Object> idsAfter1 = state.getUsedStackIds();
		state = state.advance(Collections.singletonMap(c, 1), callback, 2, asList(1, 2));
		Set<Object> idsAfter2 = state.getUsedStackIds();
		assertEquals(idsAfter2, idsAfter1);
		state = state.advance(singletonMap(e, 1), callback, 3, singleton(3));
		System.out.println("---");
		for (Object id : state.getUsedStackIds()) {
			callback.stack.enumerate(id, System.out::print);
			System.out.println();
		}
	}

}
