package org.strangeforest.test.jmx;

import java.io.*;
import java.lang.management.*;
import java.rmi.registry.*;
import java.util.concurrent.*;
import javax.management.*;
import javax.management.modelmbean.*;
import javax.management.remote.*;

import org.strangeforest.test.jmx.dynamic.*;
//import org.strangeforest.test.jmx.gmbal.*;
import org.strangeforest.test.jmx.model.*;
import org.strangeforest.test.jmx.mx.*;
import org.strangeforest.test.jmx.open.*;
import org.strangeforest.test.jmx.standard.*;

//import com.sun.org.glassfish.gmbal.*;

public class Server {

	public static final String TEST_STANDARD_URL = "org.strangeforrest.test:type=TestStandard";
	public static final String TEST_DYNAMIC_URL = "org.strangeforrest.test:type=TestDynamic";
	public static final String TEST_OPEN_URL = "org.strangeforrest.test:type=TestOpen";
	public static final String TEST_MODEL_URL = "org.strangeforrest.test:type=TestModel";
	public static final String TEST_MX_URL = "org.strangeforrest.test:type=TestMX";
	public static final String TEST_GMBAL_URL = "org.strangeforrest.test:type=TestGmbal";

	private static final int JMX_RMI_PORT = 9999;
	private static final String JMX_URL = "service:jmx:rmi:///jndi/rmi://localhost:%d/server";

	private static JMXConnectorServer connector;
	private static int port;

	public static void main(String[] args) throws Exception {
		start(JMX_RMI_PORT);
		System.out.println("JMX Server started.");
		TimeUnit.DAYS.sleep(1L);
	}

	public static String start(int port) throws Exception {
		Server.port = port;

		MBeanServer server = ManagementFactory.getPlatformMBeanServer();

		// Standard MBean
		server.registerMBean(new TestStandard(), new ObjectName(TEST_STANDARD_URL));

		// Dynamic MBean
		server.registerMBean(new TestDynamic(), new ObjectName(TEST_DYNAMIC_URL));

		// Open MBean
		server.registerMBean(new TestOpen(), new ObjectName(TEST_OPEN_URL));

		// Model MBean
		RequiredModelMBean modelMBean = new RequiredModelMBean(ModelMBeanInfoProvider.getModelMBeanInfo());
		modelMBean.setManagedResource(new TestModel(), "ObjectReference");
		server.registerMBean(modelMBean, new ObjectName(TEST_MODEL_URL));

		// MXBean
		server.registerMBean(new CountersMBeanImpl(), new ObjectName(TEST_MX_URL));

		// Gmbal
//		GmbalMBean gmbalMBean = ManagedObjectManagerFactory.createStandalone("Test").registerAtRoot(new TestGmbal(), TEST_GMBAL_URL);
//		server.registerMBean(gmbalMBean, new ObjectName(TEST_GMBAL_URL));

		return startRMIConnectorServer(server);
	}

	public static boolean stop() throws IOException {
		if (connector != null) {
			connector.stop();
			connector = null;
			return true;
		}
		else
			return false;
	}

	private static String startRMIConnectorServer(MBeanServer server) throws IOException {
		LocateRegistry.createRegistry(port);
		String url = String.format(JMX_URL, port);
		connector = JMXConnectorServerFactory.newJMXConnectorServer(new JMXServiceURL(url), null, server);
		connector.start();
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override public void run() {
				try {
					if (Server.stop())
						System.out.println("JMX Server stopped.");
				}
				catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		});
		return url;
	}
}
