package org.msyu.parser.glr.test;

import org.msyu.parser.glr.ASymbol;
import org.msyu.parser.glr.GlrCallback;
import org.msyu.parser.glr.ProductionHandle;
import org.msyu.parser.glr.State;
import org.msyu.parser.glr.Terminal;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
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

	private Object newBranchId() {
		return ++indexSource;
	}

	@Override
	public Object shift(Object oldBranch, List<ASymbol> prependedEmptySymbols) {
		boolean notInitialBranch = oldBranch != null;
		Deque<ASymbol> newDeque = new ArrayDeque<>(notInitialBranch ? dequeByBranch.get(oldBranch) : Collections.emptyList());
		newDeque.addAll(prependedEmptySymbols);
		if (notInitialBranch) {
			newDeque.add(symbol);
		}
		Object newBranch = newBranchId();
		dequeByBranch.put(newBranch, newDeque);
		System.out.printf("shift(%s, %s) -> %s %s\n", oldBranch, prependedEmptySymbols, newBranch, newDeque);
		return newBranch;
	}

	@Override
	public Object skip(Object oldBranch, List<ASymbol> emptySymbols) {
		Deque<ASymbol> newDeque = new ArrayDeque<>(dequeByBranch.get(oldBranch));
		newDeque.addAll(emptySymbols);
		Object newBranch = newBranchId();
		dequeByBranch.put(newBranch, newDeque);
		System.out.printf("skip(%s, %s) -> %s %s\n", oldBranch, emptySymbols, newBranch, newDeque);
		return newBranch;
	}

	@Override
	public Object reduce(Object oldBranch, ProductionHandle production) {
		Deque<ASymbol> newDeque = new ArrayDeque<>(dequeByBranch.get(oldBranch));
		for (int i = 0; i < production.getRHS().size(); ++i) {
			newDeque.removeLast();
		}
		newDeque.add(production.getLHS());
		Object newBranch = newBranchId();
		dequeByBranch.put(newBranch, newDeque);
		System.out.printf("reduce(%s, %s) -> %s %s\n", oldBranch, production, newBranch, newDeque);
		return newBranch;
	}

	@Override
	public Object insert(Object oldBranch, List<ASymbol> emptySymbols) {
		Deque<ASymbol> newDeque = new ArrayDeque<>(dequeByBranch.get(oldBranch));
		ASymbol popped = newDeque.removeLast();
		newDeque.addAll(emptySymbols);
		newDeque.add(popped);
		Object newBranch = newBranchId();
		dequeByBranch.put(newBranch, newDeque);
		System.out.printf("insert(%s, %s) -> %s %s\n", oldBranch, emptySymbols, newBranch, newDeque);
		return newBranch;
	}

	void completeIteration(State state) {
		System.out.println("completed");
		dequeByBranch.keySet().retainAll(state.getUsedStackIds());
		System.out.println(dequeByBranch.keySet());
		System.out.println();
	}

}
