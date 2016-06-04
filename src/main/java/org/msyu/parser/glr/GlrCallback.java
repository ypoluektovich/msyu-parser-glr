package org.msyu.parser.glr;

import java.util.List;

public interface GlrCallback {

	Object newBranchId();

	void shift(Object oldBranch, List<ASymbol> symbols, Object newBranch);

	void reduce(Object oldBranch, ProductionHandle productionHandle, int prependedEmptySymbols, Object newBranch);

}
