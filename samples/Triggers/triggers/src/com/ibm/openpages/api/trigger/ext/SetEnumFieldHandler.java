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

import java.util.ArrayList;
import java.util.List;

import com.ibm.openpages.api.metadata.DataType;
import com.ibm.openpages.api.metadata.IEnumValue;
import com.ibm.openpages.api.metadata.IFieldDefinition;
import com.ibm.openpages.api.metadata.ITypeDefinition;
import com.ibm.openpages.api.resource.IEnumField;
import com.ibm.openpages.api.resource.IField;
import com.ibm.openpages.api.resource.IGRCObject;
import com.ibm.openpages.api.resource.IMultiEnumField;
import com.ibm.openpages.api.trigger.events.AbstractEvent;
import com.ibm.openpages.api.trigger.events.CreateResourceEvent;
import com.ibm.openpages.api.trigger.events.UpdateResourceEvent;

/**
 * This GRC trigger event handler sets the value of an enumerate field 
 * to a specific value defined in the attributes. <br/>
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
 * <td>enum.field</td>
 * <td>&nbsp</td>
 * <td>The field name of an enumerate field. E.g. &lt;attribute name="enum.field" value="OPSS-Iss:Issue Type"/>.</td>
 * <td>Yes</td>
 * <td>&nbsp</td>
 * </tr>
 * <tr>
 * <td>set.value</td>
 * <td>&nbsp</td>
 * <td>The system name for the enumerate value. E.g. &lt;attribute name="set.value" value="Scoping"/>.</td>
 * <td>Yes</td>
 * <td>&nbsp</td>
 * </tr>
 * </table>
 * The format of a field name should be [FieldGroup]:[FieldName], for example: 'OPSS-Iss:Issue Type'.
 */
public class SetEnumFieldHandler extends DefaultEventHandler {
    
    /**
     * Handler attribute 'enum.field'.
     */
    protected static final String ATTR_ENUM_FIELD = "enum.field";

    /**
     * Handler attribute 'set.value'.
     */
    protected static final String ATTR_SET_VALUE = "set.value";
    
    /**
     * Handles create resource event.
     * @param event a create resource event to be handled 
     * @return true.
     */
    @Override
    public boolean handleEvent(CreateResourceEvent event) {
        IGRCObject object = (IGRCObject)event.getResource();
        setEnumValue(object, event);
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
        setEnumValue(object, event);
        return true;
    }

    private void setEnumValue(IGRCObject object,
            AbstractEvent event) {
        IField enumField = TriggerHandlerUtil.getField(this, object, ATTR_ENUM_FIELD);

        ITypeDefinition def = object.getType();
        IFieldDefinition fieldDef = def.getField(TriggerHandlerUtil.getRequiredAttributeValue(this, ATTR_ENUM_FIELD));
        List<IEnumValue> enumValues =  fieldDef.getEnumValues();
        
        String setValue = TriggerHandlerUtil.getRequiredAttributeValue(this, ATTR_SET_VALUE);

        for(IEnumValue enumValue : enumValues){
            String name = enumValue.getName();
            if (name.equals(setValue)) {
                setEnumFieldValue(enumField, enumValue);
                return;
            }
        }
        
        throw new IllegalStateException("The value of attribute '" + ATTR_SET_VALUE + "' is invalid.");
    }

    private void setEnumFieldValue(IField enumField, IEnumValue value) {
        if (enumField.getDataType() == DataType.ENUM_TYPE) {
            ((IEnumField)enumField).setEnumValue(value);
        }
        else if (enumField.getDataType() == DataType.MULTI_VALUE_ENUM) {
            List<IEnumValue> values = new ArrayList<IEnumValue>();
            values.add(value);
            ((IMultiEnumField)enumField).setEnumValues(values);
        }
    }
}
