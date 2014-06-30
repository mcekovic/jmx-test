package org.strangeforest.jmx;

import java.lang.reflect.*;
import javax.management.*;

import static org.strangeforest.jmx.MBeanUtil.*;

/**
 * This is the DynamicMBean implementation that is returned from the {@link MBeanFactory#createMBean(Object)} method.
 */
@MXBean
public class MBeanImpl implements DynamicMBean {

	private Object object;
	private MBeanInfo mBeanInfo;

	public MBeanImpl(Object object, MBeanInfo mBeanInfo) {
		this.object = object;
		this.mBeanInfo = mBeanInfo;
	}

	@Override public Object getAttribute(String attributeName) throws AttributeNotFoundException, MBeanException, ReflectionException {
		if (attributeName == null)
			throw new RuntimeOperationsException(new IllegalArgumentException("Attribute name cannot be null"), "Cannot invoke a getter of " + mBeanInfo.getClassName() + " with null attribute name");
		MBeanAttributeInfo mBeanAttribute = findMBeanAttribute(attributeName);
		if (mBeanAttribute == null)
			throw new AttributeNotFoundException("Cannot find " + attributeName + " attribute in " + mBeanInfo.getClassName());
		Method getterMethod = findGetterMethod(mBeanAttribute);
		if (getterMethod == null)
			throw new AttributeNotFoundException("Cannot find " + attributeName + " attribute or equivalent getter in " + mBeanInfo.getClassName());
		try {
			return getterMethod.invoke(object);
		}
		catch (Exception e) {
			throw new MBeanException(e);
		}
	}

