package org.msyu.parser.treestack;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;

public class TreeStackMergeTest {

	private TreeStack<Integer> stack;

	@BeforeMethod
	public void beforeMethod() throws Exception {
		stack = new TreeStack<>();
	}

	@Test
	public void mergeTail() {
		// given
		Object id1 = stack.push(null, singletonList(1).iterator());
		Object id2 = stack.push(id1, singletonList(2).iterator());
		Object id3 = stack.push(id2, singletonList(3).iterator());
		// when
		stack.merge(singleton(id3));
		// then
		assertEquals(stack.branchById.keySet(), singleton(id3));
		Branch<Integer> b3 = stack.branchById.get(id3);
		assertEquals(b3.elements, asList(1, 2, 3));
		assertNull(b3.parent);
	}

	@Test
	public void mergeTailWithRetains() {
		// given
		Object id1 = stack.push(null, singletonList(1).iterator());
		Object id2 = stack.push(id1, singletonList(2).iterator());
		Object id3 = stack.push(id2, singletonList(3).iterator());
		Object id4 = stack.push(id3, singletonList(4).iterator());
		// when
		stack.merge(asList(id2, id4));
		// then
		assertThat(stack.branchById.keySet(), hasSize(2));
		assertThat(stack.branchById.keySet(), hasItems(id2, id4));
		Branch<Integer> b2 = stack.branchById.get(id2);
		assertEquals(b2.elements, asList(1, 2));
		assertNull(b2.parent);
		Branch<Integer> b4 = stack.branchById.get(id4);
		assertEquals(b4.elements, asList(3, 4));
		assertSame(b4.parent, b2);
	}

	@Test
	public void mergeRetainsSplitTail() {
		// given
		Object id1 = stack.push(null, singletonList(1).iterator());
		Object id2 = stack.push(id1, singletonList(2).iterator());
		Object id3 = stack.push(id2, singletonList(3).iterator());
		Object id4 = stack.push(id2, singletonList(4).iterator());
		Object id5 = stack.push(id4, singletonList(5).iterator());
		// when
		stack.merge(emptySet());
		// then
		assertThat(stack.branchById.keySet(), hasSize(3));
		assertThat(stack.branchById.keySet(), hasItems(id2, id3, id5));
	}

}
