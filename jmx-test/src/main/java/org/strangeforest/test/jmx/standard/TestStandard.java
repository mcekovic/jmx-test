package org.strangeforest.test.jmx.standard;

public class TestStandard implements TestStandardMBean {

	private int count;

	@Override public synchronized int getCount() {
		return count;
	}

	@Override public synchronized void setCount(int count) {
		this.count = count;
	}

	@Override public synchronized void inc() {
		count++;
	}

	@Override public synchronized void dec() {
		count--;
	}
}
