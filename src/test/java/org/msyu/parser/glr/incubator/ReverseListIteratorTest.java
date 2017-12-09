package org.msyu.parser.glr.incubator;

import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;

public class ReverseListIteratorTest {

	@Test
	public void traversal() {
		List<Integer> list = asList(1, 2);
		ListIterator<Integer> fwd = list.listIterator(list.size());
		ListIterator<Integer> rev = new ReverseListIterator<>(fwd, list.size());

		assertIndexes(fwd, rev, 2, 0);
		rev.next();
		assertIndexes(fwd, rev, 1, 1);
		rev.next();
		assertIndexes(fwd, rev, 0, 2);
		rev.previous();
		assertIndexes(fwd, rev, 1, 1);
		rev.previous();
		assertIndexes(fwd, rev, 2, 0);
	}

	private <E> void assertIndexes(
			ListIterator<E> fwd,
			ListIterator<E> rev,
			int expectedFwdIndex,
			int expectedRevIndex
	) {
		assertEquals(fwd.nextIndex(), expectedFwdIndex);
		assertEquals(rev.nextIndex(), expectedRevIndex);
		assertEquals(fwd.previousIndex(), expectedFwdIndex - 1);
		assertEquals(rev.previousIndex(), expectedRevIndex - 1);
	}

	@Test
	public void modification() {
		List<Integer> list = new ArrayList<>(asList(1, 2));
		ListIterator<Integer> fwd = list.listIterator(list.size());
		ListIterator<Integer> rev = new ReverseListIterator<>(fwd, list.size());

		rev.next();
		assertIndexes(fwd, rev, 1, 1);

		rev.add(0);
		assertIndexes(fwd, rev, 1, 2);
		assertEquals(list, asList(1, 0, 2));

		assertEquals(rev.previous().intValue(), 0);
		rev.set(-1);
		assertEquals(list, asList(1, -1, 2));
		rev.remove();
		assertEquals(list, asList(1, 2));
		assertIndexes(fwd, rev, 1, 1);

		assertEquals(rev.next().intValue(), 1);
		rev.remove();
		assertIndexes(fwd, rev, 0, 1);
		assertEquals(list, singletonList(2));
	}

}
