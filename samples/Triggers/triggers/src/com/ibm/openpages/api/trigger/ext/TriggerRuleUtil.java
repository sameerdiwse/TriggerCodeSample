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

import com.ibm.openpages.api.metadata.ITypeDefinition;
import com.ibm.openpages.api.resource.IGRCObject;

/**
 * Provide some common methods for trigger rules
 */
final class TriggerRuleUtil {

	/**
	 * evaluate if the object is the specific content type
	 */
	static boolean evaluateContentType(IGRCObject object, String contentType) {
		if (contentType == null || contentType.isEmpty()) {
			throw new IllegalArgumentException("Attribute contentType is required.");
		}

		if (object.isFolder()) {
			return false;
		}
		ITypeDefinition def = object.getType();
		if (contentType.trim().equals(def.getName().trim())) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * return specific attribute value.
	 * 
	 * @param rule
	 * @param attributeName
	 * @param defaultValue
	 * @return
	 */
	static String getAttributeValue(final DefaultRule rule, final String attributeName, final String defaultValue) {
		HashMap<String, String> map = rule.getAttributes();
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
	 * 
	 * @param rule
	 * @param attributeName
	 * @return
	 */
	static String getRequiredAttributeValue(final DefaultRule rule, final String attributeName) {
		HashMap<String, String> map = rule.getAttributes();
		if (null == map) {
			throw new IllegalStateException("Attribute '" + attributeName + "' is required.");
		}
		String contentType = map.get(attributeName);
		if (null == contentType || contentType.isEmpty()) {
			throw new IllegalStateException("Attribute '" + attributeName + "' is required.");
		}

		return contentType;
	}
}
