package org.strangeforest.test.jmx.model;

import javax.management.*;
import javax.management.modelmbean.*;

public abstract class ModelMBeanInfoProvider {

	public static ModelMBeanInfo getModelMBeanInfo() throws MBeanException {
		Descriptor countDesc = new DescriptorSupport();
		countDesc.setField("name", "Count");
		countDesc.setField("descriptorType", "attribute");
		countDesc.setField("getMethod", "getCount");
		countDesc.setField("setMethod", "setCount");
		return new ModelMBeanInfoSupport(
			TestModel.class.getName(),
			null,
			new ModelMBeanAttributeInfo[] {
				new ModelMBeanAttributeInfo("Count", "int", null, true, true, false, countDesc)
			},
			null,
			new ModelMBeanOperationInfo[] {
				new ModelMBeanOperationInfo("getCount", null, null, "int", MBeanOperationInfo.INFO),
				new ModelMBeanOperationInfo("setCount", null, new MBeanParameterInfo[] {new MBeanParameterInfo("count", "int", null)}, "void", MBeanOperationInfo.ACTION),
				new ModelMBeanOperationInfo("inc", null, null, "void", MBeanOperationInfo.ACTION),
				new ModelMBeanOperationInfo("dec", null, null, "void", MBeanOperationInfo.ACTION)
			},
			null
		);
	}
}
