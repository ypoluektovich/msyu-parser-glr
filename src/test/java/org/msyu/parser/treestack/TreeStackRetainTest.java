package org.msyu.parser.treestack;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;

public class TreeStackRetainTest {

	private TreeStack<Integer> stack;

	@BeforeMethod
	public void beforeMethod() throws Exception {
		stack = new TreeStack<>();
	}

	@Test
	public void retainAll() {
		// given
		Object id1 = stack.push(null, singletonList(1).iterator());
		Object id4 = stack.push(id1, asList(2, 3, 4).iterator());
		Object id5 = stack.push(id1, asList(2, 5).iterator());
		// when
		stack.retain(asList(id1, id4, id5));
		// then
		assertThat(stack.branchById.keySet(), hasItems(id1, id4, id5));
	}

	@Test
	public void dropTail() {
		// given
		Object id1 = stack.push(null, singletonList(1).iterator());
		Object id2 = stack.push(id1, singletonList(2).iterator());
		// when
		stack.retain(singleton(id1));
		// then
		assertThat(stack.branchById.keySet(), not(hasItems(id2)));
	}

	@Test
	public void dropLongTail() {
		// given
		Object id1 = stack.push(null, singletonList(1).iterator());
		Object id2 = stack.push(id1, singletonList(2).iterator());
		Object id3 = stack.push(id2, singletonList(3).iterator());
		// when
		stack.retain(singleton(id1));
		// then
		assertThat(stack.branchById.keySet(), not(hasItem(id2)));
		assertThat(stack.branchById.keySet(), not(hasItem(id3)));
	}

	@Test
	public void dropSplitTail() {
		// given
		Object id1 = stack.push(null, singletonList(1).iterator());
		Object id2 = stack.push(id1, singletonList(2).iterator());
		Object id3 = stack.push(id2, singletonList(3).iterator());
		Object id4 = stack.push(id2, singletonList(4).iterator());
		// when
		stack.retain(singleton(id1));
		// then
		assertThat(stack.branchById.keySet(), not(hasItem(id2)));
		assertThat(stack.branchById.keySet(), not(hasItem(id3)));
		assertThat(stack.branchById.keySet(), not(hasItem(id4)));
	}

	@Test
	public void doesNotDropWithinTail() {
		// given
		Object id1 = stack.push(null, singletonList(1).iterator());
		Object id2 = stack.push(id1, singletonList(2).iterator());
		Object id3 = stack.push(id2, singletonList(3).iterator());
		// when
		stack.retain(singleton(id2));
		// then
		assertThat(stack.branchById.keySet(), hasItems(id1, id2));
		assertThat(stack.branchById.keySet(), not(hasItem(id3)));
	}

}
