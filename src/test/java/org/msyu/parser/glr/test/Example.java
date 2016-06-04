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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
		callback.completeIteration();
		state = state.advance(id, callback);
		callback.completeIteration();
		state = state.advance(times, callback);
		callback.completeIteration();
		state = state.advance(num, callback);
		callback.completeIteration();
		state = state.advance(plus, callback);
		callback.completeIteration();
		state = state.advance(num, callback);
		callback.completeIteration();
		state = state.advance(eof, callback);
		callback.completeIteration();
	}

	private static class MyCallback implements GlrCallback {

		private int indexSource = 0;
		private final Map<Object, Deque<ASymbol>> dequeByBranch = new HashMap<>();
		private final Map<Object, Integer> indexByBranch = new HashMap<>();
		private final Set<Object> newAndNotReferencedBranches = new HashSet<>();

		@Override
		public void shift(Object oldBranch, List<ASymbol> symbols, Object newBranch) {
			if (oldBranch == null) {
				dequeByBranch.put(newBranch, new ArrayDeque<>(symbols));
			} else {
				Deque<ASymbol> newDeque = new ArrayDeque<>(dequeByBranch.get(oldBranch));
				newDeque.addAll(symbols);
				dequeByBranch.put(newBranch, newDeque);
			}
			newAndNotReferencedBranches.remove(oldBranch);
			newAndNotReferencedBranches.add(newBranch);
			System.out.printf(
					"shift(%s, %s, %s%s) -> %s\n",
					indexByBranch.get(oldBranch),
					symbols,
					indexByBranch.computeIfAbsent(newBranch, __ -> ++indexSource),
					newBranch,
					dequeByBranch.get(newBranch)
			);
		}

		@Override
		public void reduce(Object oldBranch, ProductionHandle productionHandle, List<ASymbol> prependedEmptySymbols, Object newBranch) {
			Deque<ASymbol> newDeque = new ArrayDeque<>(dequeByBranch.get(oldBranch));
			for (int i = 0; i < productionHandle.getRHS().size(); ++i) {
				newDeque.removeLast();
			}
			newDeque.addAll(prependedEmptySymbols);
			newDeque.add(productionHandle.getLHS());
			dequeByBranch.put(newBranch, newDeque);
			newAndNotReferencedBranches.remove(oldBranch);
			newAndNotReferencedBranches.add(newBranch);
			System.out.printf(
					"reduce(%s, %s, %s, %s%s) -> %s\n",
					indexByBranch.get(oldBranch),
					productionHandle,
					prependedEmptySymbols,
					indexByBranch.computeIfAbsent(newBranch, __ -> ++indexSource),
					newBranch,
					dequeByBranch.get(newBranch)
			);
		}

		final void completeIteration() {
			System.out.println("completed");
			for (Iterator<Object> iterator = dequeByBranch.keySet().iterator(); iterator.hasNext(); ) {
				Object branch = iterator.next();
				if (!newAndNotReferencedBranches.contains(branch)) {
					iterator.remove();
					indexByBranch.remove(branch);
				}
			}
			newAndNotReferencedBranches.clear();
			System.out.println(indexByBranch.values());
			System.out.println();
		}

	}

}
