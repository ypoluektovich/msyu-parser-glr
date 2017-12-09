package org.msyu.parser.glr.incubator;

import org.testng.annotations.Test;

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static java.util.Arrays.asList;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public class CutoffIteratorTest {

	private Iterator<?> iterator;

	private void expectNext() {
		assertTrue(iterator.hasNext());
		assertNotNull(iterator.next());
	}

	private void expectEnd() {
		assertFalse(iterator.hasNext());
		try {
			iterator.next();
			fail("should have thrown");
		} catch (NoSuchElementException e) {
			// ignore, this is expected
		}
	}

	@Test
	public void realCutoff() {
		iterator = new CutoffIterator<>(asList(1, 2).iterator(), 1);
		expectNext();
		expectEnd();
	}

	@Test
	public void exactCutoff() {
		iterator = new CutoffIterator<>(asList(1, 2).iterator(), 2);
		expectNext();
		expectNext();
		expectEnd();
	}

	@Test
	public void underflow() {
		iterator = new CutoffIterator<>(asList(1, 2).iterator(), 3);
		expectNext();
		expectNext();
		expectEnd();
	}

	@Test
	public void emptyBack() {
		iterator = new CutoffIterator<>(Collections.emptyIterator(), 2);
		expectEnd();
	}

}
