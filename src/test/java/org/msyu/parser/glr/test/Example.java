package org.msyu.parser.glr.test;

import org.msyu.parser.glr.ASymbol;
import org.msyu.parser.glr.GlrCallback;
import org.msyu.parser.glr.Grammar;
import org.msyu.parser.glr.GrammarBuilder;
import org.msyu.parser.glr.NonTerminal;
import org.msyu.parser.glr.ProductionHandle;
import org.msyu.parser.glr.Sapling;
import org.msyu.parser.glr.State;
import org.msyu.parser.glr.Terminal;
import org.testng.annotations.Test;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Example {

	@Test
	public void example() {
		GrammarBuilder gb = new GrammarBuilder();

		Terminal id = gb.addTerminal("id");
		Terminal num = gb.addTerminal("num");
		Terminal times = gb.addTerminal("*");
		Terminal plus = gb.addTerminal("+");
		Terminal eof = gb.addTerminal("eof");

		NonTerminal value = gb.addNonTerminal("Value");
		gb.addProduction(value, id);
		gb.addProduction(value, num);

		NonTerminal products = gb.addNonTerminal("Products");
		gb.addProduction(products, products, times, value);
		gb.addProduction(products, value);

		NonTerminal sums = gb.addNonTerminal("Sums");
		gb.addProduction(sums, sums, plus, products);
		gb.addProduction(sums, products);

		NonTerminal goal = gb.addNonTerminal("Goal");
		gb.addProduction(goal, sums, eof);

		Grammar grammar = gb.build();

		Sapling sapling = grammar.newSapling(goal);

		MyCallback callback = new MyCallback();

		State state = State.initializeFrom(sapling, callback);
		callback.completeIteration(state);
		state = state.advance(id, callback);
		callback.completeIteration(state);
		state = state.advance(times, callback);
		callback.completeIteration(state);
		state = state.advance(num, callback);
		callback.completeIteration(state);
		state = state.advance(plus, callback);
		callback.completeIteration(state);
		state = state.advance(num, callback);
		callback.completeIteration(state);
		state = state.advance(eof, callback);
		callback.completeIteration(state);
	}

	private static class MyCallback implements GlrCallback {

		private int indexSource = 0;
		private final Map<Object, Deque<ASymbol>> dequeByBranch = new HashMap<>();

		@Override
		public Object newBranchId() {
			return ++indexSource;
		}

		@Override
		public void shift(Object oldBranch, List<ASymbol> symbols, Object newBranch) {
			if (oldBranch == null) {
				dequeByBranch.put(newBranch, new ArrayDeque<>(symbols));
			} else {
				Deque<ASymbol> newDeque = new ArrayDeque<>(dequeByBranch.get(oldBranch));
				newDeque.addAll(symbols);
				dequeByBranch.put(newBranch, newDeque);
			}
			System.out.printf(
					"shift(%s, %s, %s) -> %s\n",
					oldBranch,
					symbols,
					newBranch,
					dequeByBranch.get(newBranch)
			);
		}

		@Override
		public void reduce(Object oldBranch, ProductionHandle productionHandle, int prependedEmptySymbols, Object newBranch) {
			Deque<ASymbol> newDeque = new ArrayDeque<>(dequeByBranch.get(oldBranch));
			for (int i = 0, n = productionHandle.getRHS().size() - prependedEmptySymbols; i < n; ++i) {
				newDeque.removeLast();
			}
			newDeque.add(productionHandle.getLHS());
			dequeByBranch.put(newBranch, newDeque);
			System.out.printf(
					"reduce(%s, %s, %s, %s) -> %s\n",
					oldBranch,
					productionHandle,
					prependedEmptySymbols,
					newBranch,
					dequeByBranch.get(newBranch)
			);
		}

		final void completeIteration(State state) {
			System.out.println("completed");
			dequeByBranch.keySet().retainAll(state.getUsedStackIds());
			System.out.println(dequeByBranch.keySet());
			System.out.println();
		}

	}

}
