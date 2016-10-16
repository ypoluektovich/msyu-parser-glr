package org.msyu.parser.richgrammar;

import org.msyu.parser.glr.ASymbol;
import org.msyu.parser.glr.NonTerminal;

import static java.util.Arrays.asList;
import static java.util.Collections.nCopies;
import static java.util.Collections.singletonList;

public final class RptProduction implements NoopReducibleRichProduction {

	private final int min;
	private final int max;
	private final RichProduction production;

	public RptProduction(int min, int max, RichProduction production) {
		if (min < 0) {
			throw new IllegalArgumentException("negative min repeat number");
		}
		if (min >= max) {
			throw new IllegalArgumentException("max repeat number is not greater than min");
		}
		this.min = min;
		this.max = max;
		this.production = production;
	}

	public final RichProduction reduce(RptReducer appender, RptReducer finisher) {
		return new RichProduction() {
			@Override
			final NonTerminal deplete(DepletedProduction.Builder builder) {
				NonTerminal lhs = builder.createOuterNonTerminal();

				ASymbol elementSymbol = production.deplete(builder);

				NonTerminal body = builder.createInnerNonTerminal();
				builder.addProduction(body, nCopies(min, elementSymbol), call(appender, 0, min));
				builder.addProduction(lhs, singletonList(body), call(finisher, 0, min));

				NonTerminal prev = body;
				int count = min;
				while (count != max) {
					NonTerminal next = builder.createInnerNonTerminal();
					builder.addProduction(next, asList(prev, elementSymbol), call(appender, count, ++count));
					builder.addProduction(lhs, singletonList(next), call(finisher, 0, count));
					prev = next;
				}

				return lhs;
			}

		};
	}

	private static BasicReducer call(RptReducer reducer, int was, int increase) {
		return (gc, ob) -> reducer.reduce(gc, ob, was, increase);
	}

	@Override
	public RichProduction reduceNOOP() {
		return reduce(RptReducer.NOOP, RptReducer.NOOP);
	}

}
