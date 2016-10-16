package org.msyu.parser.richgrammar;

import org.msyu.parser.glr.GrammarBuilder;
import org.msyu.parser.glr.NonTerminal;

public abstract class RichProduction {

	RichProduction() {
		// Until a use case is found, constructor access will remain limited.
	}

	abstract NonTerminal deplete(DepletedProduction.Builder builder);

	public final DepletedProduction installIn(GrammarBuilder gb, String lhsName) {
		DepletedProduction.Builder builder = new DepletedProduction.Builder(gb, lhsName);
		NonTerminal top = deplete(builder);
		return builder.build(top);
	}

}
