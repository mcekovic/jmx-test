package org.strangeforest.jmx;

public abstract class MBeanUtil {

	public static String capitalize(String string) {
		return Character.toUpperCase(string.charAt(0)) + string.substring(1);
	}

	public static String decapitalize(String string) {
		return Character.toLowerCase(string.charAt(0)) + string.substring(1);
	}
}
