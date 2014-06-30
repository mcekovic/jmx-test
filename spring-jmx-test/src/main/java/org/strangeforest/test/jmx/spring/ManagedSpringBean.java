package org.strangeforest.test.jmx.spring;

import java.util.*;

import javax.management.openmbean.*;

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
	public CompositeData getStatisticsAsOpenType() throws OpenDataException {
		return new CompositeDataSupport(
			new CompositeType(Stats.class.getName(), null, new String[] {"name", "count"}, null, new OpenType[] {SimpleType.STRING, SimpleType.INTEGER}),
			new String[] {"name", "count"},
			new Object[] {"counter", count}
		);
	}

	@ManagedAttribute
	public Map<String, Integer> getStatisticsAsMap() {
		return Collections.singletonMap("counter", count);
	}
}
