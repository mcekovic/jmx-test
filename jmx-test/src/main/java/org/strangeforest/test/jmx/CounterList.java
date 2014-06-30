package org.strangeforest.test.jmx;

import java.util.*;

public class CounterList {

	private List<Counter> counters;

	public CounterList(Counter... counters) {
		this.counters = Arrays.asList(counters);
	}

	public List<Counter> getCounters() {
		return counters;
	}

	public void setCounters(List<Counter> counters) {
		this.counters = counters;
	}

	public int getTotal() {
		int total = 0;
		for (Counter counter : counters)
			total += counter.getCount();
		return total;
	}
}
