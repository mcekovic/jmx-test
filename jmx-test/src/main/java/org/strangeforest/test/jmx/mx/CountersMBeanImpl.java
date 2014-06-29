package org.strangeforest.test.jmx.mx;

public class CountersMBeanImpl implements CountersMBean {

	private CounterList counters;

	public CountersMBeanImpl() {
		this(new CounterList(
			new Counter("BMW", "Car", 5),
			new Counter("Mercedes", "Car", 3),
			new Counter("FAP", "Truck", 2)
		));
	}

	public CountersMBeanImpl(CounterList counters) {
		this.counters = counters;
	}

	@Override public CounterList getCounters() {
		return counters;
	}

	@Override public int getTotal() {
		return counters.getTotal();
	}
}
