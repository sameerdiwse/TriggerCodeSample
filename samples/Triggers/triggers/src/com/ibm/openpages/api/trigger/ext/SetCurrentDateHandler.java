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

import java.util.Date;

import com.ibm.openpages.api.resource.ICurrencyField;
import com.ibm.openpages.api.resource.IDateField;
import com.ibm.openpages.api.resource.IGRCObject;
import com.ibm.openpages.api.trigger.events.CreateResourceEvent;
import com.ibm.openpages.api.trigger.events.UpdateResourceEvent;
import com.ibm.openpages.api.trigger.ext.DefaultEventHandler;

/**
 * This GRC trigger event handler sets the value of a date field to the current date. <br/>
 * <br/>
 * <b>Usage:</b> This event handler can be used for following events: <br/>
 *  create.object <br/>
 *  update.object <br/>
 *  This event handler must be executed only in the PRE position. 
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
 * <td>current.date.field</td>
 * <td>&nbsp</td>
 * <td>The field name of a date field</td>
 * <td>Yes</td>
 * <td>&nbsp</td>
 * </tr>
 * </table>
 * The format of a field name should be [FieldGroup]:[FieldName], for example: 'OPSS-Iss:Due Date'.
 */
public class SetCurrentDateHandler extends DefaultEventHandler {

    /**
     * Handler attribute 'current.date.field'.
     */
    protected static final String ATTR_CURRENT_DATE_FIELD = "current.date.field";
    
    /**
     * Handles create resource event.
     * @param event a create resource event to be handled 
     * @return true.
     */
    @Override
    public boolean handleEvent(CreateResourceEvent event) {
        IGRCObject object = (IGRCObject)event.getResource();
        setCurrentDate(object);
        return true;
    }

    /**
     * Handles update resource event.
     * @param event an update resource event to be handled 
     * @return true.
     */
    @Override
    public boolean handleEvent(UpdateResourceEvent event) {
        IGRCObject object = (IGRCObject)event.getResource();
        setCurrentDate(object);
        return true;
    }

    private void setCurrentDate(IGRCObject object) {
        IDateField currentDateField = (IDateField)TriggerHandlerUtil.getField(this, object, ATTR_CURRENT_DATE_FIELD);
        currentDateField.setValue(new Date());
    }
}
