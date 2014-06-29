package org.strangeforest.test.jmx.mx;

import javax.management.*;

@MXBean
public interface CountersMBean {

	CounterList getCounters();
	int getTotal();
}