	@Override public AttributeList getAttributes(String[] attributesNames) {
		if (attributesNames == null)
			throw new RuntimeOperationsException(new IllegalArgumentException("attributeNames[] cannot be null"), "Cannot invoke a getter of " + mBeanInfo.getClassName());
		AttributeList resultList = new AttributeList();
		if (attributesNames.length == 0)
			return resultList;
		for (String attributesName : attributesNames) {
			try {
				Object value = getAttribute(attributesName);
				resultList.add(new Attribute(attributesName, value));
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return resultList;
	}

	@Override public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
		if (attribute == null)
			throw new RuntimeOperationsException(new IllegalArgumentException("Attribute cannot be null"), "Cannot invoke a setter of " + mBeanInfo.getClassName() + " with null attribute");
		String attributeName = attribute.getName();
		Object value = attribute.getValue();
		if (attributeName == null)
			throw new RuntimeOperationsException(new IllegalArgumentException("Attribute name cannot be null"), "Cannot invoke the setter of " + mBeanInfo.getClassName() + " with null attribute name");
		if (value == null)
			throw new InvalidAttributeValueException("Cannot set attribute " + attributeName + " to null");
		MBeanAttributeInfo mBeanAttribute = findMBeanAttribute(attributeName);
		if (mBeanAttribute == null)
			throw new AttributeNotFoundException("Cannot find " + attributeName + " attribute in " + mBeanInfo.getClassName());

		try {
			Class<?> type = findClass(mBeanAttribute.getType());
			if (!isAssignable(type, value.getClass()))
				throw new InvalidAttributeValueException("Cannot set attribute " + attributeName + " to a " + value.getClass().getName() + " object, " + type.getName() + " expected");
			Method setterMethod = findSetterMethod(mBeanAttribute, type);
			if (setterMethod == null)
				throw new ManagementException("No setter method for attribute " + attributeName);
			try {
				setterMethod.invoke(object, value);
			}
			catch (Exception e) {
				throw new MBeanException(e);
			}
		}
		catch (ClassNotFoundException e) {
			throw new ManagementException(e);
		}
	}

	@Override public AttributeList setAttributes(AttributeList attributes) {
		if (attributes == null)
			throw new RuntimeOperationsException(new IllegalArgumentException("AttributeList attributes cannot be null"), "Cannot invoke a setter of " + mBeanInfo.getClassName());
		AttributeList resultList = new AttributeList();
		if (attributes.isEmpty())
			return resultList;
		try {
			for (Object attribute : attributes) {
				Attribute attr = (Attribute)attribute;
				setAttribute(attr);
				String name = attr.getName();
				Object value = getAttribute(name);
				resultList.add(new Attribute(name, value));
			}
		}
		catch (Exception e) {
			throw new ManagementException(e);
		}
		return resultList;
	}

	@Override public Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException, ReflectionException {
		if (actionName == null)
			throw new RuntimeOperationsException(new IllegalArgumentException("Operation name cannot be null"), "Cannot invoke a null operation in " + mBeanInfo.getClassName());
		MBeanOperationInfo mBeanOperation = findMBeanOperation(actionName, signature);
		if (mBeanOperation == null)
			throw new ReflectionException(new NoSuchMethodException(actionName), "Cannot find the operation " + actionName + " with specified signature in " + mBeanInfo.getClassName());

		try {
			Method method = object.getClass().getMethod(actionName, getParametersTypes(signature));
			return method.invoke(object, params);
		}
		catch (Exception e) {
			throw new ReflectionException(e);
		}
	}

	private Class<?>[] getParametersTypes(String[] signature) throws ClassNotFoundException {
		if (signature == null)
			return null;
		Class<?>[] paramTypes = new Class<?>[signature.length];
		for (int i = 0; i < signature.length; i++)
			paramTypes[i] = findClass(signature[i]);
		return paramTypes;
	}

	@Override public MBeanInfo getMBeanInfo() {
		return mBeanInfo;
	}

	private MBeanAttributeInfo findMBeanAttribute(String attributeName) {
		MBeanAttributeInfo[] mBeanAttributes = mBeanInfo.getAttributes();
		for (MBeanAttributeInfo mBeanAttribute : mBeanAttributes) {
			if (mBeanAttribute.getName().equals(attributeName))
				return mBeanAttribute;
		}
		return null;
	}

	private MBeanOperationInfo findMBeanOperation(String operationName, String[] receivedSignature) {
		MBeanOperationInfo[] mBeanOperations = mBeanInfo.getOperations();
		for (MBeanOperationInfo mBeanOperation : mBeanOperations) {
			if (mBeanOperation.getName().equals(operationName)
				&& isAssignableSignature(mBeanOperation.getSignature(), receivedSignature)) {
				return mBeanOperation;
			}
		}
		return null;
	}

	private boolean isAssignableSignature(MBeanParameterInfo[] operationSignature, String[] receivedSignature) {
		if (operationSignature == null && receivedSignature == null)
			return true;
		if ((operationSignature == null) || (receivedSignature == null))
			return false;
		if (operationSignature.length != receivedSignature.length)
			return false;

		try {
			for (int i = 0; i < operationSignature.length; i++) {
				Class<?> operationParamClass = findClass(operationSignature[i].getType());
				Class<?> receivedParamClass = findClass(receivedSignature[i]);
				if (!isAssignable(operationParamClass, receivedParamClass))
					return false;
			}
		}
		catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		return true;
	}

	private Method findGetterMethod(MBeanAttributeInfo mBeanAttribute) {
		String prefix = "get";
		if (mBeanAttribute.isIs())
			prefix = "is";
		try {
			return object.getClass().getMethod(prefix + capitalize(mBeanAttribute.getName()));
		}
		catch (NoSuchMethodException e) {
			return null;
		}
	}

	private Method findSetterMethod(MBeanAttributeInfo mBeanAttribute, Class<?> attributeType) {
		try {
			return object.getClass().getMethod("set" + capitalize(mBeanAttribute.getName()), attributeType);
		}
		catch (NoSuchMethodException e) {
			return null;
		}
	}

	private Class<?> findClass(String className) throws ClassNotFoundException {
		if (className == null)
			throw new NullPointerException();
		if (Integer.TYPE.getName().equals(className))
			return Integer.TYPE;
		else if (Byte.TYPE.getName().equals(className))
			return Byte.TYPE;
		else if (Short.TYPE.getName().equals(className))
			return Short.TYPE;
		else if (Long.TYPE.getName().equals(className))
			return Long.TYPE;
		else if (Float.TYPE.getName().equals(className))
			return Float.TYPE;
		else if (Double.TYPE.getName().equals(className))
			return Double.TYPE;
		else if (Boolean.TYPE.getName().equals(className))
			return Boolean.TYPE;
		else if (Character.TYPE.getName().equals(className))
			return Character.TYPE;
		return Class.forName(className);
	}

	private boolean isAssignable(final Class<?> to, final Class<?> from) {
		if (to == null)
			throw new IllegalArgumentException("no to class specified");
		if (from == null)
			throw new IllegalArgumentException("no from class specified");

		Class<?> toClass = to;
		if (toClass.isPrimitive())
			toClass = fromPrimitiveToObject(toClass);

		Class<?> fromClass = from;
		if (fromClass.isPrimitive())
			fromClass = fromPrimitiveToObject(fromClass);

		return toClass.isAssignableFrom(fromClass);
	}

	private Class<?> fromPrimitiveToObject(Class<?> primitive) {
		if (primitive.equals(Integer.TYPE))
			return Integer.class;
		else if (primitive.equals(Byte.TYPE))
			return Byte.class;
		else if (primitive.equals(Short.TYPE))
			return Short.class;
		else if (primitive.equals(Long.TYPE))
			return Long.class;
		else if (primitive.equals(Float.TYPE))
			return Float.class;
		else if (primitive.equals(Double.TYPE))
			return Double.class;
		else if (primitive.equals(Boolean.TYPE))
			return Boolean.class;
		else if (primitive.equals(Character.TYPE))
			return Character.class;
		return primitive;
	}
}
