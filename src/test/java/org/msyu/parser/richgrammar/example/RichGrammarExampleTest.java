package org.msyu.parser.richgrammar.example;

import org.mockito.ArgumentCaptor;
import org.msyu.parser.glr.ASymbol;
import org.msyu.parser.glr.GlrCallback;
import org.msyu.parser.glr.NonTerminal;
import org.msyu.parser.glr.Production;
import org.msyu.parser.glr.Terminal;
import org.msyu.parser.glr.grammartest.ReachTheGoalTestBase;
import org.msyu.parser.richgrammar.DepletedProduction;
import org.msyu.parser.richgrammar.RichProduction;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.msyu.parser.richgrammar.RichProductions.alt;
import static org.msyu.parser.richgrammar.RichProductions.rpt;
import static org.msyu.parser.richgrammar.RichProductions.seq;
import static org.msyu.parser.richgrammar.RichProductions.tok;

public class RichGrammarExampleTest extends ReachTheGoalTestBase<Terminal, RichGrammarExampleTest.Callback> {

	class Callback implements GlrCallback<Terminal> {
		private final Collection<DepletedProduction> depletedProductions = new ArrayList<>();

		@Override
		public Terminal getSymbolOfToken(Terminal token) {
			return token;
		}

		@Override
		public Object shift(Object oldBranch, List<ASymbol> prependedEmptySymbols, Terminal token) {
			return null;
		}

		@Override
		public Object skip(Object oldBranch, List<ASymbol> emptySymbols) {
			return null;
		}

		@Override
		public Object reduce(Object oldBranch, Production production) {
			for (DepletedProduction dp : depletedProductions) {
				if (dp.canReduce(production)) {
					return dp.reduce(this, oldBranch, production);
				}
			}
			return null;
		}

		@Override
		public Object insert(Object oldBranch, List<ASymbol> emptySymbols) {
			return null;
		}
	}

	{
		callback = new Callback();
	}

	Terminal p1 = gb.addTerminal("p1");
	Terminal p2 = gb.addTerminal("p2");
	Terminal m = gb.addTerminal("m");
	Terminal s = gb.addTerminal("s");

	private NonTerminal install(String name, RichProduction rp) {
		DepletedProduction depletedProduction = rp.installIn(gb, name);
		callback.depletedProductions.add(depletedProduction);
		return depletedProduction.lhs;
	}

	RichProduction P1 = tok(p1).reduce((c, o) -> null);
	RichProduction P2 = tok(p2).reduce((c, o) -> null);

	NonTerminal M = install(
			"M",
			rpt(
					0, 2,
					tok(m).reduce((c, o) -> null)
			).reduce(
					(c, o, w, i) -> null,
					(c, o, w, i) -> null
			)
	);

	{
		goal = install(
				"Goal",
				seq(
						alt(P1, P2).reduce((c, o, i) -> null),
						tok(M, s).reduce((c, o) -> null)
				).reduce((c, o) -> null)
		);
	}

	@Test
	public void run() throws Exception {
		List<Terminal> tokens = asList(p1, m, m, s);

		for (Terminal token : tokens) {
			state = state.advance(token, callback);
		}

		ArgumentCaptor<Production> reductionCaptor = ArgumentCaptor.forClass(Production.class);
		verify(callback, atLeastOnce()).reduce(any(), reductionCaptor.capture());
		assert reductionCaptor.getAllValues().stream().filter(p -> p.lhs.getName().equals("Goal")).count() == 1;
	}

}
