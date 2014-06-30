package org.strangeforest.test.jmx.gmbal;

import com.sun.org.glassfish.gmbal.*;

@ManagedObject
public class TestGmbal {

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
}
