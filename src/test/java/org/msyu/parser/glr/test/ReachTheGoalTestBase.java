package org.msyu.parser.glr.test;

import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.msyu.parser.glr.ProductionHandle;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

abstract class ReachTheGoalTestBase {

	ProductionHandle goalProduction;

	@Spy LoggingCallback callback = new LoggingCallback();

	@BeforeMethod
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
	}

	@AfterMethod
	public void afterMethod() {
		Mockito.validateMockitoUsage();
	}

}
