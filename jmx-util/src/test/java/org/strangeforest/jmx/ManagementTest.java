package org.strangeforest.jmx;

import java.lang.management.*;
import javax.management.*;

import org.junit.*;
import org.strangeforest.jmx.annotation.*;

import static org.junit.Assert.*;

public class ManagementTest {

	@Test
	public void shouldRegisterAndUnregisterAnnotatedObject() throws Exception {
		
		String name = "org.test:type=AnnotatedCounter9w83793";
		
		AnnotatedCounter counter = new AnnotatedCounter();
		JMXUtil.register(counter, name);
		
		MBeanInfo mBeanInfo = ManagementFactory.getPlatformMBeanServer().getMBeanInfo( new ObjectName(name) );
		assertNotNull(mBeanInfo);
		assertEquals(AnnotatedCounter.class.getName(), mBeanInfo.getClassName());
		assertEquals("Annotated", mBeanInfo.getDescription());
		assertEquals(0, mBeanInfo.getConstructors().length);
		assertEquals(1, mBeanInfo.getAttributes().length);
		assertEquals(2, mBeanInfo.getOperations().length);
		
		MBeanAttributeInfo mBeanAttribute = mBeanInfo.getAttributes()[0];
		assertNotNull(mBeanAttribute);
		assertEquals("counter", mBeanAttribute.getName());
		assertEquals("int", mBeanAttribute.getType());
		
		boolean resetCounter = false;
		boolean addCounter = false;
		for (MBeanOperationInfo mBeanOperation : mBeanInfo.getOperations()) {
			
			assertNotNull(mBeanOperation);
			
			if (mBeanOperation.getName().equals("resetCounter")) {
				resetCounter = true;
				assertEquals("void", mBeanOperation.getReturnType());
				assertEquals(0, mBeanOperation.getSignature().length);
			} else if (mBeanOperation.getName().equals("addCounter")) {
				addCounter = true;
				assertEquals("boolean", mBeanOperation.getReturnType());
				assertEquals(1, mBeanOperation.getSignature().length);
			}
			
		}
		
		assertTrue(resetCounter);
		assertTrue(addCounter);

		JMXUtil.unregister(name);
		assertFalse(ManagementFactory.getPlatformMBeanServer().isRegistered(new ObjectName(name)));
		
	}
	
	@Test
	public void shouldInstrumentObject() throws Exception {
		
		String name = "org.test:type=AnnotatedCounter7284746";
		
		AnnotatedCounter counter = new AnnotatedCounter();
		JMXUtil.register(counter, name);
		
		ObjectName on = new ObjectName(name);
		MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
		
		assertEquals(mBeanServer.getAttribute(on, "counter"), 0);
		
		mBeanServer.setAttribute(on, new Attribute("counter", 10));
		assertEquals(mBeanServer.getAttribute(on, "counter"), 10);
		
		mBeanServer.invoke(on, "resetCounter", new Object[0], new String[0]);
		assertEquals(mBeanServer.getAttribute(on, "counter"), 0);
		
		mBeanServer.invoke(on, "addCounter", new Object[] { 20 } , new String[] { "int" });
		assertEquals(mBeanServer.getAttribute(on, "counter"), 20);
		
	}
	
	@Test
	public void shouldInstrumentObjectWithEnum() throws Exception {
		
		String name = "org.test:type=EnumAnnotatedCounter424974";
		
		EnumAnnotatedCounter counter = new EnumAnnotatedCounter();
		JMXUtil.register(counter, name);
		
		ObjectName on = new ObjectName(name);
		MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
	
		assertEquals(mBeanServer.getAttribute(on, "state"), null);
		
		mBeanServer.setAttribute( on, new Attribute("state", EnumAnnotatedCounter.State.STOPPED) );
		assertEquals(mBeanServer.getAttribute(on, "state"), EnumAnnotatedCounter.State.STOPPED);
		
	}
	
	@Test(expected=NullPointerException.class)
	public void shouldNotRegisterNullObject() throws Exception {
		JMXUtil.register(null, "org.test:type=Counter7464789");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void shouldNotRegisterNullName() throws Exception {
		JMXUtil.register( new AnnotatedCounter(), null);
	}
	
	@Test(expected=ManagementException.class)
	public void shouldFailWithMethodAnnotatedAsAttributeAndOperation() throws Exception {
	
		String name = "org.test:type=AnnotatedCounter39645";
		
		WrongAnnotatedCounter counter = new WrongAnnotatedCounter();
		JMXUtil.register(counter, name);
		
	}
	
	@Test
	public void shouldNotAddAttributeIfNotReadableOrWritable() throws Exception {
		
		String name = "org.test:type=AnnotatedCounter3456";
		
		AnnotatedCounterNoAttributes counter = new AnnotatedCounterNoAttributes();
		JMXUtil.register(counter, name);
		
		MBeanInfo mBeanInfo = ManagementFactory.getPlatformMBeanServer().getMBeanInfo( new ObjectName(name) );
		assertNotNull(mBeanInfo);
		assertEquals(mBeanInfo.getAttributes().length, 0);
		
	}
	
	private class AnnotatedCounterNoAttributes {
		
		@ManagedAttribute(readable=false)
		public int getCounter() { return 0; }
		
	}
	
	private class WrongAnnotatedCounter {
		
		@ManagedAttribute
		@ManagedOperation
		public int getCounter() { return 0; }
	}
	
}
