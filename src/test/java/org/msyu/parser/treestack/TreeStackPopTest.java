package org.msyu.parser.treestack;

import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.function.Consumer;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.inOrder;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;

public class TreeStackPopTest {

	private TreeStack<Integer> stack;
	private Object id2;
	private Object id4;
	private Object id6;
	@Mock private Consumer<Integer> sink;
	private InOrder inOrder;

	@BeforeMethod
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		inOrder = inOrder(sink);
	}

	private void emptyStack() {
		stack = new TreeStack<>();
	}

	private void threeSegmentStack() {
		emptyStack();
		id2 = stack.push(null, asList(1, 2).iterator());
		id4 = stack.push(id2, asList(3, 4).iterator());
		id6 = stack.push(id4, asList(5, 6).iterator());
	}

	private void verifyPops(int... elements) {
		for (Integer element : elements) {
			inOrder.verify(sink).accept(element);
		}
	}

	@AfterMethod
	public void afterMethod() {
		inOrder.verifyNoMoreInteractions();
		Mockito.validateMockitoUsage();
	}

	@Test
	public void popZeroFromNothing() {
		// given
		emptyStack();
		// when
		Object id = stack.pop(null, 0, sink);
		// then
		assertNull(id);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void popSomeFromNothing() {
		// given
		emptyStack();
		// when
		stack.pop(null, 1, sink);
		// then
		// should throw
	}

	@Test
	public void popOneBranch() {
		// given
		threeSegmentStack();
		// when
		Object id = stack.pop(id6, 2, sink);
		// then
		assertSame(id, id4);
		verifyPops(6, 5);
	}

	@Test
	public void popHalfBranch() {
		// given
		threeSegmentStack();
		// when
		Object id = stack.pop(id6, 1, sink);
		// then
		assertSame(stack.branchById.get(id6).parent, id);
		assertSame(stack.branchById.get(id).parent, id4);
		verifyPops(6);
	}

	@Test
	public void popOneAndAHalfBranches() {
		// given
		threeSegmentStack();
		// when
		Object id = stack.pop(id6, 3, sink);
		// then
		assertSame(stack.branchById.get(id4).parent, id);
		assertSame(stack.branchById.get(id).parent, id2);
		verifyPops(6, 5, 4);
	}

	@Test
	public void popTwoBranches() {
		// given
		threeSegmentStack();
		// when
		Object id = stack.pop(id6, 4, sink);
		// then
		assertSame(id, id2);
		verifyPops(6, 5, 4, 3);
	}

	@Test
	public void popFullStack() {
		// given
		threeSegmentStack();
		// when
		Object id = stack.pop(id6, 6, sink);
		// then
		assertNull(id);
		verifyPops(6, 5, 4, 3, 2, 1);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void underflow() {
		// given
		threeSegmentStack();
		// when
		stack.pop(id6, 7, sink);
		// then
		// should throw
	}

}
