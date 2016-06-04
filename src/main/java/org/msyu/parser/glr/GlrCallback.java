package org.msyu.parser.glr;

import java.util.List;

public interface GlrCallback {

	void shift(Object oldBranch, List<ASymbol> symbols, Object newBranch);

	void reduce(Object oldBranch, ProductionHandle productionHandle, List<ASymbol> prependedEmptySymbols, Object newBranch);

}
