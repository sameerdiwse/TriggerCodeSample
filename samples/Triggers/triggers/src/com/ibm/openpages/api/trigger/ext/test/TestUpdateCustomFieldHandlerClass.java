package com.ibm.openpages.api.trigger.ext.test;

import org.apache.log4j.Logger;

import com.ibm.openpages.api.resource.IField;
import com.ibm.openpages.api.resource.IGRCObject;
import com.ibm.openpages.api.resource.IResource;
import com.ibm.openpages.api.resource.IStringField;
import com.ibm.openpages.api.trigger.events.UpdateResourceEvent;
import com.ibm.openpages.api.trigger.ext.DefaultEventHandler;
import com.openpages.ext.solutions.common.LoggerUtilExtended;

public class TestUpdateCustomFieldHandlerClass extends DefaultEventHandler {
	
	private Logger logger = LoggerUtilExtended.getLogger(TestCreateRuleClass.class.getSimpleName());
	
	@Override
	public boolean handleEvent(UpdateResourceEvent event) {
		this.getAttributes();
		IResource resource = event.getResource();
		IGRCObject igrco = (IGRCObject)resource;
		IField ifield = igrco.getField("OPSS-TestP-Aud:Audit Test Procedure");
		if(ifield instanceof IStringField)
		{
			((IStringField)ifield).setValue("We are at 18-04-2023; It's Tuesday.");
		}
		logger.error("In Update custom field handler class.");
		return true;
	}
}
