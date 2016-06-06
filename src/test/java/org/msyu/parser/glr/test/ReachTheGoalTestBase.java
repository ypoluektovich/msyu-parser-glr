package org.msyu.parser.glr.test;

import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.msyu.parser.glr.GrammarBuilder;
import org.msyu.parser.glr.NonTerminal;
import org.msyu.parser.glr.ProductionHandle;
import org.msyu.parser.glr.Sapling;
import org.msyu.parser.glr.SingleGrammarTestBase;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

abstract class ReachTheGoalTestBase extends SingleGrammarTestBase {

	GrammarBuilder gb = new GrammarBuilder();

	NonTerminal goal;

	ProductionHandle goalProduction;

	Sapling sapling;

	@Spy LoggingCallback callback = new LoggingCallback();

	@BeforeClass
	public void finishRTGInit() {
		grammar = gb.build();
		sapling = grammar.newSapling(goal);
	}

	@BeforeMethod
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
	}

	@AfterMethod
	public void afterMethod() {
		Mockito.validateMockitoUsage();
	}

}
