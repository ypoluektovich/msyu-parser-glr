package org.msyu.parser.treestack;

import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.function.Consumer;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class TreeStackEnumerateTest {

	private static final TreeStack<Integer> stack = new TreeStack<>();
	private static final Object id1 = stack.push(null, singletonList(1).iterator());
	private static final Object id2 = stack.push(id1, singletonList(2).iterator());
	private static final Object id3 = stack.push(id2, singletonList(3).iterator());
	private static final Object id4 = stack.push(id1, singletonList(4).iterator());

	@DataProvider
	public static Object[][] tests() {
		return new Object[][]{
				new Object[]{id1, singletonList(1)},
				new Object[]{id2, asList(1, 2)},
				new Object[]{id3, asList(1, 2, 3)},
				new Object[]{id4, asList(1, 4)},
				new Object[]{null, emptyList()}
		};
	}

	@Mock private Consumer<Integer> sink;
	private InOrder inOrder;

	@BeforeMethod
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		inOrder = Mockito.inOrder(sink);
	}

	@Test(dataProvider = "tests")
	public void test(Object id, List<Integer> expected) {
		stack.enumerate(id, sink);
		for (Integer element : expected) {
			inOrder.verify(sink).accept(element);
		}
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void badId() {
		stack.enumerate(new Object(), sink);
	}

	@AfterMethod
	public void afterMethod() {
		inOrder.verifyNoMoreInteractions();
		Mockito.validateMockitoUsage();
	}

}

