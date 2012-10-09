package se.six.jmeter.visualizer.statagg;

import junit.framework.TestCase;

public class Test extends TestCase {
	StatAggCalculator calc;

	public Test(String name) {
		super(name);
	}

	public void setUp() {
		calc = new StatAggCalculator();
	}

	public void testCalculation() {
		calc.addValue(18);
		calc.addValue(10);
		calc.addValue(9);
		calc.addValue(11);
		calc.addValue(28);
		calc.addValue(3);
		calc.addValue(30);
		calc.addValue(15);
		calc.addValue(15);
		calc.addValue(21);
		assertEquals(16, (int) calc.getMean());
		assertEquals(8.0622577F, (float) calc.getStandardDeviation(), 0F);
		assertEquals(30, calc.getMax().intValue());
		assertEquals(3, calc.getMin().intValue());
		assertEquals(15, calc.getMedian().intValue());
	}
}