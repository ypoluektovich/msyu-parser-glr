package org.msyu.parser.richgrammar;

import org.msyu.javautil.cf.CopyList;
import org.msyu.parser.glr.NonTerminal;

import java.util.List;

import static java.util.Collections.singletonList;

public final class AltProduction {

	private final List<RichProduction> alternatives;

	public AltProduction(List<RichProduction> alternatives) {
		this.alternatives = CopyList.immutable(alternatives);
	}

	public final RichProduction reduce(AltReducer reducer) {
		return new RichProduction() {
			@Override
			final NonTerminal deplete(DepletedProduction.Builder builder) {
				NonTerminal lhs = builder.createOuterNonTerminal();
				for (int i = 0; i < alternatives.size(); i++) {
					RichProduction alternative = alternatives.get(i);
					int index = i;
					builder.addProduction(
							lhs,
							singletonList(alternative.deplete(builder)),
							(gc, ob) -> reducer.reduce(gc, ob, index)
					);
				}
				return lhs;
			}
		};
	}


}
