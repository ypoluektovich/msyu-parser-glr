package org.msyu.parser.richgrammar;

import org.msyu.javautil.cf.CopyList;
import org.msyu.parser.glr.ASymbol;
import org.msyu.parser.glr.NonTerminal;

import java.util.List;

public final class TokProduction implements NoopReducibleRichProduction {

	private final List<ASymbol> tokens;

	public TokProduction(List<? extends ASymbol> tokens) {
		this.tokens = CopyList.immutable(tokens);
	}

	public final RichProduction reduce(BasicReducer reducer) {
		return new RichProduction() {
			@Override
			final NonTerminal deplete(DepletedProduction.Builder builder) {
				NonTerminal lhs = builder.createOuterNonTerminal();
				builder.addProduction(lhs, tokens, reducer);
				return lhs;
			}
		};
	}

	@Override
	public final RichProduction reduceNOOP() {
		return reduce(BasicReducer.NOOP);
	}

}
