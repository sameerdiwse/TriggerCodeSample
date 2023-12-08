/**
 * IBM Confidential Copyright IBM Corporation 2020 The source code for this program is not published
 * or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S.
 * Copyright Office.
 */

package com.ibm.openpages.ext.custom.trigger.handler;

import java.util.ArrayList;
import java.util.Arrays;
import org.apache.log4j.Logger;
import com.ibm.openpages.api.Context;
import com.ibm.openpages.api.metadata.Id;
import com.ibm.openpages.api.query.IQuery;
import com.ibm.openpages.api.query.IResultSetRow;
import com.ibm.openpages.api.query.ITabularResultSet;
import com.ibm.openpages.api.resource.IField;
import com.ibm.openpages.api.resource.IGRCObject;
import com.ibm.openpages.api.resource.util.ResourceUtil;
import com.ibm.openpages.api.service.ServiceFactory;
import com.ibm.openpages.api.trigger.events.AbstractResourceEvent;
import com.ibm.openpages.api.trigger.events.CreateResourceEvent;
import com.ibm.openpages.api.trigger.events.UpdateResourceEvent;
import com.ibm.openpages.api.trigger.ext.DefaultEventHandler;
import com.ibm.openpages.ext.custom.util.CommonUtils;
import com.openpages.ext.solutions.common.LoggerUtilExtended;

public class AssociateSecondaryParentToVendorHandler extends DefaultEventHandler {

  private static final String IS_ENABLED_TRIGGER_SETTING = "Is Enabled";
  
  private Logger logger =
      LoggerUtilExtended.getLogger(AssociateSecondaryParentToVendorHandler.class.getSimpleName());

  private CommonUtils utils;

  // /OpenPages/Solutions/Custom/Vendor - Associate Secondary Parent Trigger
  private static final String SETTINGS_PATH = "trigger.settingsPath";

  // Error occurred in Associate Secondary Parent To Vendor Trigger. Please contact support team.
  private static final String GENERIC_ERROR_APP_TEXT_KEY =
      "com.posten.vendor.be.secondary.association.trigger.generic.error.message";

  // POS-VRM-Vendor:Business unit 1
  private static final String VENDOR_BUSINESS_ENTITY_FIELD_SETTING = "Vendor Secondary Business Unit Field";
  
  //Trigger Name
  private static final String TRIGGER_NAME = "Associate Secondary Parent To Vendor Trigger";

  Context context = null;
  private IGRCObject vendor = null;
  AbstractResourceEvent event = null;

  
  // CREATE event
  public boolean handleEvent(CreateResourceEvent event) {
    try {
    	
    logger.error(TRIGGER_NAME + "CREATE Event");
      String settingsPathAttr = getAttributes().get(SETTINGS_PATH);
      utils = new CommonUtils(logger, ServiceFactory.getServiceFactory(event.getContext()),
          settingsPathAttr);

      if (!utils.getSettingWithDefaultValueIfNull(IS_ENABLED_TRIGGER_SETTING, "true")
          .equalsIgnoreCase("true")) {
        logger.error(
            "Trigger is disabled in setting - exiting handleEvent(CreateResourceEvent event)");
      } else {
        vendor = (IGRCObject) event.getCreatedResource();
        AssociateSecondaryParentBE();
        utils.saveResorce(vendor);
      }
    } catch (Exception e) {
      logger.error("Error occurred.", e);
      throwException(utils.getAppText(GENERIC_ERROR_APP_TEXT_KEY)
       + (e.getMessage() == null ? "" : "\nMessage: " + e.getMessage())
       + (e.getCause() == null ? "" : "\nCause: " + e.getCause()),
        new ArrayList<Object>(), e, context);
    } finally {
      logger.error("Exiting handleEvent(CreateResourceEvent event)");
    }
    return super.handleEvent(event);
  }

  // UPDATE event
  public boolean handleEvent(UpdateResourceEvent event) {
    try {
    	logger.error(TRIGGER_NAME + "UPDATE Event");
      String settingsPathAttr = getAttributes().get(SETTINGS_PATH);
      utils = new CommonUtils(logger, ServiceFactory.getServiceFactory(event.getContext()),
          settingsPathAttr);

      if (!utils.getSettingWithDefaultValueIfNull(IS_ENABLED_TRIGGER_SETTING, "true")
          .equalsIgnoreCase("true")) {
        logger.error(
            "Trigger is disabled in setting - exiting handleEvent(CreateResourceEvent event)");
      } else {
        vendor = (IGRCObject) event.getResource();
        AssociateSecondaryParentBE();
      }
    } catch (Exception e) {
      logger.error("Error occurred.", e);
       throwException(
       utils.getAppText(GENERIC_ERROR_APP_TEXT_KEY)
       + (e.getMessage() == null ? "" : "\nMessage: " + e.getMessage())
       + (e.getCause() == null ? "" : "\nCause: " + e.getCause()),
       new ArrayList<Object>(), e, context);
    } finally {
      logger.error("Exiting handleEvent(UpdateResourceEvent event)");
    }
    return super.handleEvent(event);
  }

