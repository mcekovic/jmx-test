package org.strangeforest.test.jmx.model;

public class TestModel {

	private int count;

	public synchronized int getCount() {
		return count;
	}

	public synchronized void setCount(int count) {
		this.count = count;
	}

	public synchronized void inc() {
		count++;
	}

	public synchronized void dec() {
		count--;
	}
}
