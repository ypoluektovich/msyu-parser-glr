package org.msyu.parser.glr.test;

import org.msyu.parser.glr.ASymbol;
import org.msyu.parser.glr.GlrCallback;
import org.msyu.parser.glr.ProductionHandle;
import org.msyu.parser.glr.State;
import org.msyu.parser.glr.Terminal;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

class LoggingCallback implements GlrCallback {

	private int indexSource = 0;
	private final Map<Object, Deque<ASymbol>> dequeByBranch = new HashMap<>();

	private Terminal symbol;

	State advance(State state, Terminal symbol) {
		System.out.printf("iteration: %s\n", symbol);
		this.symbol = symbol;
		state = state.advance(symbol, this);
		completeIteration(state);
		this.symbol = null;
		return state;
	}

	@Override
	public Object newBranchId() {
		return ++indexSource;
	}

	@Override
	public void shift(Object oldBranch, Object newBranch) {
		Deque<ASymbol> newDeque;
		if (oldBranch == null) {
			newDeque = new ArrayDeque<>();
		} else {
			newDeque = new ArrayDeque<>(dequeByBranch.get(oldBranch));
			newDeque.add(symbol);
		}
		dequeByBranch.put(newBranch, newDeque);
		System.out.printf(
				"shift(%s, %s) -> %s\n",
				oldBranch,
				newBranch,
				dequeByBranch.get(newBranch)
		);
	}

	@Override
	public void reduce(Object oldBranch, ProductionHandle production, int prependedEmptySymbols, Object newBranch) {
		Deque<ASymbol> newDeque = new ArrayDeque<>(dequeByBranch.get(oldBranch));
		for (int i = 0, n = production.getRHS().size() - prependedEmptySymbols; i < n; ++i) {
			newDeque.removeLast();
		}
		newDeque.add(production.getLHS());
		dequeByBranch.put(newBranch, newDeque);
		System.out.printf(
				"reduce(%s, %s, %s, %s) -> %s\n",
				oldBranch,
				production,
				prependedEmptySymbols,
				newBranch,
				dequeByBranch.get(newBranch)
		);
	}

	void completeIteration(State state) {
		System.out.println("completed");
		dequeByBranch.keySet().retainAll(state.getUsedStackIds());
		System.out.println(dequeByBranch.keySet());
		System.out.println();
	}

}
