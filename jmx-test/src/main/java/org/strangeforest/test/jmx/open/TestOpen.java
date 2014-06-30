package org.strangeforest.test.jmx.open;

import java.util.*;
import javax.management.*;
import javax.management.openmbean.*;

import org.strangeforest.test.jmx.*;

public class TestOpen implements DynamicMBean {

	private int count;
	private final CompositeType counterType;

	public TestOpen() throws OpenDataException {
		counterType = new CompositeType(Counter.class.getName(), "Counter", new String[] {"name", "type", "count"}, new String[] {"Name", "Type", "Count"}, new OpenType[] {SimpleType.STRING, SimpleType.STRING, SimpleType.INTEGER});
	}

	@Override public MBeanInfo getMBeanInfo() {
		return new OpenMBeanInfoSupport(
			getClass().getName(),
			"Test Open",
			new OpenMBeanAttributeInfo[] {
				new OpenMBeanAttributeInfoSupport("Count", "Count", SimpleType.INTEGER, true, true, false),
				new OpenMBeanAttributeInfoSupport("Counter", "Counter", counterType, true, false, false)
			},
			null,
			new OpenMBeanOperationInfoSupport[] {
				new OpenMBeanOperationInfoSupport("inc", "inc", null, SimpleType.VOID, MBeanOperationInfo.ACTION),
				new OpenMBeanOperationInfoSupport("dec", "dec", null, SimpleType.VOID, MBeanOperationInfo.ACTION)
			},
			null
		);
	}

	@Override public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException {
		switch (attribute) {
			case "Count": return getCount();
			case "Counter": return getCounter();
			default: throw new IllegalArgumentException("Unknown attribute: " + attribute);
		}
	}

	@Override public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
		String name = attribute.getName();
		Object value = attribute.getValue();
		switch (name) {
			case "Count": count = (Integer)value; break;
			default: throw new IllegalArgumentException("Unknown attribute: " + name);
		}
	}

	@Override public AttributeList getAttributes(String[] attributes) {
		return new AttributeList(Arrays.asList(
			new Attribute("Count", getCount()),
			new Attribute("Counter", getCounter())
		));
	}

	private int getCount() {
		return count;
	}

	private CompositeDataSupport getCounter() {
		try {
			return new CompositeDataSupport(counterType, new String[] {"name", "type", "count"}, new Object[] {"count", "counter", count});
		}
		catch (OpenDataException ex) {
			JMRuntimeException jmEx = new JMRuntimeException(ex.getMessage());
			jmEx.initCause(ex);
			throw jmEx;
		}
	}

	@Override public AttributeList setAttributes(AttributeList attributes) {
		for (Attribute attribute : attributes.asList()) {
			try {
				setAttribute(attribute);
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return attributes;
	}

	@Override public Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException, ReflectionException {
		switch (actionName) {
			case "inc": count++; return null;
			case "dec": count--; return null;
			default: throw new IllegalArgumentException("Unknown operation: " + actionName);
		}
	}
}
