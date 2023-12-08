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

import com.ibm.openpages.api.Context;
import com.ibm.openpages.api.metadata.Id;
import com.ibm.openpages.api.resource.IGRCObject;
import com.ibm.openpages.api.resource.IResource;
import com.ibm.openpages.api.service.IResourceService;
import com.ibm.openpages.api.service.IServiceFactory;
import com.ibm.openpages.api.service.ServiceFactory;
import com.ibm.openpages.api.trigger.TriggerPositionType;
import com.ibm.openpages.api.trigger.events.AbstractEvent;
import com.ibm.openpages.api.trigger.events.AssociateResourceEvent;
import com.ibm.openpages.api.trigger.events.CopyResourceEvent;
import com.ibm.openpages.api.trigger.events.CreateResourceEvent;
import com.ibm.openpages.api.trigger.events.DeleteResourceEvent;
import com.ibm.openpages.api.trigger.events.DisassociateResourceEvent;
import com.ibm.openpages.api.trigger.events.UpdateResourceEvent;

/**
 * This GRC trigger rule checks if the object type of the operating object matches the specific value. <br/>
 * <br/>
 * <b>Usage:</b> This rule can be used for following events: <br/>
 *  create.object <br/>
 *  update.object <br/>
 *  delete.objects <br/>
 *  associate.objects <br/>
 *  disassociate.objects <br/>
 *  copy.object <br/>
 *  copy.objects <br/>
 *  This rule can be executed in the PRE or POST position, but the rule must be executed only in the PRE position for delete.objects event. 
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
 * <td>check.on</td>
 * <td>parent - Check on the parent only. This applies to associate.objects or disassociate.objects events only. <br>
 * child - Check on the child only. This applies to associate.objects or disassociate.objects events only.<br>
 * source - Check on the source only. This applies to copy.object or copy.objects events only.<br>
 * destination - Check on the destination only. This applies to copy.object or copy.objects events only.</td>
 * <td>Determines the scope of the search. E.g. <attribute name="check.on" value="parent"/>. <br>
 * This applies to associate.objects, disassociate.objects, copy.object or copy.objects events only.
 * <td>No</td>
 * <td>parent - This applies to associate.objects or disassociate.objects events only.<br>
 * source - Check on the source only. This applies to copy.object or copy.objects events only.
 * </td>
 * </tr>
 * </table>
 * <br>
 * Example usage in _trigger_config_.xml:<br>
 * <code><pre>
 *   &lt;rule class="com.ibm.openpages.api.trigger.ext.ContentTypeMatchRule" >
 *      &lt;attribute name="content.type" value="LossEvent"/>
 *   &lt;/rule>
 * </pre></code>
 */
public class ContentTypeMatchRule extends DefaultRule {

    private enum AssociateCheckOnType {
        Parent,
        Child
    }
    private enum CopyCheckOnType {
        Source,
        Destination
    }
    
    /**
     * Rule attribute 'content.type'.
     * This is a required attributes.
     */
    protected static final String ATTR_CONTENT_TYPE = "content.type";
    
    /**
     * Rule attribute 'check.on'.
     * This applies to associate.objects, disassociate.objects, copy.object or copy.objects events only.
     */
    protected static final String ATTR_CHECK_ON = "check.on";

    /**
     * Rule attribute 'check.on' value 'parent'.
     * This applies to associate.objects or disassociate.objects events only.
     */
    protected static final String CHECK_ON_PARENT = "parent";

    /**
     * Rule attribute 'check.on' value 'child'.
     * This applies to associate.objects or disassociate.objects events only.
     */
    protected static final String CHECK_ON_CHILD = "child";  
    
    /**
     * Rule attribute 'check.on' value 'source'.
     * This applies to copy.object or copy.objects events only.
     */
    protected static final String CHECK_ON_SOURCE = "source"; // default
    
    /**
     * Rule attribute 'check.on' value 'destination'.
     * This applies to copy.object or copy.objects events only.
     */
    protected static final String CHECK_ON_DESTINATION = "destination";

    private String getAttributeValue(final String attributeName, 
            final String defaultValue) {
        return TriggerRuleUtil.getAttributeValue(this, attributeName, defaultValue);
    }
    
    private String getRequiredAttributeValue(String attributeName) {
        return TriggerRuleUtil.getRequiredAttributeValue(this, attributeName);
    }
    
    /**
     * Get attribute check.on for associated / disassociate
     * @return
     */
    private AssociateCheckOnType getAssociateCheckOnType() {
        String checkOn = getAttributeValue(ATTR_CHECK_ON, CHECK_ON_CHILD);
        
        if (checkOn.equalsIgnoreCase(CHECK_ON_PARENT)) {
            return AssociateCheckOnType.Parent;
        } else if (checkOn.equalsIgnoreCase(CHECK_ON_CHILD)) {
            return AssociateCheckOnType.Child;
        }
        
        throw new IllegalStateException("The value of attribute '" + ATTR_CHECK_ON + "' is invalid.");
    }
    
