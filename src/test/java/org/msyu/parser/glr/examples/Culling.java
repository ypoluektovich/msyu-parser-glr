package org.msyu.parser.glr.examples;

import org.msyu.parser.glr.Grammar;
import org.msyu.parser.glr.GrammarBuilder;
import org.msyu.parser.glr.ItemStackView;
import org.msyu.parser.glr.Node;
import org.msyu.parser.glr.NodeAstCallback;
import org.msyu.parser.glr.NonTerminal;
import org.msyu.parser.glr.Production;
import org.msyu.parser.glr.Sapling;
import org.msyu.parser.glr.State;
import org.msyu.parser.glr.StateBuilder;
import org.msyu.parser.glr.Terminal;
import org.msyu.parser.glr.UnexpectedTokenException;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

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
	public void noSequentialCs_checkAfterShift() throws UnexpectedTokenException {
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
			public Predicate<ItemStackView> cull(ItemStackView stack) {
				if (stack.getFirstFrame().getItem().production != C_c) {
					return null;
				}
				Object branch = stack.getId();
				List<Object> buf = tree.getLastElements(branch, 2);
				if (buf.size() < 2) {
					return null;
				}
				if (buf.get(1) != c) {
					return null;
				}
				Node prev = (Node) buf.get(0); // either S_C or S_SC
				prev = (Node) prev.elements.get(prev.elements.size() - 1); // C_*
				if (prev.production != C_c) {
					return null;
				}
				return s -> s.getId() == branch;
			}
		};

		State state = State.initializeFrom(sapling, 0);
		StateBuilder sb;

		sb = state.startAdvance(1);
		sb.advance(0, c, callback);
		state = sb.finish(singleton(1));

		Set<Object> idsAfter1 = state.getUsedStackIds();

		sb = state.startAdvance(2);
		try {
			sb.advance(1, c, callback);
			fail("should have thrown");
		} catch (UnexpectedTokenException e1) {
			// ignore
		}
		state = sb.finish(asList(1, 2));

		Set<Object> idsAfter2 = state.getUsedStackIds();
		assertEquals(idsAfter2, idsAfter1);

		sb = state.startAdvance(3);
		sb.advance(1, e, callback);
		state = sb.finish(singleton(3));

		System.out.println("---");
		for (Object id : state.getUsedStackIds()) {
			callback.tree.enumerate(id, System.out::print);
			System.out.println();
		}
	}

	/**
	 * No two 'c' tokens are allowed in a row.
	 * This version checks the condition after a S_SC reduction.
	 *
	 * The code is not too much different, but you can spot places in cull() that have become more uniform.
	 * Perhaps with some effort spent on "syntactic sugar", it could become much more readable.
	 */
	@Test
	public void noSequentialCs_checkAfterReduction() throws UnexpectedTokenException {
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
			public Predicate<ItemStackView> cull(ItemStackView stack) {
				if (stack.getFirstFrame().getItem().production != S_SC) {
					return null;
				}
				Object branch = stack.getId();
				List<Object> buf = tree.getLastElements(branch, 2);
				if (buf.size() < 2) {
					return null;
				}
				Node curr = (Node) buf.get(1);
				if (curr.production != C_c) {
					return null;
				}
				Node prev = (Node) buf.get(0); // either S_C or S_SC
				prev = (Node) prev.elements.get(prev.elements.size() - 1); // C_*
				if (prev.production != C_c) {
					return null;
				}
				return s -> s.getId() == branch;
			}
		};

		State state = State.initializeFrom(sapling, 0);
		StateBuilder sb;

		sb = state.startAdvance(1);
		sb.advance(0, c, callback);
		state = sb.finish(singleton(1));

		Set<Object> idsAfter1 = state.getUsedStackIds();

		sb = state.startAdvance(2);
		try {
			sb.advance(1, c, callback);
			fail("should have thrown");
		} catch (UnexpectedTokenException e1) {
			// ignore
		}
		state = sb.finish(asList(1, 2));

		Set<Object> idsAfter2 = state.getUsedStackIds();
		assertEquals(idsAfter2, idsAfter1);

		sb = state.startAdvance(3);
		sb.advance(1, e, callback);
		state = sb.finish(singleton(3));

		System.out.println("---");
		for (Object id : state.getUsedStackIds()) {
			callback.tree.enumerate(id, System.out::print);
			System.out.println();
		}
	}

}
