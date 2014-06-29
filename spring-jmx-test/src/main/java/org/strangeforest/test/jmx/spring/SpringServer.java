package org.strangeforest.test.jmx.spring;

import org.springframework.context.*;
import org.springframework.context.event.*;
import org.springframework.context.support.*;

public class SpringServer {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = new GenericXmlApplicationContext("appContext.xml");
		context.registerShutdownHook();
		context.addApplicationListener(SpringServer::onApplicationEvent);
		System.out.println("Spring Application Context started.");
	}

	private static void onApplicationEvent(ApplicationEvent event) {
		if (event instanceof ContextClosedEvent)
			System.out.println("Spring Application Context stopped.");
	}
}
