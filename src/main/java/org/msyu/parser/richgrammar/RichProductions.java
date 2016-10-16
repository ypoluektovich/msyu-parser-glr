package org.msyu.parser.richgrammar;

import org.msyu.parser.glr.ASymbol;

import static java.util.Arrays.asList;

public class RichProductions {

	public static TokProduction tok(ASymbol... tokens) {
		return new TokProduction(asList(tokens));
	}

	public static SeqProduction seq(RichProduction... sequence) {
		return new SeqProduction(asList(sequence));
	}

	public static AltProduction alt(RichProduction... alternatives) {
		return new AltProduction(asList(alternatives));
	}

	public static RptProduction rpt(int min, int max, RichProduction production) {
		return new RptProduction(min, max, production);
	}

}