    /**
     * Get attribute check.on for copy
     * @return
     */
    private CopyCheckOnType getCopyCheckOnType() {
        String checkOn = getAttributeValue(ATTR_CHECK_ON, CHECK_ON_SOURCE);
        
        if (checkOn.equalsIgnoreCase(CHECK_ON_SOURCE)) {
            return CopyCheckOnType.Source;
        } else if (checkOn.equalsIgnoreCase(CHECK_ON_DESTINATION)) {
            return CopyCheckOnType.Destination;
        }
        
        throw new IllegalStateException("The value of attribute '" + ATTR_CHECK_ON + "' is invalid.");
    }
    
    /**
     * Evaluate if the specific object matches the rule
     * @param object
     * @param contentType
     * @return
     */
    private boolean evaluate(IGRCObject object, String contentType) {
        return TriggerRuleUtil.evaluateContentType(object, contentType);
    }

    /**
     * Evaluate a list of object id.
     * @param event
     * @param objectIds
     * @param contentType
     * @return
     */
    private boolean evaluateIdList(AbstractEvent event,
            List<Id> objectIds,
            String contentType) {
        Context context = event.getContext();
        IServiceFactory serviceFactory = ServiceFactory.getServiceFactory(context);
        IResourceService resourceService = serviceFactory.createResourceService();
        
        boolean flag = false;
        for (Id objectId : objectIds) {
            IGRCObject object = resourceService.getGRCObject(objectId);
            flag = evaluate(object, contentType);
            if (flag) {
                return true;
            }
        }

        return flag;
    }
    
    /**
     * Check if the create resource event should be applied.
     * @param event a create resource event to be checked. 
     * @return true if the event should be applied, otherwise return false.
     */
    @Override
    public boolean isApplicable(CreateResourceEvent event) {
        
        String contentType = getRequiredAttributeValue(ATTR_CONTENT_TYPE);
        IResource resource = event.getResource();
        if (resource.isFolder()) {
            return false;
        }
        return evaluate((IGRCObject) resource, contentType);
    }

    /**
     * Check if the update resource event should be applied.
     * @param event an update resource event to be checked. 
     * @return true if the event should be applied, otherwise return false.
     */
    @Override
    public boolean isApplicable(UpdateResourceEvent event) {
        
        String contentType = getRequiredAttributeValue(ATTR_CONTENT_TYPE);
        IResource resource = event.getResource();
        if (resource.isFolder()) {
            return false;
        }
        return evaluate((IGRCObject) resource, contentType);
    }

    /**
     * Check if the associate resource event should be applied.
     * @param event an associate resource event to be checked. 
     * @return true if the event should be applied, otherwise return false.
     */
    @Override
    public boolean isApplicable(AssociateResourceEvent event) {

        String contentType = getRequiredAttributeValue(ATTR_CONTENT_TYPE);
        AssociateCheckOnType checkOn = getAssociateCheckOnType();
        
        List<Id> objectIds = null;
        
        if (checkOn == AssociateCheckOnType.Parent) {
            objectIds = event.getParents();
        }
        else {
            objectIds = event.getChildren();
        }

        return evaluateIdList(event, objectIds, contentType);
    }

    /**
     * Check if the disassociate resource event should be applied.
     * @param event a dissasociate resource event to be checked. 
     * @return true if the event should be applied, otherwise return false.
     */
    @Override
    public boolean isApplicable(DisassociateResourceEvent event) {
        String contentType = getRequiredAttributeValue(ATTR_CONTENT_TYPE);
        AssociateCheckOnType checkOn = getAssociateCheckOnType();
        
        List<Id> objectIds = null;
        
        if (checkOn == AssociateCheckOnType.Parent) {
            objectIds = event.getParents();
        }
        else {
            objectIds = event.getChildren();
        }

        return evaluateIdList(event, objectIds, contentType);
    }

    /**
     * Check if the copy resource event should be applied.
     * @param event a copy resource event to be checked. 
     * @return true if the event should be applied, otherwise return false.
     */
    @Override
    public boolean isApplicable(CopyResourceEvent event) {

        String contentType = getRequiredAttributeValue(ATTR_CONTENT_TYPE);
        CopyCheckOnType checkOn = getCopyCheckOnType();
        
        IGRCObject object= null;
        
        Context context = event.getContext();
        IServiceFactory serviceFactory = ServiceFactory.getServiceFactory(context);
        IResourceService resourceService = serviceFactory.createResourceService();
        
        if (checkOn == CopyCheckOnType.Source) {
            Id sourceId = event.getSourceResouceId();
            object = resourceService.getGRCObject(sourceId);
        }
        else {
            Id targetId = event.getTargetResourceId();
            object = resourceService.getGRCObject(targetId);
        }

        return evaluate(object, contentType);            
    }

    /**
     * Check if the delete resource event should be applied.
     * @param event a delete resource event to be checked. 
     * @return true if the event should be applied, otherwise return false.
     */
    @Override
    public boolean isApplicable(DeleteResourceEvent event) {

        if (event.getPosition() == TriggerPositionType.POST) {
            // cannot evaluate resource information in the post position.
            return false;
        }
        
        String contentType = getRequiredAttributeValue(ATTR_CONTENT_TYPE);
        List<Id> objectIds = event.getResourceIds();
        return evaluateIdList(event, objectIds, contentType);
    }
}