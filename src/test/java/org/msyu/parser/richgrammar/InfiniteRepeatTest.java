package org.msyu.parser.richgrammar;

import org.mockito.ArgumentCaptor;
import org.msyu.parser.glr.ASymbol;
import org.msyu.parser.glr.GlrCallback;
import org.msyu.parser.glr.NonTerminal;
import org.msyu.parser.glr.Production;
import org.msyu.parser.glr.Terminal;
import org.msyu.parser.glr.grammartest.ReachTheGoalTestBase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.msyu.parser.richgrammar.RichProductions.rpt;
import static org.msyu.parser.richgrammar.RichProductions.seq;
import static org.msyu.parser.richgrammar.RichProductions.tok;

public class InfiniteRepeatTest extends ReachTheGoalTestBase<Terminal, InfiniteRepeatTest.Callback> {

	class Callback implements GlrCallback<Terminal> {
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

	private final Collection<DepletedProduction> depletedProductions = new ArrayList<>();

	Terminal s = gb.addTerminal("s");
	Terminal e = gb.addTerminal("e");

	private NonTerminal install(String name, RichProduction rp) {
		DepletedProduction depletedProduction = rp.installIn(gb, name);
		depletedProductions.add(depletedProduction);
		return depletedProduction.lhs;
	}

	{
		goal = install("Goal", seq(rpt(0, RptReducer.INF, tok(s).reduceNOOP()).reduceNOOP(), tok(e).reduceNOOP()).reduceNOOP());
	}

	@BeforeMethod
	@Override
	public void beforeMethod() {
		callback = new Callback();
		super.beforeMethod();
	}

	private void test(int n) throws Exception {
		List<Terminal> tokens = new ArrayList<>();
		for (int i = 0; i < n; ++i) {
			tokens.add(s);
		}
		tokens.add(e);

		for (Terminal token : tokens) {
			state = state.advance(token, callback);
		}

		ArgumentCaptor<Production> reductionCaptor = ArgumentCaptor.forClass(Production.class);
		verify(callback, atLeastOnce()).reduce(any(), reductionCaptor.capture());
		assert reductionCaptor.getAllValues().stream().filter(p -> p.lhs.equals(goal)).count() == 1;
	}

	@Test
	public void zero() throws Exception {
		test(0);
	}

	@Test
	public void one() throws Exception {
		test(1);
	}

	@Test
	public void two() throws Exception {
		test(2);
	}

	@Test
	public void five() throws Exception {
		test(5);
	}

}
