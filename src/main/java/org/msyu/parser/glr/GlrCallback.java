package org.msyu.parser.glr;

import java.util.List;

public interface GlrCallback {

	Object shift(Object oldBranch, List<ASymbol> prependedEmptySymbols);

	Object skip(Object oldBranch, List<ASymbol> emptySymbols);

	Object reduce(Object oldBranch, ProductionHandle production);

	Object insert(Object oldBranch, List<ASymbol> emptySymbols);

}
