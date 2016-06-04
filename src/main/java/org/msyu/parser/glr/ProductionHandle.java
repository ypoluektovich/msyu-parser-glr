package org.msyu.parser.glr;

import java.util.List;

public interface ProductionHandle {
	NonTerminal getLHS();
	List<ASymbol> getRHS();
}
