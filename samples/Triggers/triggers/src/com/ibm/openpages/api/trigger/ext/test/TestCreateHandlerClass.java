package com.ibm.openpages.api.trigger.ext.test;

import org.apache.log4j.Logger;

import com.ibm.openpages.api.resource.IGRCObject;
import com.ibm.openpages.api.resource.IResource;
import com.ibm.openpages.api.resource.util.ResourceUtil;
import com.ibm.openpages.api.trigger.events.CreateResourceEvent;
import com.ibm.openpages.api.trigger.ext.DefaultEventHandler;
import com.openpages.ext.solutions.common.LoggerUtilExtended;

public class TestCreateHandlerClass extends DefaultEventHandler 
{
	private Logger logger = LoggerUtilExtended.getLogger(TestCreateHandlerClass.class.getSimpleName());
	
	@Override
	public boolean handleEvent(CreateResourceEvent event) 
	{
		IResource resource = event.getResource();
		IGRCObject igrco = (IGRCObject)resource;
		igrco.setDescription("Comment from test trigger.");
		ResourceUtil.setFieldValue(null, igrco);
		
		return true;
	}
}
