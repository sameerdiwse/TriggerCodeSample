package com.ibm.openpages.api.trigger.ext.test;

import org.apache.log4j.Logger;

import com.ibm.openpages.api.resource.IGRCObject;
import com.ibm.openpages.api.resource.IResource;
import com.ibm.openpages.api.resource.util.ResourceUtil;
import com.ibm.openpages.api.trigger.events.UpdateResourceEvent;
import com.ibm.openpages.api.trigger.ext.DefaultEventHandler;
import com.openpages.ext.solutions.common.LoggerUtilExtended;

public class TestUpdateDefaultFieldHandlerClass extends DefaultEventHandler {
	
	private Logger logger = LoggerUtilExtended.getLogger(TestCreateHandlerClass.class.getSimpleName());
	
	@Override
	public boolean handleEvent(UpdateResourceEvent event) {
		IResource resource = event.getResource();
		IGRCObject igrco = (IGRCObject)resource;
//		CommonUtils cu = new CommonUtils(logger, null);
		igrco.setDescription("Comment from test trigger.");
		return true;
	}

}
