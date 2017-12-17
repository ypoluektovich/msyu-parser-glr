package org.msyu.parser.glr;

import org.msyu.parser.treestack.TreeStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.msyu.javautil.cf.Iterators.concat;
import static org.msyu.javautil.cf.Iterators.singletonIterator;

public abstract class NodeAstCallback<T> implements GlrCallback<T> {

	public final TreeStack<Object> tree = new TreeStack<>();

	protected Iterator<Object> emptySymbolsToTokens(List<ASymbol> prependedEmptySymbols) {
		assert prependedEmptySymbols.isEmpty() : "why are there empty symbols?";
		return Collections.nCopies(prependedEmptySymbols.size(), null).iterator();
	}

	protected abstract Object getStackableToken(T token);

	@Override
	public Object shift(Object oldBranch, List<ASymbol> prependedEmptySymbols, T token) {
		return tree.push(
				oldBranch,
				concat(
						emptySymbolsToTokens(prependedEmptySymbols),
						singletonIterator(getStackableToken(token))
				)
		);
	}

	@Override
	public Object skip(Object oldBranch, List<ASymbol> emptySymbols) {
		return tree.push(oldBranch, emptySymbolsToTokens(emptySymbols));
	}

	@Override
	public Object reduce(Object oldBranch, Production production) {
		List<Object> stuffList = new ArrayList<>();
		Object popped = tree.pop(oldBranch, production.rhs.size(), thing -> stuffList.add(0, thing));
		return tree.push(popped, singletonIterator(new Node(production, stuffList)));
	}

	@Override
	public Object insert(Object oldBranch, List<ASymbol> emptySymbols) {
		if (emptySymbols.isEmpty()) {
			return oldBranch;
		}
		Ref<Object> temp = new Ref<>();
		Object popped = tree.pop(oldBranch, 1, temp);
		return tree.push(popped, concat(emptySymbolsToTokens(emptySymbols), temp.iterator()));
	}

}
