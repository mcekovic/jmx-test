package org.strangeforest.jmx.annotation;

import javax.management.*;

public enum Impact {

	ACTION(MBeanOperationInfo.ACTION),
	INFO(MBeanOperationInfo.INFO),
	ACTION_INFO(MBeanOperationInfo.ACTION_INFO),
	UNKNOWN(MBeanOperationInfo.UNKNOWN);

	private int code;

	private Impact(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}
}
