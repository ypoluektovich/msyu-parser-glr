package org.msyu.parser.richgrammar;

import org.msyu.javautil.cf.CopyMap;
import org.msyu.parser.glr.ASymbol;
import org.msyu.parser.glr.GlrCallback;
import org.msyu.parser.glr.GrammarBuilder;
import org.msyu.parser.glr.NonTerminal;
import org.msyu.parser.glr.Production;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DepletedProduction {

	public final NonTerminal lhs;
	private final Map<Production, BasicReducer> reducerByProduction;

	private DepletedProduction() {
		lhs = null;
		reducerByProduction = new HashMap<>();
	}

	private DepletedProduction(DepletedProduction builder, NonTerminal lhs) {
		this.lhs = lhs;
		reducerByProduction = CopyMap.immutableHash(builder.reducerByProduction);
	}

	public final boolean canReduce(Production production) {
		return reducerByProduction.containsKey(production);
	}

	public final Object reduce(GlrCallback<?> glrCallback, Object oldBranch, Production production) {
		return reducerByProduction.get(production).reduce(glrCallback, oldBranch);
	}

	static class Builder extends DepletedProduction {

		private final GrammarBuilder gb;
		private final String lhsName;
		private int outerIndexSource = 0;
		private int innerIndexSource;

		Builder(GrammarBuilder gb, String lhsName) {
			this.gb = gb;
			this.lhsName = lhsName;
		}

		final NonTerminal createOuterNonTerminal() {
			innerIndexSource = 0;
			int index = outerIndexSource++;
			return gb.addNonTerminal(index == 0 ? lhsName : (lhsName + "-" + index));
		}

		final NonTerminal createInnerNonTerminal() {
			return gb.addNonTerminal(lhsName + "-" + outerIndexSource + "-" + (++innerIndexSource));
		}

		final void addProduction(NonTerminal lhs, List<ASymbol> rhs, BasicReducer reducer) {
			((DepletedProduction) this).reducerByProduction.put(gb.addProduction(lhs, rhs), reducer);
		}

		final DepletedProduction build(NonTerminal lhs) {
			return new DepletedProduction(this, lhs);
		}

	}

}
