package org.msyu.parser.glr.examples;

import org.mockito.Mock;
import org.msyu.parser.glr.ASymbol;
import org.msyu.parser.glr.GlrCallback;
import org.msyu.parser.glr.Lifeline;
import org.msyu.parser.glr.Production;
import org.msyu.parser.treestack.TreeStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.msyu.javautil.cf.Iterators.concat;
import static org.msyu.javautil.cf.Iterators.singletonIterator;

public abstract class NaiveAstCallback<T> implements GlrCallback<T> {

	private final TreeStack<Object> stack = new TreeStack<>();
	@Mock public final ReductionCallback reductionCallback;

	public NaiveAstCallback(ReductionCallback reductionCallback) {
		this.reductionCallback = reductionCallback;
	}

	public interface ReductionCallback {
		void reduced(Production production, Object toStack, Object branchId, Lifeline lifeline);
	}

	protected Iterator<Object> emptySymbolsToTokens(List<ASymbol> prependedEmptySymbols) {
		assert prependedEmptySymbols.isEmpty() : "why are there empty symbols?";
		return Collections.nCopies(prependedEmptySymbols.size(), null).iterator();
	}

	protected abstract Object getStackableToken(T token);

	@Override
	public Object shift(Object oldBranch, List<ASymbol> prependedEmptySymbols, T token) {
		return stack.push(
				oldBranch,
				concat(
						emptySymbolsToTokens(prependedEmptySymbols),
						singletonIterator(getStackableToken(token))
				)
		);
	}

	@Override
	public Object skip(Object oldBranch, List<ASymbol> emptySymbols) {
		return stack.push(oldBranch, emptySymbolsToTokens(emptySymbols));
	}

	@Override
	public Object reduce(Object oldBranch, Production production, Lifeline lifeline) {
		List<Object> stuffList = new ArrayList<>();
		Object popped = stack.pop(oldBranch, production.rhs.size(), thing -> stuffList.add(0, thing));
		Object stuffToStack;
		if (stuffList.size() == 1 && stuffList.get(0) instanceof List) {
			stuffToStack = stuffList.get(0);
		} else {
			stuffList.add(0, production.lhs);
			stuffToStack = stuffList;
		}
		Object pushed = stack.push(popped, singletonIterator(stuffToStack));
		reductionCallback.reduced(production, stuffToStack, pushed, lifeline);
		return pushed;
	}

	@Override
	public Object insert(Object oldBranch, List<ASymbol> emptySymbols) {
		if (emptySymbols.isEmpty()) {
			return oldBranch;
		}
		AtomicReference<Object> temp = new AtomicReference<>();
		Object popped = stack.pop(oldBranch, 1, temp::set);
		return stack.push(popped, concat(emptySymbolsToTokens(emptySymbols), singletonIterator(temp.get())));
	}

}
