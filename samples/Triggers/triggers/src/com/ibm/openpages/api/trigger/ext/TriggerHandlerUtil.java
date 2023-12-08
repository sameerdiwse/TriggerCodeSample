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

import java.util.HashMap;

import com.ibm.openpages.api.resource.IField;
import com.ibm.openpages.api.resource.IGRCObject;
import com.ibm.openpages.api.trigger.ext.DefaultEventHandler;

/**
 * Provide some common methods for trigger handlers
 */
final class TriggerHandlerUtil {
    
    /**
     * return specific attribute value.
     * @param handler
     * @param attributeName
     * @param defaultValue
     * @return
     */
    static String getAttributeValue(final DefaultEventHandler handler, final String attributeName, final String defaultValue) {
        HashMap<String, String> map = handler.getAttributes();
        if (null == map) {
            return defaultValue;
        }
        
        String contentType = map.get(attributeName);
        if (null == contentType) {
            return defaultValue;
        }
        
        return contentType;
    }

    /**
     * return specific attribute value, throw error if the value is not provided.  
     * @param handler
     * @param attributeName
     * @return
     */
    static String getRequiredAttributeValue(final DefaultEventHandler handler, final String attributeName) {
        HashMap<String, String> map = handler.getAttributes();
        if (null == map) {
            throw new IllegalStateException("Attribute '" + attributeName + "' is required.");
        }
        String contentType = map.get(attributeName);
        if (null == contentType || contentType.isEmpty()) {
            throw new IllegalStateException("Attribute '" + attributeName + "' is required.");
        }
        
        return contentType;
    }
    
    /**
     * return the specific field.
     * @param handler
     * @param object
     * @param attributeName
     * @return
     */
    static IField getField(final DefaultEventHandler handler,
            final IGRCObject object, 
            final String attributeName) {
        
        return object.getField(getRequiredAttributeValue(handler, attributeName));
    }
}
