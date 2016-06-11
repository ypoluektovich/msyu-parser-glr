package org.msyu.parser.treestack;

import org.hamcrest.Matchers;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyIterator;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;

public class TreeStackPushTest {

	@Test
	public void addNothingToEmptyStack() {
		// given
		TreeStack<Integer> stack = new TreeStack<>();
		// when
		Object result = stack.push(null, emptyIterator());
		// then
		assertNull(result);
	}

	@Test
	public void addFirstElement() {
		// given
		TreeStack<Integer> stack = new TreeStack<>();
		// when
		List<Integer> addedElements = singletonList(1);
		Object one = stack.push(null, addedElements.iterator());
		// then
		assertNotNull(one);
		assertThat(stack.branchById.keySet(), is(singleton(one)));
		assertThat(stack.branchById.get(one).elements, is(addedElements));
	}

	@Test
	public void addFirstElements() {
		// given
		TreeStack<Integer> stack = new TreeStack<>();
		// when
		List<Integer> addedElements = asList(1, 2);
		Object one = stack.push(null, addedElements.iterator());
		// then
		assertNotNull(one);
		assertThat(stack.branchById.keySet(), is(singleton(one)));
		assertThat(stack.branchById.get(one).elements, is(addedElements));
	}

	@Test(dependsOnMethods = "addFirstElement")
	public void addNothingToLeafBranch() {
		// given
		TreeStack<Integer> stack = new TreeStack<>();
		Object branch = stack.push(null, singletonList(1).iterator());
		// when
		Object result = stack.push(branch, emptyIterator());
		// then
		assertSame(result, branch);
	}

	@Test(dependsOnMethods = "addFirstElement")
	public void addSequentially() {
		// given
		TreeStack<Integer> stack = new TreeStack<>();
		List<Integer> oneElements = singletonList(1);
		Object one = stack.push(null, oneElements.iterator());
		// when
		List<Integer> twoElements = singletonList(2);
		Object two = stack.push(one, twoElements.iterator());
		// then
		assertNotNull(one);
		assertNotNull(two);
		assertThat(stack.branchById.keySet(), Matchers.<Set<Object>>allOf(hasSize(2), hasItems(one, two)));
		Branch<Integer> branch1 = stack.branchById.get(one);
		assertThat(branch1.elements, is(oneElements));
		Branch<Integer> branch2 = stack.branchById.get(two);
		assertThat(branch2.elements, is(twoElements));
		assertNull(branch1.parent);
		assertSame(branch2.parent, branch1);
	}

	@Test(dependsOnMethods = "addSequentially")
	public void splitSequentially() {
		// given
		TreeStack<Integer> stack = new TreeStack<>();
		List<Integer> oneElements = singletonList(1);
		Object one = stack.push(null, oneElements.iterator());
		// when
		List<Integer> twoElements = singletonList(2);
		Object two = stack.push(one, twoElements.iterator());
		List<Integer> threeElements = singletonList(3);
		Object three = stack.push(one, threeElements.iterator());
		// then
		assertNotNull(one);
		assertNotNull(two);
		assertNotNull(three);
		assertThat(stack.branchById.keySet(), Matchers.<Set<Object>>allOf(hasSize(3), hasItems(one, two, three)));
		Branch<Integer> branch1 = stack.branchById.get(one);
		assertThat(branch1.elements, is(oneElements));
		Branch<Integer> branch2 = stack.branchById.get(two);
		assertThat(branch2.elements, is(twoElements));
		Branch<Integer> branch3 = stack.branchById.get(three);
		assertThat(branch3.elements, is(threeElements));
		assertNull(branch1.parent);
		assertSame(branch2.parent, branch1);
		assertSame(branch3.parent, branch1);
	}

	@Test(dependsOnMethods = "splitSequentially")
	public void addNothingToInnerBranch() {
		// given
		TreeStack<Integer> stack = new TreeStack<>();
		Object branch = stack.push(null, singletonList(1).iterator());
		Object leaf1 = stack.push(branch, singletonList(2).iterator());
		Object leaf2 = stack.push(branch, singletonList(3).iterator());
		// when
		Object result = stack.push(branch, emptyIterator());
		// then
		assertSame(result, branch);
	}

	@Test(dependsOnMethods = "addFirstElement")
	public void addFirstElementTwice() {
		// given
		TreeStack<Integer> stack = new TreeStack<>();
		// when
		List<Integer> addedElements = singletonList(1);
		Object one = stack.push(null, addedElements.iterator());
		Object two = stack.push(null, addedElements.iterator());
		// then
		assertNotNull(one);
		assertSame(two, one);
		assertThat(stack.branchById.keySet(), is(singleton(one)));
		assertThat(stack.branchById.get(one).elements, is(addedElements));
	}

	@Test(dependsOnMethods = "addFirstElements")
	public void addFirstElementsTwice() {
		// given
		TreeStack<Integer> stack = new TreeStack<>();
		// when
		List<Integer> addedElements = asList(1, 2);
		Object one = stack.push(null, addedElements.iterator());
		Object two = stack.push(null, addedElements.iterator());
		// then
		assertNotNull(one);
		assertSame(two, one);
		assertThat(stack.branchById.keySet(), is(singleton(one)));
		assertThat(stack.branchById.get(one).elements, is(addedElements));
	}

	@Test(dependsOnMethods = "addSequentially")
	public void addWithGrowth() {
		// given
		TreeStack<Integer> stack = new TreeStack<>();
		List<Integer> oneElements = singletonList(1);
		Object one = stack.push(null, oneElements.iterator());
		List<Integer> twoElements = singletonList(2);
		Object two = stack.push(one, twoElements.iterator());
		// when
		List<Integer> elements23 = singletonList(3);
		Object id23 = stack.push(one, elements23.iterator());
		// then
		assertNotNull(elements23);
		assertThat(stack.branchById.keySet(), Matchers.<Set<Object>>allOf(hasSize(3), hasItems(one, two, id23)));
		Branch<Integer> branch1 = stack.branchById.get(one);
		assertThat(branch1.elements, is(oneElements));
		assertNull(branch1.parent);
		Branch<Integer> branch2 = stack.branchById.get(two);
		assertThat(branch2.elements, is(twoElements));
		assertSame(branch2.parent, branch1);
		Branch<Integer> branch3 = stack.branchById.get(id23);
		assertThat(branch3.elements, is(singletonList(3)));
		assertSame(branch3.parent, branch2);
	}

}
