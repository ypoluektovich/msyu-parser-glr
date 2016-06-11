package org.msyu.parser.glr.test;

import org.msyu.javautil.cf.NoOp;
import org.msyu.parser.glr.ASymbol;
import org.msyu.parser.glr.GlrCallback;
import org.msyu.parser.glr.ProductionHandle;
import org.msyu.parser.glr.State;
import org.msyu.parser.glr.Terminal;
import org.msyu.parser.treestack.TreeStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.msyu.javautil.cf.Iterators.concat;
import static org.msyu.javautil.cf.Iterators.singletonIterator;

class LoggingCallback implements GlrCallback {

	private final TreeStack<ASymbol> stack = new TreeStack<>();

	private int indexSource = 0;
	private final Map<Object, Integer> indexByBranch = new HashMap<>();

	private Terminal symbol;

	State advance(State state, Terminal symbol) {
		System.out.printf("iteration: %s\n", symbol);
		this.symbol = symbol;

		state = state.advance(symbol, this);

		Set<Object> usedStackIds = state.getUsedStackIds();
		System.out.println("completed: " + usedStackIds);
		stack.retain(usedStackIds);
		stack.merge(usedStackIds);
		System.out.println();

		this.symbol = null;
		return state;
	}

	private List<ASymbol> enumerate(Object branch) {
		List<ASymbol> elements = new ArrayList<>();
		stack.enumerate(branch, elements::add);
		return elements;
	}

	@Override
	public Object shift(Object oldBranch, List<ASymbol> prependedEmptySymbols) {
		Object newBranch = stack.push(oldBranch, concat(prependedEmptySymbols.iterator(), singletonIterator(symbol)));
		Integer oldIndex = indexByBranch.get(oldBranch);
		Integer newIndex = indexByBranch.computeIfAbsent(newBranch, __ -> ++indexSource);
		System.out.printf("shift(%s, %s) -> %s %s\n", oldIndex, prependedEmptySymbols, newIndex, enumerate(newBranch));
		return newBranch;
	}

	@Override
	public Object skip(Object oldBranch, List<ASymbol> emptySymbols) {
		Object newBranch = stack.push(oldBranch, emptySymbols.iterator());
		Integer oldIndex = indexByBranch.get(oldBranch);
		Integer newIndex = indexByBranch.computeIfAbsent(newBranch, __ -> ++indexSource);
		System.out.printf("skip(%s, %s) -> %s %s\n", oldIndex, emptySymbols, newIndex, enumerate(newBranch));
		return newBranch;
	}

	@Override
	public Object reduce(Object oldBranch, ProductionHandle production) {
		Object popped = stack.pop(oldBranch, production.getRHS().size(), NoOp.consumer());
		Object pushed = stack.push(popped, singletonIterator(production.getLHS()));
		Integer oldIndex = indexByBranch.get(oldBranch);
		Integer newIndex = indexByBranch.computeIfAbsent(pushed, __ -> ++indexSource);
		System.out.printf("reduce(%s, %s) -> %s %s\n", oldIndex, production, newIndex, enumerate(pushed));
		return pushed;
	}

	@Override
	public Object insert(Object oldBranch, List<ASymbol> emptySymbols) {
		Object newBranch;
		if (emptySymbols.isEmpty()) {
			newBranch = oldBranch;
		} else {
			AtomicReference<ASymbol> temp = new AtomicReference<>();
			Object popped = stack.pop(oldBranch, 1, temp::set);
			newBranch = stack.push(popped, concat(emptySymbols.iterator(), singletonIterator(temp.get())));
		}
		Integer oldIndex = indexByBranch.get(oldBranch);
		Integer newIndex = indexByBranch.computeIfAbsent(newBranch, __ -> ++indexSource);
		System.out.printf("insert(%s, %s) -> %s %s\n", oldIndex, emptySymbols, newIndex, enumerate(newBranch));
		return newBranch;
	}

}
