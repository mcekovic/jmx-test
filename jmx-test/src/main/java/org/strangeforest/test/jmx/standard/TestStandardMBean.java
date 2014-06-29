package org.strangeforest.test.jmx.standard;

public interface TestStandardMBean {

	int getCount();
	void setCount(int count);

	void inc();
	void dec();
}
