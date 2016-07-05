package org.msyu.parser.glr.examples;

import org.msyu.parser.glr.NonTerminal;
import org.msyu.parser.glr.Terminal;
import org.msyu.parser.glr.grammartest.LoggingCallback;
import org.msyu.parser.glr.grammartest.ReachTheGoalTestBase;
import org.testng.annotations.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

public class DragonBookExample extends ReachTheGoalTestBase<Terminal> {

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

		callback = new LoggingCallback();
	}

	@Test
	public void example() {
		state = state.advance(id, callback);
		state = state.advance(times, callback);
		state = state.advance(num, callback);
		state = state.advance(plus, callback);
		state = state.advance(num, callback);
		state = state.advance(eof, callback);

		verify(callback).reduce(any(), eq(goalProduction));
	}

}
