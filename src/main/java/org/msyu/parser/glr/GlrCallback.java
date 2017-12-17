package org.msyu.parser.glr;

import java.util.List;

public interface GlrCallback<T> {

	Terminal getSymbolOfToken(T token);

	Object shift(Object oldBranch, List<ASymbol> prependedEmptySymbols, T token);

	Object skip(Object oldBranch, List<ASymbol> emptySymbols);

	Object reduce(Object oldBranch, Production production);

	Object insert(Object oldBranch, List<ASymbol> emptySymbols);

	default CullPredicate cull(ItemStackView stack) {
		return null;
	}

}
