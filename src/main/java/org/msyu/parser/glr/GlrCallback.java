package org.msyu.parser.glr;

public interface GlrCallback {

	Object newBranchId();

	void shift(Object oldBranch, Object newBranch);

	void reduce(Object oldBranch, ProductionHandle productionHandle, int prependedEmptySymbols, Object newBranch);

}
