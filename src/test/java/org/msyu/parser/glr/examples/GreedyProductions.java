package org.msyu.parser.glr.examples;

import org.msyu.parser.glr.GlrCallback;
import org.msyu.parser.glr.GrammarBuilder;
import org.msyu.parser.glr.Input;
import org.msyu.parser.glr.Lifeline;
import org.msyu.parser.glr.NonTerminal;
import org.msyu.parser.glr.Production;
import org.msyu.parser.glr.ScannerlessState;
import org.msyu.parser.glr.State;
import org.msyu.parser.glr.Terminal;
import org.msyu.parser.glr.UnexpectedTokenException;
import org.msyu.parser.glr.UnexpectedTokensException;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import static java.util.Arrays.asList;
import static org.msyu.parser.glr.Input.singleton;

public class GreedyProductions {

	@Test
	public void greedyScannerless() throws UnexpectedTokenException {
		GrammarBuilder gb = new GrammarBuilder();
		Terminal t = gb.addTerminal("t");
		NonTerminal G = gb.addNonTerminal("G");
		NonTerminal R = gb.addNonTerminal("R");
		NonTerminal C = gb.addNonTerminal("C");
		Production grProd = gb.addProduction(G, R, t);
		Production grtProd = gb.addProduction(G, R);
		Production rProd = gb.addGreedyProduction(R, C, t);
		Production ctProd = gb.addProduction(C, t);
		Production crProd = gb.addProduction(C, R);

		Map<Lifeline, Object> goalByLifeline = new HashMap<>();
		GlrCallback<Terminal> callback = new NaiveAstCallback<Terminal>(
				(p, o, b, l) -> {
					System.out.println(p + " -> " + o + " on " + b + " (" + l + ")");
					if (l != null) {
						goalByLifeline.put(l, o);
					}
				}
		) {
			@Override
			protected Object getStackableToken(Terminal token) {
				return token;
			}

			@Override
			public Terminal getSymbolOfToken(Terminal token) {
				return token;
			}

			@Override
			public void cutLifelines(Predicate<Lifeline> lifelineIsCut) {
				for (Iterator<Lifeline> iterator = goalByLifeline.keySet().iterator(); iterator.hasNext(); ) {
					Lifeline lifeline = iterator.next();
					if (lifelineIsCut.test(lifeline)) {
						System.out.println("cut " + lifeline);
						iterator.remove();
					}
				}
			}
		};

		ScannerlessState state = ScannerlessState.initializeFrom(gb.build().newSapling(G));
		state = state.advance(t, callback);
		goalByLifeline.clear();
		state = state.advance(t, callback);
		goalByLifeline.clear();
		state = state.advance(t, callback);
		assert goalByLifeline.size() == 1;
	}

	@Test
	public void greedyWithScanner() throws UnexpectedTokensException {
		GrammarBuilder gb = new GrammarBuilder();
		Terminal t = gb.addTerminal("t");
		NonTerminal G = gb.addNonTerminal("G");
		NonTerminal C = gb.addNonTerminal("C");
		NonTerminal T = gb.addNonTerminal("T");
		Production gcProd = gb.addProduction(G, C);
		Production ctProd = gb.addProduction(C, T);
		Production cttProd = gb.addProduction(C, T, T);
		Production tProd = gb.addGreedyProduction(T, t);

		Map<Lifeline, Object> goalByLifeline = new HashMap<>();
		GlrCallback<AtomicReference<Terminal>> callback = new NaiveAstCallback<AtomicReference<Terminal>>(
				(p, o, b, l) -> {
					System.out.println(p + " -> " + o + " on " + b + " (" + l + ")");
					if (l != null) {
						goalByLifeline.put(l, o);
					}
				}
		) {
			@Override
			protected Object getStackableToken(AtomicReference<Terminal> token) {
				return token.get();
			}

			@Override
			public Terminal getSymbolOfToken(AtomicReference<Terminal> token) {
				return token.get();
			}

			@Override
			public void cutLifelines(Predicate<Lifeline> lifelineIsCut) {
				for (Iterator<Lifeline> iterator = goalByLifeline.keySet().iterator(); iterator.hasNext(); ) {
					Lifeline lifeline = iterator.next();
					if (lifelineIsCut.test(lifeline)) {
						System.out.println("cut " + lifeline);
						iterator.remove();
					}
				}
			}
		};

		State state = State.initializeFrom(gb.build().newSapling(G), 0);
		state = state.advance(
				singleton(0, new AtomicReference<>(t)),
				callback,
				1,
				asList(0, 1)
		);
		goalByLifeline.clear();
		state = state.advance(
				asList(
						new Input<>(0, new AtomicReference<>(t)),
						new Input<>(1, new AtomicReference<>(t))
				),
				callback,
				2,
				Collections.emptyList()
		);
		assert goalByLifeline.size() == 1;
		assert goalByLifeline.values().iterator().next().equals(asList(T, t));
	}

}
