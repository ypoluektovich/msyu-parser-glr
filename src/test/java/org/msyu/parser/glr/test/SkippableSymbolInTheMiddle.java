package org.msyu.parser.glr.test;

import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.msyu.parser.glr.Grammar;
import org.msyu.parser.glr.GrammarBuilder;
import org.msyu.parser.glr.NonTerminal;
import org.msyu.parser.glr.ProductionHandle;
import org.msyu.parser.glr.Sapling;
import org.msyu.parser.glr.State;
import org.msyu.parser.glr.Terminal;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

public class SkippableSymbolInTheMiddle {

	GrammarBuilder gb = new GrammarBuilder();

	Terminal prefix = gb.addTerminal("prefix");
	Terminal middle = gb.addTerminal("middle");
	Terminal suffix = gb.addTerminal("suffix");

	NonTerminal skippable = gb.addNonTerminal("Skippable");
	NonTerminal goal = gb.addNonTerminal("Goal");

	ProductionHandle goalProduction;

	{
		gb.addProduction(skippable);
		gb.addProduction(skippable, middle);

		goalProduction = gb.addProduction(goal, prefix, skippable, suffix);
	}

	Grammar grammar = gb.build();
	Sapling sapling = grammar.newSapling(goal);

	@Spy LoggingCallback callback = new LoggingCallback();

	@BeforeMethod
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
	}

	@AfterMethod
	public void afterMethod() {
		Mockito.validateMockitoUsage();
	}

	@Test
	public void skipped() {
		State state = State.initializeFrom(sapling, callback);
		callback.completeIteration(state);

		state = callback.advance(state, prefix);
		state = callback.advance(state, suffix);

		verify(callback).reduce(any(), eq(goalProduction), anyInt(), any());
	}

	@Test
	public void filled() {
		State state = State.initializeFrom(sapling, callback);
		callback.completeIteration(state);

		state = callback.advance(state, prefix);
		state = callback.advance(state, middle);
		state = callback.advance(state, suffix);

		verify(callback).reduce(any(), eq(goalProduction), anyInt(), any());
	}

}
