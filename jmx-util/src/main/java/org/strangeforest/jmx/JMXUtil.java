package org.strangeforest.jmx;

import java.lang.management.*;
import java.util.*;
import javax.management.*;

import org.strangeforest.jmx.annotation.*;

/**
 * <p>Provides methods to register and unregister objects as JMX MBeans.</p>
 */
public abstract class JMXUtil {

	/**
	 * <p>Registers an object with the specified <code>name</code> in the default <code>MBeanServer</code> (which is
	 * retrieved using the <code>ManagementFactory.getPlatformServer()</code> method).</p>
	 * <p/>
	 * <p>All the public attributes and methods annotated with {@link ManagedAttribute} and {@link ManagedOperation}
	 * of the object's class (and the classes it descends from) will be exposed. The object must be annotated with
	 * {@link javax.annotation.ManagedBean}.</p>
	 *
	 * @param object the object that will be exposed as an MBean.
	 * @param name   the name used to expose the object in the MBeanServer (see
	 *               <a href="http://docs.oracle.com/javase/6/docs/api/javax/management/ObjectName.html">
	 *               http://docs.oracle.com/javase/6/docs/api/javax/management/ObjectName.html</a> for more information).
	 * @throws InstanceAlreadyExistsException if the MBean is already registered.
	 * @throws ManagementException            if there is a problem creating or registering the MBean.
	 */
	public static void register(Object object, String name) throws InstanceAlreadyExistsException, ManagementException {
		Objects.requireNonNull(object, "No object specified.");
		checkName(name);
		MBeanServer mBeanServer = getMBeanServer();
		DynamicMBean mBean = MBeanFactory.createMBean(object);
		try {
			mBeanServer.registerMBean(mBean, new ObjectName(name));
		}
		catch (InstanceAlreadyExistsException ex) {
			throw ex;
		}
		catch (Exception ex) {
			throw new ManagementException(ex);
		}
	}

	/**
	 * <p>Unregisters an MBean with the specified <code>name</code> if it exists in the default
	 * <code>MBeanServer</code> (which is retrieved using the <code>ManagementFactory.getPlatformServer()</code>
	 * method).</p>
	 *
	 * @param name the name with which the MBean was registered.
	 * @throws ManagementException wraps any unexpected exception unregistering the MBean.
	 */
	public static void unregister(String name) throws ManagementException {
		checkName(name);
		MBeanServer mBeanServer = getMBeanServer();
		try {
			mBeanServer.unregisterMBean(new ObjectName(name));
		}
		catch (InstanceNotFoundException ignored) {}
		catch (Exception ex) {
			throw new ManagementException(ex);
		}
	}

	public boolean isRegistered(String name) throws ManagementException {
		checkName(name);
		MBeanServer mBeanServer = getMBeanServer();
		try {
			return mBeanServer.isRegistered(new ObjectName(name));
		}
		catch (Exception e) {
			throw new ManagementException(e);
		}
	}

	private static void checkName(String name) {
		if (name == null || name.length() == 0)
			throw new IllegalArgumentException("No name specified.");
	}

	private static MBeanServer getMBeanServer() {
		MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
		if (mBeanServer == null)
			throw new ManagementException("No MBeanServer found.");
		return mBeanServer;
	}
}
