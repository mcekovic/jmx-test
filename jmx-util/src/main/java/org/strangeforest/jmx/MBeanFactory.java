package org.strangeforest.jmx;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;
import javax.management.*;

import org.strangeforest.jmx.annotation.*;

import static org.strangeforest.jmx.MBeanUtil.*;

/**
 * <p>Factory of DynamicMBeans. Users can use this object directly to create DynamicMBeans and then registering them
 * with any MBeanServer. However, the preferred approach is to use the {@link JMXUtil} class (which internally uses
 * this class).</p>
 */
public final class MBeanFactory {

	/**
	 * Hide public constructor.
	 */
	private MBeanFactory() {
	}

	/**
	 * Creates a DynamicMBean from an object annotated with {@link javax.annotation.ManagedBean} exposing all methods and attributes
	 * annotated with {@link ManagedOperation} and {@link ManagedAttribute} respectively.
	 *
	 * @param object the object from which we are creating the DynamicMBean.
	 * @return a constructed DynamicMBean object that can be registered with any MBeanServer.
	 */
	public static DynamicMBean createMBean(Object object) {
		Objects.requireNonNull(object, "No object specified.");
		Class<?> objectType = object.getClass();
		String description = objectType.isAnnotationPresent(MBeanDescription.class) ? objectType.getAnnotation(MBeanDescription.class).value() : "";

		// build attributes and operations
		Method[] methods = objectType.getMethods();
		MethodHandler methodHandler = new MBeanFactory().new MethodHandler(objectType);
		for (Method method : methods)
			methodHandler.handleMethod(method);

		// build the MBeanInfo
		MBeanInfo mBeanInfo = new MBeanInfo(objectType.getName(), description, methodHandler.getMBeanAttributes(),
			new MBeanConstructorInfo[0], methodHandler.getMBeanOperations(), new MBeanNotificationInfo[0]);

		// create the MBean
		return new MBeanImpl(object, mBeanInfo);

	}

	/**
	 * This class is used internally to handle the methods of the object that the
	 * {@link MBeanFactory#createMBean(Object)} receives as an argument. It creates a collection of MBeanAttributeInfo
	 * and MBeanOperationInfo from the information of the methods that it handles.
	 */
	private class MethodHandler {

		private Class<?> objectType;
		private Collection<MBeanAttributeInfo> mBeanAttributes = new ArrayList<>();
		private Collection<MBeanOperationInfo> mBeanOperations = new ArrayList<>();

		/**
		 * Constructor. Initializes the object with the specified class.
		 *
		 * @param objectType the class of the object that the MBeanFactory is handling.
		 */
		public MethodHandler(Class<?> objectType) {
			this.objectType = objectType;
		}

		/**
		 * Called once for each method of the object that the {@link MBeanFactory#createMBean(Object)} receives as an
		 * argument. If the method is annotated with {@link ManagedAttribute} it will try to create a
		 * MBeanAttributeInfo. If the method is annotated with {@link ManagedOperation} it will create a
		 * MBeanOperationInfo. Otherwise, it will do nothing with the method.
		 *
		 * @param method the method we are handling.
		 * @throws ManagementException wraps anyting that could go wrong.
		 */
		public void handleMethod(Method method) throws ManagementException {
			boolean hasManagedAttribute = method.isAnnotationPresent(ManagedAttribute.class);
			boolean hasManagedOperation = method.isAnnotationPresent(ManagedOperation.class);
			if (hasManagedAttribute && hasManagedOperation)
				throw new ManagementException("Method " + method.getName() + " cannot have both ManagedAttribute and " +	"ManagedOperation annotations.");
			if (hasManagedAttribute)
				handleManagedAttribute(method);
			if (hasManagedOperation)
				handleManagedOperation(method);
		}

		/**
		 * Called after the {@link #handleMethod(Method)} is called for all the methods of the <code>objectType</code>.
		 * Retrieves the exposed attributes.
		 *
		 * @return an array of initialized MBeanAttributeInfo objects. It will never return null.
		 */
		public MBeanAttributeInfo[] getMBeanAttributes() {
			return mBeanAttributes.toArray(new MBeanAttributeInfo[mBeanAttributes.size()]);
		}

		/**
		 * Called after the {@link #handleMethod(Method)} is called for all the methods of the <code>objectType</code>.
		 * Retrieves the exposed operations.
		 *
		 * @return an array of initialized MBeanOperationInfo objects. It will never return null.
		 */
		public MBeanOperationInfo[] getMBeanOperations() {
			return mBeanOperations.toArray(new MBeanOperationInfo[mBeanOperations.size()]);
		}

