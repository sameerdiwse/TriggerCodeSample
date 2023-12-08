package com.ibm.openpages.api.trigger.ext.test;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.ibm.openpages.api.resource.IGRCObject;
import com.ibm.openpages.api.trigger.events.UpdateResourceEvent;
import com.ibm.openpages.api.trigger.ext.DefaultRule;
import com.openpages.ext.solutions.common.LoggerUtilExtended;

public class TestUpdateCustomFieldRuleClass extends DefaultRule {
	
	private Logger logger = LoggerUtilExtended.getLogger(TestCreateRuleClass.class.getSimpleName());
	
	@Override
	public boolean isApplicable(UpdateResourceEvent event) {
		String objectType = "SOXTest";
//		Here we'll pass Object Data Type Dynamically.
//		HashMap<String, String> hm = this.getAttributes();
//		String attributeName = hm.get("testplancontent");
		
		logger.error("In TestRule Class.");
		String contentType = ((IGRCObject)event.getResource()).getType().getName();
		logger.error("Object Type is : "+ contentType);
		
		if(contentType.equals(objectType))
		{
			return true;
		}
		return false;
	}
}
