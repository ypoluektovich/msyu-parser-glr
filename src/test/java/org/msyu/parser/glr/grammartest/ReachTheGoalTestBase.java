package org.msyu.parser.glr.grammartest;

import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.msyu.parser.glr.GlrCallback;
import org.msyu.parser.glr.GrammarBuilder;
import org.msyu.parser.glr.NonTerminal;
import org.msyu.parser.glr.ProductionHandle;
import org.msyu.parser.glr.Sapling;
import org.msyu.parser.glr.SingleGrammarTestBase;
import org.msyu.parser.glr.State;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

public abstract class ReachTheGoalTestBase<T> extends SingleGrammarTestBase {

	protected GrammarBuilder gb = new GrammarBuilder();

	protected NonTerminal goal;

	protected ProductionHandle goalProduction;

	protected Sapling sapling;

	@Spy protected GlrCallback<T> callback;

	protected State state;

	@BeforeClass
	public void finishRTGInit() {
		grammar = gb.build();
		sapling = grammar.newSapling(goal);
	}

	@BeforeMethod
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		state = State.initializeFrom(sapling);
	}

	@AfterMethod
	public void afterMethod() {
		Mockito.validateMockitoUsage();
	}

}