  private void AssociateSecondaryParentBE() throws Exception {
    logger.error("Entered AssociateSecondaryParentBE()");
    
    
    String vendorSecondaryParentField = utils.getSetting(VENDOR_BUSINESS_ENTITY_FIELD_SETTING);
    
    try {
	    
	    //Get value of new secondary parent field and associate it
	    String vendorSecondaryParentValue_New = utils.getFieldValueAsString(vendorSecondaryParentField, vendor);    
	    logger.error("vendorSecondaryParentValue_New: " + vendorSecondaryParentValue_New);
	    
	    if (vendorSecondaryParentValue_New != null && !vendorSecondaryParentValue_New.isEmpty()) {
	    	updateAssociations(vendorSecondaryParentValue_New, false);
	    }
   
	  //Get value of old secondary parent field and disassociate it
	    String vendorSecondaryParentValue_Old = (String) ResourceUtil.getOriginalValueForField(vendorSecondaryParentField, vendor);
	    logger.error("vendorSecondaryParentValue_Old: " + vendorSecondaryParentValue_Old);
	    
	    if (vendorSecondaryParentValue_Old != null && !vendorSecondaryParentValue_Old.isEmpty()) {
	        //Do nothing for old value just continue
	    	updateAssociations(vendorSecondaryParentValue_Old, true);
	      
	      }      	    
    logger.error("SUCCESS: Exiting AssociateSecondaryParentBE()");
    
    }catch(Exception e) {
		logger.error("Still haven't got the BE. The error is: "+e.getMessage());
	}
      
  }
  
  public boolean updateAssociations(String vendorSecondaryParentValue, boolean isDisassociate) {
	  
	  logger.error("Start: updateAssociations(): \n vendorSecondaryParentValue= "+vendorSecondaryParentValue+" \n isDisassociate= "+isDisassociate);
	  boolean result = false; 
	  Id secParentNew_Id =  null ;
	  IGRCObject secondaryParentBE = null;
	  String secParentIdNew = "";
	    
	    if (vendorSecondaryParentValue != null && !vendorSecondaryParentValue.isEmpty()) {
	    	 secParentIdNew = getSecondaryParentId(vendorSecondaryParentValue);
	    }
	   
	    if(secParentIdNew != null || secParentIdNew != "") {
	    	secParentNew_Id =  new Id(secParentIdNew);
	    }
	    
	    try {		
	    		if(secParentNew_Id !=null) {
	    			secondaryParentBE =  utils.getResourceService().getGRCObject(secParentNew_Id);
	    			
		    	    logger.error("Found secondaryParentBE. Id: " + secondaryParentBE.getId() + "  Name: " + secondaryParentBE.getName());
		    	    
		    	    if(isDisassociate) {
		    	    //Call the disassociate method
		    	    utils.getResourceService().dissociate(vendor.getId(), Arrays.asList(secondaryParentBE.getId()),new ArrayList<Id>());
		    	        logger.error("Dis-Associated the Vendor from Old Secondary Parent BE: " + secondaryParentBE.getName() 
		    	        				+ "  having id= " + secondaryParentBE.getId());
		    	        result = true;
		    		}else {
		    			//Call the associate method
			    	    utils.getResourceService().associate(vendor.getId(), Arrays.asList(secondaryParentBE.getId()),new ArrayList<Id>());
			    	        logger.error("Associated the Vendor to Secondary Parent BE: " + secondaryParentBE.getName() 
			    	        				+ "  having id= " + secondaryParentBE.getId());
			    	        result = true;
		    		}
	    	} 
	    logger.error("SUCCESS: Exiting updateAssociations()");
	    
	    }catch(Exception e) {
			logger.error("Still haven't got the BE. The error is: "+e.getMessage());
		}
	  
	  return result;
	  
  }
  
  public String getSecondaryParentId(String vendorSecondaryParentValue) {
	  
	  logger.error("Start: getSecondaryParentId(): \n vendorSecondaryParentValue= "+vendorSecondaryParentValue);
	  String objId = "";
	  //Get the Business entity listed in the Business Entity 1 field
	    
		logger.error("vendorSecondaryParentValue: " + vendorSecondaryParentValue);
	    String relativePath = ResourceUtil.trimPathWhitespace(vendorSecondaryParentValue);
		    if (!relativePath.startsWith("/")) {
		      relativePath = "/" + relativePath; 
		    }
	    		logger.error("FULL path- "+relativePath);
	    		
	    		/* Query to use to fetch the object id
	    		 SELECT [SOXBusEntity].[Resource ID]  FROM [SOXBusEntity] 
				 WHERE [SOXBusEntity].[Location] = 'PATH_OF_BE'
	    		 * 
	    		 */    		
	    		String queryStmt = "SELECT [SOXBusEntity].[Resource ID]  FROM [SOXBusEntity] "
	    				  		  + "WHERE [SOXBusEntity].[Location] = '"+ relativePath +"'";
	    		
	    		logger.error("Starting executeQuery. queryStmt= " + queryStmt);
	    		    		
	    		try {
	    			IQuery query = utils.getQueryService().buildQuery(queryStmt);
	    			logger.error("IQuery not null = "+ (query==null));
	    			// query.setHonorPrimary(true);
	    			ITabularResultSet resultset = query.fetchRows(0);
	    			if (resultset == null) {
	    				logger.error("No query results. Returning");
	    				objId = "";
	    			}
	    			logger.error("Query results return SUCCESS. Proceeding...");
	    		
	    			for (IResultSetRow row : resultset) {
	    				logger.error("Line@160");
	    				for (IField field : row) {
	    					logger.error("Line@162");
	    					//Get the Resource Id
	    					objId = (String) utils.getFieldValueAsObject(field);
	    					logger.error("objId= " + objId);
	    					
	    					}    				
	    				}     			    			
	    			}catch (Exception e) {
		    			logger.error("Error occurred.", e);
//		    			 throwException(
//		    			 utils.getAppText(GENERIC_ERROR_APP_TEXT_KEY)
//		    			 + (e.getMessage() == null ? "" : "\nMessage: " + e.getMessage())
//		    			 + (e.getCause() == null ? "" : "\nCause: " + e.getCause()),
//		    			 new ArrayList<Object>(), e, context);
	    		}
	    		logger.error("Returning the objId= " + objId);
	    		logger.error("Exit: getSecondaryParentId()");
	    		return objId;
  	}
  
 }
