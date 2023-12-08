/*******************************************************************************
 * Licensed Materials - Property of IBM
 *
 * OpenPages GRC Platform (PID: 5725-D51)
 *
 * (c) Copyright IBM Corporation 2015 - 2020. All Rights Reserved.
 *  
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has been
 * deposited with the U.S. Copyright Office.
 *******************************************************************************/
package com.ibm.openpages.api.trigger.ext;

import java.util.List;

import org.slf4j.Logger;

import com.ibm.openpages.api.metadata.ITypeDefinition;
import com.ibm.openpages.api.resource.IField;
import com.ibm.openpages.api.resource.IGRCObject;
import com.ibm.openpages.api.resource.IResource;
import com.ibm.openpages.api.resource.util.ResourceUtil;
import com.ibm.openpages.api.trigger.events.CreateResourceEvent;
import com.ibm.openpages.api.trigger.events.UpdateResourceEvent;
import com.ibm.openpages.api.trigger.ext.DefaultRule;
import com.ibm.openpages.api.logging.LoggerFactory;

/**
 * This GRC trigger rule detects when a specified list of fields of 
 * a GRC object have changed their value 
 * during a create.object or update.object event. <br/>
 * <br/>
 * <b>Usage:</b> This rule can be used for following events: <br/>
 *  create.object <br/>
 *  update.object <br/>
 *  This rule can be executed in the PRE or POST position. 
 * <br/>
 * <br/>
 * <b>Attributes:</b>
 * <table width="100%" border="1" cellpadding="3" cellspacing="0">
 * <tr bgcolor="#CCCCFF" class="TableHeadingColor">
 * <th>Attribute</th>
 * <th>Values</th>
 * <th>Description</th>
 * <th>Required</th>
 * <th>Default</th>
 * </tr>
 * <tr>
 * <td>content.type</td>
 * <td>&nbsp</td>
 * <td>The object type name of the object. E.g. &lt;attribute name="content.type" value="SOXIssue"/&gt;</td>
 * <td>Yes</td>
 * <td>&nbsp</td>
 * </tr>
 * <tr>
 * <td>fields</td>
 * <td>&nbsp</td>
 * <td>Determines the set of fields to detect changes.
 * A comma-separated list of field names. E.g. &lt;attribute name="fields" value= "OPSS-Iss:Assignee,OPSS-Iss:Due Date"/&gt;</td>
 * <td>Yes</td>
 * <td>&nbsp</td>
 * </tr>
 * <tr>
 * <td>check.for</td>
 * <td>all - All fields will be checked for changes. <br>
 * any - Checks whether any one field was changed.</td>
 * <td>Determines the scope of the search. E.g. &lt;attribute name="check.for" value="all"/&gt;</td>
 * <td>No</td>
 * <td>all</td>
 * </tr>
 * </table>
 * The format of a field name should be [FieldGroup]:[FieldName], for example: 'OPSS-Iss:Due Date'.
 */
public class DetectPropertyChangeRule extends DefaultRule {

    private enum CheckForType {
        All,
        Any
    }
    
    /**
     * Rule attribute 'fields'.
     * This is a required attributes.
     */
   protected static final String ATTR_FIELDS = "fields";

   /**
    * Rule attribute 'content.type'.
    * This is a required attributes.
    */
   protected static final String ATTR_CONTENT_TYPE = "content.type";
   
   /**
    * Rule attribute 'check.for'.
    */
   protected static final String ATTR_CHECK_FOR = "check.for";

   /**
    * Rule attribute 'check.for' value 'all'
    */
   protected static final String CHECK_FOR_ALL = "all";  // default
   
   /**
    * Rule attribute 'check.for' value 'any'
    */
   protected static final String CHECK_FOR_ANY = "any";
   
   private Logger logger = LoggerFactory.getLoggerFactory().getSimpleLogger();
   
   private String getAttributeValue(final String attributeName, 
           final String defaultValue) {
       return TriggerRuleUtil.getAttributeValue(this, attributeName, defaultValue);
   }
   
   private String getRequiredAttributeValue(final String attributeName) {
       return TriggerRuleUtil.getRequiredAttributeValue(this, attributeName);
   }
   
   /**
    * Get attribute rule.check.for
    * @return
    */
   private CheckForType getRuleCheckFor() {
       String ruleCheckFor = getAttributeValue(ATTR_CHECK_FOR, CHECK_FOR_ALL);
       
       if (ruleCheckFor.equalsIgnoreCase(CHECK_FOR_ANY)) {
           return CheckForType.Any;
       } else if (ruleCheckFor.equalsIgnoreCase(CHECK_FOR_ALL)) {
           return CheckForType.All;
       }
       
       throw new IllegalStateException("The value of attribute '" + ATTR_CHECK_FOR + "' is invalid.");
   }

   private boolean evaluate(IGRCObject object) {
       logger.debug("DetectPropertyChangeRule:evaluate");

       String contentType = getRequiredAttributeValue(ATTR_CONTENT_TYPE);
       String fields = getRequiredAttributeValue(ATTR_FIELDS);
       CheckForType checkFor = getRuleCheckFor();

       boolean returnValForAny = false;
       boolean returnValForAll = true;

       String[] fieldNames = fields.split(",");
       logger.debug("fields: {}", fields);

       ITypeDefinition def = object.getType();
       
       if (def.getName().equals(contentType)) {
           try {
               List<IField> modifiedFields = ResourceUtil.getModifiedFields(object);

               for (String field : fieldNames) {
                   logger.debug("---------------field: {}", field);
                   boolean flag = false;
                   for (IField modifiedField : modifiedFields) {
                       logger.debug("Modified field: {}", modifiedField.getName());
                       if (modifiedField.getName().equals(field)) {
                           if (checkFor == CheckForType.Any) {
                               returnValForAny = true;
                               break;
                           }
                           else if (checkFor == CheckForType.All) {
                               flag = true;
                               break;
                           }
                       }

                   }
                   if ((checkFor == CheckForType.All) && (!flag)) {
                       returnValForAll = false;
                       break;
                   }
               }
           }
           catch (Exception exp) {
               logger.error("Exception occoured during evaluation, return false",exp);
               return false;
           }
           
           logger.debug("returning :");
           if (checkFor == CheckForType.All) {
               return returnValForAll;
           }
           else if (checkFor == CheckForType.Any) {
               return returnValForAny;
           }
           else {
               return false;
           }

       }
       else {
           logger.debug("Contenttype do not match, return false");
           return false;
       }
   }

   /**
    * Check if the create resource event should be applied.
    * @param event a create resource event to be checked. 
    * @return true if the event should be applied, otherwise return false.
    */
   @Override
   public boolean isApplicable(CreateResourceEvent event) {
       logger.debug("DetectPropertyChangeRule:isApplicable, Create");

       IResource resource = event.getResource();

       if (resource.isFolder()) {
           return false;
       }
       else {
           return evaluate((IGRCObject) resource);
       }
   }

   /**
    * Check if the update resource event should be applied.
    * @param event an update resource event to be checked. 
    * @return true if the event should be applied, otherwise return false.
    */
   @Override
   public boolean isApplicable(UpdateResourceEvent event) {
       logger.debug("DetectPropertyChangeRule:isApplicable, Update");
       IResource resource = event.getResource();

       if (resource.isFolder()) {
           return false;
       }
       else {
           return evaluate((IGRCObject) resource);
       }
   }
}
