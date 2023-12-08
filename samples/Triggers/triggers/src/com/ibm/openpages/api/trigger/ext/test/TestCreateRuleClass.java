package com.ibm.openpages.api.trigger.ext.test;

import org.apache.log4j.Logger;

import com.ibm.openpages.api.resource.IGRCObject;
import com.ibm.openpages.api.trigger.events.CreateResourceEvent;
import com.ibm.openpages.api.trigger.ext.DefaultRule;
import com.openpages.ext.solutions.common.LoggerUtilExtended;

public class TestCreateRuleClass extends DefaultRule 
{
	private Logger logger = LoggerUtilExtended.getLogger(TestCreateRuleClass.class.getSimpleName());
	
	@Override
	public boolean isApplicable(CreateResourceEvent event) 
	{
//		String objectType = "SOXProcess";
		String objectType = "SOXRisk";
		logger.error("In TestRule Class.");
//		IResource resource = event.getResource();
//		IGRCObject igrco = (IGRCObject)resource;
//		ITypeDefinition itd = igrco.getType();
//		String contentType =  itd.getName();
		String contentType = ((IGRCObject)event.getResource()).getType().getName();
		logger.error("Object Type is : "+ contentType);
		if(contentType.equals(objectType))
		{
			return true;
		}
		logger.error("Condition not matched.");
			return false;
	}
}
