package com.ibm.openpages.api.trigger.ext.snowint;

import org.apache.log4j.Logger;

import com.ibm.openpages.api.resource.IGRCObject;
import com.ibm.openpages.api.trigger.events.CreateResourceEvent;
import com.ibm.openpages.api.trigger.ext.DefaultRule;
import com.ibm.openpages.api.trigger.ext.test.TestCreateRuleClass;
import com.openpages.ext.solutions.common.LoggerUtilExtended;

public class SnowIntRuleClass extends DefaultRule 
{

	private Logger logger = LoggerUtilExtended.getLogger(TestCreateRuleClass.class.getSimpleName());
	
	@Override
	public boolean isApplicable(CreateResourceEvent event) 
	{
		String objectType = "Resource";
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
