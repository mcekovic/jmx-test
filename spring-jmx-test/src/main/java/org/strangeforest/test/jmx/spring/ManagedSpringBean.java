package org.strangeforest.test.jmx.spring;

import java.util.*;

import org.springframework.jmx.export.annotation.*;

@ManagedResource
public class ManagedSpringBean {

	private int count;

	@ManagedAttribute
	public synchronized int getCount() {
		return count;
	}

	@ManagedAttribute
	public synchronized void setCount(int count) {
		this.count = count;
	}

	@ManagedOperation
	public synchronized void inc() {
		count++;
	}

	@ManagedOperation
	public synchronized void dec() {
		count--;
	}

	@ManagedAttribute
	public Stats getStatistics() {
		return new Stats("counter", count);
	}

	@ManagedAttribute
	public Map<String, Integer> getStatistics2() {
		return Collections.singletonMap("counter", count);
	}
}
