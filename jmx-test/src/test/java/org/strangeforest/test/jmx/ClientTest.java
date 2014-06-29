package org.strangeforest.test.jmx;

import java.io.*;
import javax.management.*;
import javax.management.openmbean.*;
import javax.management.remote.*;

import org.junit.*;
import org.strangeforest.test.jmx.standard.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.strangeforest.test.jmx.Server.*;

public class ClientTest {

	private static MBeanServerConnection conn;
	private static JMXConnector connector;

	private static final int TEST_JMX_PORT = 19999;

	@BeforeClass
	public static void setUp() throws Exception {
		String url = Server.start(TEST_JMX_PORT);
		connector = JMXConnectorFactory.connect(new JMXServiceURL(url), null);
		conn = connector.getMBeanServerConnection();
	}

	@AfterClass
	public static void tearDown() throws IOException {
		if (connector != null)
			connector.close();
		Server.stop();
	}

	@Test
	public void standardMBeanTest() throws JMException {
		TestStandardMBean testStandard = JMX.newMBeanProxy(conn, new ObjectName(TEST_STANDARD_URL), TestStandardMBean.class);
		assertNotNull(testStandard);

		testStandard.setCount(10);
		assertThat(testStandard.getCount(), is(equalTo(10)));

		testStandard.inc();
		testStandard.inc();
		testStandard.dec();
		assertThat(testStandard.getCount(), is(equalTo(11)));
	}

	@Test
	public void dynamicMBeanTest() throws JMException, IOException {
		ObjectName objectName = new ObjectName(TEST_DYNAMIC_URL);
		assertNotNull(conn.getObjectInstance(objectName));

		conn.setAttribute(objectName, new Attribute("Count", 10));
		assertThat((Integer)conn.getAttribute(objectName, "Count"), is(equalTo(10)));

		conn.invoke(objectName, "inc", null, null);
		conn.invoke(objectName, "inc", null, null);
		conn.invoke(objectName, "dec", null, null);
		assertThat((Integer)conn.getAttribute(objectName, "Count"), is(equalTo(11)));
	}

	@Test
	public void modelMBeanTest() throws JMException, IOException {
		ObjectName objectName = new ObjectName(TEST_MODEL_URL);
		assertNotNull(conn.getObjectInstance(objectName));

		conn.setAttribute(objectName, new Attribute("Count", 10));
		assertThat((Integer)conn.getAttribute(objectName, "Count"), is(equalTo(10)));

		conn.invoke(objectName, "inc", null, null);
		conn.invoke(objectName, "inc", null, null);
		conn.invoke(objectName, "dec", null, null);
		assertThat((Integer)conn.getAttribute(objectName, "Count"), is(equalTo(11)));
	}

	@Test
	public void mxBeanTest() throws JMException, IOException {
		ObjectName objectName = new ObjectName(TEST_MX_URL);
		assertNotNull(conn.getObjectInstance(objectName));

		assertThat((Integer)conn.getAttribute(objectName, "Total"), is(equalTo(10)));

		CompositeData counterList = (CompositeData)conn.getAttribute(objectName, "Counters");
		assertNotNull(counterList);
		assertTrue(counterList.containsKey("counters"));
		assertThat((Integer)counterList.get("total"), is(equalTo(10)));

		CompositeData[] counters = (CompositeData[])counterList.get("counters");
		assertThat(counters.length, is(equalTo(3)));
	}
}
