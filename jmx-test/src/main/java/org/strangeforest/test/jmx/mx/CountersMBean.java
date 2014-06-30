package org.strangeforest.test.jmx.mx;

import javax.management.*;

import org.strangeforest.test.jmx.*;

@MXBean
public interface CountersMBean {

	CounterList getCounters();
	int getTotal();
}