		private void handleManagedAttribute(Method method) {
			// validate if the method is a getter or setter
			Method getterMethod = isGetterMethod(method) ? method : null;
			Method setterMethod = isSetterMethod(method) ? method : null;

			if (getterMethod == null && setterMethod == null)
				throw new ManagementException("Method " + method.getName() + " is annotated as ManagedAttribute " + "but doesn't looks like a valid getter or setter.");

			// retrieve the attribute name from the method name
			String attributeName = method.getName().startsWith("is") ? decapitalize(method.getName().substring(2)) : decapitalize(method.getName().substring(3));

			// retrieve the attribute type from the setter argument type or the getter return type
			Class<?> attributeType = setterMethod != null ? method.getParameterTypes()[0] : method.getReturnType();

			// find the missing method
			getterMethod = getterMethod == null ? findGetterMethod(objectType, attributeName) : getterMethod;
			setterMethod = setterMethod == null ? findSetterMethod(objectType, attributeName, attributeType) : setterMethod;

			boolean existsAttribute = existsAttribute(mBeanAttributes, attributeName, attributeType);
			if (!existsAttribute) {
				// add the MBeanAttribute to the collection
				MBeanAttributeInfo mBeanAttribute = buildMBeanAttribute(attributeName, attributeType, getterMethod, setterMethod, method);
				if (mBeanAttribute != null)
					mBeanAttributes.add(mBeanAttribute);
			}
			else
				throw new ManagementException("Both getter and setter are annotated for attribute " + attributeName + ". Please remove one of the annotations.");
		}

		private boolean isGetterMethod(Method method) {
			return (method.getName().startsWith("get") || method.getName().startsWith("is"))	&& (!method.getReturnType().equals(Void.TYPE) && method.getParameterTypes().length == 0);
		}

		private boolean isSetterMethod(Method method) {
			return method.getName().startsWith("set") && method.getReturnType().equals(Void.TYPE) && method.getParameterTypes().length == 1;
		}

		private Method findGetterMethod(Class<?> objectType, String attributeName) {
			try {
				return objectType.getMethod("get" + capitalize(attributeName));
			}
			catch (NoSuchMethodException ignored) {}
			try {
				return objectType.getMethod("is" + capitalize(attributeName));
			}
			catch (NoSuchMethodException ignored) {}
			return null;
		}

		private Method findSetterMethod(Class<?> objectType, String name, Class<?> attributeType) {
			try {
				return objectType.getMethod("set" + capitalize(name), attributeType);
			}
			catch (NoSuchMethodException e) {
				return null;
			}
		}

		private boolean existsAttribute(Collection<MBeanAttributeInfo> mBeanAttributes, String attributeName, Class<?> attributeType) {
			for (MBeanAttributeInfo mBeanAttribute : mBeanAttributes) {
				if (mBeanAttribute.getName().equals(attributeName) && mBeanAttribute.getType().equals(attributeType.getName()))
					return true;
			}
			return false;
		}

		private MBeanAttributeInfo buildMBeanAttribute(String attributeName, Class<?> attributeType, Method getterMethod, Method setterMethod, Method annotatedMethod) {
			ManagedAttribute managedAttribute = annotatedMethod.getAnnotation(ManagedAttribute.class);
			boolean readable = managedAttribute.readable() && getterMethod != null;
			boolean writable = managedAttribute.writable() && setterMethod != null;
			boolean isIs = getterMethod != null && getterMethod.getName().startsWith("is");
			if (readable || writable)
				return new MBeanAttributeInfo(attributeName, attributeType.getName(), managedAttribute.description(),	readable, writable, isIs);
			return null;
		}

		private void handleManagedOperation(Method method) {
			MBeanParameterInfo[] mBeanParameters = buildMBeanParameters(method.getParameterTypes(), method.getParameterAnnotations());
			ManagedOperation managedOperation = method.getAnnotation(ManagedOperation.class);
			mBeanOperations.add(new MBeanOperationInfo(method.getName(), managedOperation.description(), mBeanParameters, method.getReturnType().getName(), managedOperation.impact().getCode()));
		}

		private MBeanParameterInfo[] buildMBeanParameters(Class<?>[] paramsTypes, Annotation[][] paramsAnnotations) {
			MBeanParameterInfo[] mBeanParameters = new MBeanParameterInfo[paramsTypes.length];
			for (int i = 0; i < paramsTypes.length; i++)
				mBeanParameters[i] = new MBeanParameterInfo("param" + i, paramsTypes[i].getName(), "");
			return mBeanParameters;
		}
	}
}