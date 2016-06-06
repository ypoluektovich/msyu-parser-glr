package org.msyu.parser.glr.test;

import org.msyu.parser.glr.NonTerminal;
import org.msyu.parser.glr.State;
import org.msyu.parser.glr.Terminal;
import org.testng.annotations.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

public class Example extends ReachTheGoalTestBase {

	Terminal id = gb.addTerminal("id");
	Terminal num = gb.addTerminal("num");
	Terminal times = gb.addTerminal("*");
	Terminal plus = gb.addTerminal("+");
	Terminal eof = gb.addTerminal("eof");

	NonTerminal value = gb.addNonTerminal("Value");
	NonTerminal products = gb.addNonTerminal("Products");
	NonTerminal sums = gb.addNonTerminal("Sums");

	{
		goal = gb.addNonTerminal("Goal");

		gb.addProduction(value, id);
		gb.addProduction(value, num);

		gb.addProduction(products, products, times, value);
		gb.addProduction(products, value);

		gb.addProduction(sums, sums, plus, products);
		gb.addProduction(sums, products);

		goalProduction = gb.addProduction(goal, sums, eof);
	}

	@Test
	public void example() {
		State state = State.initializeFrom(sapling, callback);
		callback.completeIteration(state);

		state = callback.advance(state, id);
		state = callback.advance(state, times);
		state = callback.advance(state, num);
		state = callback.advance(state, plus);
		state = callback.advance(state, num);
		state = callback.advance(state, eof);

		verify(callback).reduce(any(), eq(goalProduction));
	}

}
