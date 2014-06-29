package org.strangeforest.test.jmx.dynamic;

import java.util.*;
import javax.management.*;

public class TestDynamic implements DynamicMBean {

	private int count;

	@Override public MBeanInfo getMBeanInfo() {
		return new MBeanInfo(
			getClass().getName(),
			null,
			new MBeanAttributeInfo[] {
				new MBeanAttributeInfo("Count", "int", null, true, true, false)
			},
			null,
			new MBeanOperationInfo[] {
				new MBeanOperationInfo("inc", null, null, "void", MBeanOperationInfo.ACTION),
				new MBeanOperationInfo("dec", null, null, "void", MBeanOperationInfo.ACTION)
			},
			null
		);
	}

	@Override public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException {
		switch (attribute) {
			case "Count": return count;
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
		return new AttributeList(Arrays.asList(new Attribute("Count", count)));
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
