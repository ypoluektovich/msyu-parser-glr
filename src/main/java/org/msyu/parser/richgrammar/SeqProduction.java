package org.msyu.parser.richgrammar;

import org.msyu.javautil.cf.CopyList;
import org.msyu.parser.glr.NonTerminal;

import java.util.List;

import static java.util.stream.Collectors.toList;

public final class SeqProduction {

	private final List<RichProduction> productions;

	public SeqProduction(List<RichProduction> productions) {
		this.productions = CopyList.immutable(productions);
	}

	public final RichProduction reduce(BasicReducer reducer) {
		return new RichProduction() {
			@Override
			final NonTerminal deplete(DepletedProduction.Builder builder) {
				NonTerminal lhs = builder.createOuterNonTerminal();
				builder.addProduction(
						lhs,
						productions.stream().map(p -> p.deplete(builder)).collect(toList()),
						reducer
				);
				return lhs;
			}
		};
	}

}
