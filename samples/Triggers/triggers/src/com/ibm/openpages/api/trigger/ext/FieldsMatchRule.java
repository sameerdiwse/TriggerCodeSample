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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.ibm.openpages.api.metadata.DataType;
import com.ibm.openpages.api.metadata.IEnumValue;
import com.ibm.openpages.api.metadata.Id;
import com.ibm.openpages.api.resource.IBooleanField;
import com.ibm.openpages.api.resource.ICurrencyField;
import com.ibm.openpages.api.resource.IDateField;
import com.ibm.openpages.api.resource.IEnumField;
import com.ibm.openpages.api.resource.IField;
import com.ibm.openpages.api.resource.IFloatField;
import com.ibm.openpages.api.resource.IGRCObject;
import com.ibm.openpages.api.resource.IIdField;
import com.ibm.openpages.api.resource.IIntegerField;
import com.ibm.openpages.api.resource.IMultiEnumField;
import com.ibm.openpages.api.resource.IResource;
import com.ibm.openpages.api.resource.IStringField;
import com.ibm.openpages.api.service.IResourceService;
import com.ibm.openpages.api.service.ServiceFactory;
import com.ibm.openpages.api.trigger.TriggerPositionType;
import com.ibm.openpages.api.trigger.events.AbstractEvent;
import com.ibm.openpages.api.trigger.events.AssociateResourceEvent;
import com.ibm.openpages.api.trigger.events.CopyResourceEvent;
import com.ibm.openpages.api.trigger.events.CreateResourceEvent;
import com.ibm.openpages.api.trigger.events.DeleteResourceEvent;
import com.ibm.openpages.api.trigger.events.DisassociateResourceEvent;
import com.ibm.openpages.api.trigger.events.UpdateResourceEvent;
import com.ibm.openpages.api.trigger.ext.DefaultRule;

/**
 * This GRC trigger rule checks if values of the operating object match specific values. <br/>
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
 * <td>rule.field.nnn</td>
 * <td>&nbsp</td>
 * <td>The name of field to be compared.</td>
 * <td>Yes</td>
 * <td>&nbsp</td>
 * </tr>
 * <tr>
 * <td>rule.field.value.nnn</td>
 * <td>&nbsp</td>
 * <td>The value of field to be compared. For date data type, the format of value should be in MM/dd/yyyy, e.g. 12/31/2015.</td>
 * <td>Yes</td>
 * <td>&nbsp</td>
 * </tr>
 * <tr>
 * <tr>
 * <td>rule.operator.nnn</td>
 * <td>= - equals to<br>
 * != - not equal to<br>
 * > - greater than<br>
 * >= - greater than or equals to<br>
 * &lt; - less than<br>
 * &lt;= - less than or equals to<br></td>
 * <td>The compared operator.</td>
 * <td>Yes</td>
 * <td>&nbsp</td>
 * </tr>
 * <td>check.for</td>
 * <td>all - Checks whether all fields are matched. <br>
 * any - Checks whether any one field is matched.</td>
 * <td>Determines the scope of the compare. E.g. &lt;attribute name="check.for" value="all"/&gt;</td>
 * <td>No</td>
 * <td>all</td>
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
 *   &lt;rule class="com.ibm.openpages.api.trigger.ext.FieldsMatchRule" >
 *      &lt;attribute name="content.type" value="SOXIssue"/>
 *      &lt;attribute name="rule.field.1" value="OPSS-Iss:Additional Description"/>
 *      &lt;attribute name="rule.field.value.1" value="Test"/>
 *      &lt;attribute name="rule.operator.1" value="="/>
 *      &lt;attribute name="rule.field.2" value="OPSS-Iss:Issue Type"/>
 *      &lt;attribute name="rule.field.value.2" value="Scoping"/>
 *      &lt;attribute name="rule.operator.2" value="="/>
 *      &lt;attribute name="check.for" value="all"/>
 *  &lt;/rule>
 * </pre></code>
 */
public class FieldsMatchRule extends DefaultRule {
    
    private enum AssociateCheckOnType {
        Parent,
        Child
    }
    
    private enum CopyCheckOnType {
        Source,
        Destination
    }
    
    private enum CheckForType {
        All,
        Any
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
    
    /**
     * Rule attribute 'rule.field.' prefix.
     */
    protected static final String ATTR_RULE_FIELD_PREFIX = "rule.field.";
    
    /**
     * Rule attribute 'rule.field.value.' prefix.
     */
    protected static final String ATTR_RULE_FIELD_VALUE_PREFIX = "rule.field.value.";
    
    /**
     * Rule attribute 'rule.operator.' prefix.
     */
    protected static final String ATTR_RULE_OPERATOR_PREFIX = "rule.operator.";
    
    private static final String DEFAULT_DATE_FORMAT = "MM/dd/yyyy";
    private static final String DATE_TODAY = "today";
    
    private static final DataType[] ALLOWED_DATA_TYPES = {     DataType.BOOLEAN_TYPE,
                                                        DataType.CURRENCY_TYPE,
                                                        DataType.DATE_TYPE,
                                                        DataType.ENUM_TYPE,
                                                        DataType.MULTI_VALUE_ENUM,
                                                        DataType.FLOAT_TYPE,
                                                        DataType.INTEGER_TYPE,
                                                        DataType.STRING_TYPE,
                                                        DataType.ID_TYPE
                                                  };
    
    private static final String OPERATOR_EQUAL_TO = "=";
    private static final String OPERATOR_NOT_EQUAL_TO = "!=";
    private static final String OPERATOR_LESS_THAN = "<";
    private static final String OPERATOR_LESS_THAN_EQUAL_TO = "<=";
    private static final String OPERATOR_GREATER_THAN = ">";
    private static final String OPERATOR_GREATER_THAN_EQUAL_TO = ">=";
    
    private static final String[] ALLOWED_OPERATORS = { OPERATOR_EQUAL_TO,
        OPERATOR_NOT_EQUAL_TO, 
        OPERATOR_LESS_THAN, 
        OPERATOR_LESS_THAN_EQUAL_TO, 
        OPERATOR_GREATER_THAN, 
        OPERATOR_GREATER_THAN_EQUAL_TO};
    
    
    /**
     * The class provides evaluate methods for rules.
     */
    private static class RuleEvaluator {

        static boolean evaluate(final IField field, 
                final String operator, 
                final Object inputValue) {
            
            Object fieldData = getFieldValue(field);
            
            if (operator.equals(OPERATOR_EQUAL_TO)) {
                return checkEquals(field, fieldData, inputValue);
            }
            else if (operator.equals(OPERATOR_NOT_EQUAL_TO)) {
                return checkNotEqual(field, fieldData, inputValue);
            }
            else if (operator.equals(OPERATOR_GREATER_THAN)) {
                return checkCreaterThan(field, fieldData, inputValue);
            }
            else if (operator.equals(OPERATOR_LESS_THAN)) {
                return checkLessThan(field, fieldData, inputValue);
            }
            else if (operator.equals(OPERATOR_GREATER_THAN_EQUAL_TO)) {
                return checkCreateThanEqualTo(field, fieldData, inputValue);
            }
            else if (operator.equals(OPERATOR_LESS_THAN_EQUAL_TO)) {
                return checkLessThanEqualTo(field, fieldData, inputValue);                     
            }
            
            return false;
        }
        
        /**
         * Check if these two values are equal.
         * Date Types;
         *  Boolean
         *  Integer
         *  Double
         *  String
         *  Date
         *  Enum
         * @param value1
         * @param value2
         * @return
         */
        @SuppressWarnings("unchecked")
        private static boolean checkEquals(final IField field,
                final Object value1, 
                final Object value2) {
            // check null value
            if (field instanceof IStringField) {
                if (isNullOrEmpty(value1) && isNullOrEmpty(value2)) {
                    return true;
                } else if (isNullOrEmpty(value1) && !isNullOrEmpty(value2) ) {
                    return false;
                } else if (!isNullOrEmpty(value1) && isNullOrEmpty(value2)) {
                    return false;
                }
            } else {
                if (value1 == null && value2 == null) {
                    return true;
                } else if (value1 == null && value2 != null) {
                    return false;
                } else if (value1 != null && value2 == null) {
                    return false;
                }
            }
            
            // check normal values
            if (value1 instanceof Boolean) {
                return ((Boolean)value1).equals((Boolean)value2);
            } else if (value1 instanceof Integer) {
                return ((Integer)value1).equals((Integer)value2);
            } else if (value1 instanceof Double) {
                return ((Double)value1).equals(value2);
            } else if (value1 instanceof Date) {
                Calendar calendar1 = Calendar.getInstance();
                calendar1.setTime((Date)value1);
                Calendar calendar2 = Calendar.getInstance();
                calendar2.setTime((Date)value2);
                return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR)
                        && calendar1.get(Calendar.MONTH) == calendar2.get(Calendar.MONTH)
                        && calendar1.get(Calendar.DAY_OF_MONTH) == calendar2.get(Calendar.DAY_OF_MONTH);
            } else if (value1 instanceof String) {
                return ((String)value1).equals((String)value2);
            } else if (value1 instanceof List<?>) {
                // single enum and multiple enum
                return checkEqualsForMultipleEnum((List<IEnumValue>)value1, (String[])value2);
            }

            return false;
        }
        
        private static boolean checkNotEqual(final IField field,
                final Object value1, 
                final Object value2) {
            return !checkEquals(field, value1, value2);
        }

        private static boolean checkLessThan(final IField field,
                final Object value1, 
                final Object value2) {
            // check null value
            if (field instanceof IStringField) {
                if (isNullOrEmpty(value1) && isNullOrEmpty(value2)) {
                    return false;
                } else if (isNullOrEmpty(value1) && !isNullOrEmpty(value2) ) {
                    return true;
                } else if (!isNullOrEmpty(value1) && isNullOrEmpty(value2)) {
                    return false;
                }
            } else {
                if (value1 == null && value2 == null) {
                return false;
                } else if (value1 == null && value2 != null) {
                     return true;
                } else if (value1 != null && value2 == null) {
                    return false;
                }
            }

            // check normal values
            if (value1 instanceof Integer) {
                return ((Integer)value1).compareTo((Integer)value2) < 0;
            } else if (value1 instanceof Double) {
                return ((Double)value1).compareTo((Double)value2) < 0;
            } else if (value1 instanceof Date) {
                Calendar calendar1 = Calendar.getInstance();
                calendar1.setTime((Date)value1);
                Calendar calendar2 = Calendar.getInstance();
                calendar2.setTime((Date)value2);
                return (calendar1).compareTo(calendar2) < 0;
            } else if (value1 instanceof String) {
                return ((String)value1).compareTo((String)value2) < 0;
            }

            throw new IllegalStateException("Field '" + field.getName() + "' does not support operator '" + OPERATOR_LESS_THAN + "'.");
        }

        private static boolean checkLessThanEqualTo(final IField field,
                final Object value1, 
                final Object value2) {
            return !checkCreaterThan(field, value1, value2);
        }

        private static boolean checkCreaterThan(final IField field,
                final Object value1, 
                final Object value2) {
            // check null value
            if (field instanceof IStringField) {
                if (isNullOrEmpty(value1) && isNullOrEmpty(value2)) {
                    return false;
                } else if (isNullOrEmpty(value1) && !isNullOrEmpty(value2) ) {
                    return false;
                } else if (!isNullOrEmpty(value1) && isNullOrEmpty(value2)) {
                    return true;
                }
            } else {
                if (value1 == null && value2 == null) {
                    return false;
                } else if (value1 == null && value2 != null) {
                     return false;
                } else if (value1 != null && value2 == null) {
                    return true;
                }
            }

            // check normal values
            if (value1 instanceof Integer) {
                return ((Integer)value1).compareTo((Integer)value2) > 0;
            } else if (value1 instanceof Double) {
                return ((Double)value1).compareTo((Double)value2) > 0;
            } else if (value1 instanceof Date) {
                Calendar calendar1 = Calendar.getInstance();
                calendar1.setTime((Date)value1);
                Calendar calendar2 = Calendar.getInstance();
                calendar2.setTime((Date)value2);
                calendar2.add(Calendar.DATE, 1);
                return (calendar1).compareTo(calendar2) >= 0;
            } else if (value1 instanceof String) {
                return ((String)value1).compareTo((String)value2) > 0;
            }

            throw new IllegalStateException("Field '" + field.getName() + "' does not support operator '" + OPERATOR_GREATER_THAN + "'.");
        }
        
        private static boolean checkCreateThanEqualTo(final IField field,
                final Object value1, 
                final Object value2) {
            return !checkLessThan(field, value1, value2);
        }
        
        private static boolean checkEqualsForMultipleEnum(final List<IEnumValue> value1, final String[] value2) {
            
            if (value1.size() != value2.length) {
                return false;
            }
                
            for (int i = 0; i < value2.length; i++) {
                
                boolean hasEnumMatched = false;
                
                for (int j = 0; j < value1.size(); j++) {
                    if (value2[i].equals(value1.get(j).getName())) {
                        hasEnumMatched = true;
                        break;
                    }
                }
                
                if (!hasEnumMatched)
                    return false;
            }
           return true;
        }

        private static Date parseDate(final String value) {
            
            try {
                if (DATE_TODAY.equalsIgnoreCase(value)) {
                    return new Date();
                }
                
                SimpleDateFormat sdf = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
                return sdf.parse(value);
            } catch (ParseException e) {
                throw new IllegalStateException("Value '" + value + "' is not correct date.", e);
            }
        }
        
        private static Object getFieldValue(final IField field) {
            
            if (field.isNull()) {
                return null;
            }
            
            if (field instanceof IStringField) {
                IStringField stringField = (IStringField) field;
                return stringField.getValue();
            }
            else if (field instanceof IBooleanField) {
                IBooleanField booleanField = (IBooleanField) field;
                return booleanField.getValue();
            }
            else if (field instanceof ICurrencyField) {
                ICurrencyField currencyField = (ICurrencyField) field;
                return currencyField.getBaseAmount();
            }
            else if (field instanceof IDateField) {
                IDateField dateField = (IDateField) field;
                return dateField.getValue();
            }
            else if (field instanceof IIntegerField) {
                IIntegerField integerField = (IIntegerField) field;
                return integerField.getValue();
            }
            else if (field instanceof IIdField) {
                IIdField idField = (IIdField) field;
                return Integer.parseInt(idField.getValue().toString());
            }
            else if (field instanceof IFloatField) {
                IFloatField floatField = (IFloatField) field;
                return floatField.getValue();
            }
            else if (field instanceof IEnumField) {
                IEnumField enumField = (IEnumField) field;
                List<IEnumValue> enumValues = new ArrayList<IEnumValue>();
                enumValues.add(enumField.getEnumValue());
                return enumValues;
            }
            else if (field instanceof IMultiEnumField) {
                IMultiEnumField multiEnumField = (IMultiEnumField) field;
                return multiEnumField.getEnumValues();
            }
            
            return null;
        }
        
        static Object getInputFieldValue(final IField field, final String inputValue) {
            
            if (field instanceof IStringField) {
                return inputValue;
            }
            else if (field instanceof IBooleanField) {
                return isNullOrEmpty(inputValue) ? null
                        : Boolean.parseBoolean(inputValue);
            }
            else if (field instanceof ICurrencyField) {
                return isNullOrEmpty(inputValue) ? null
                        : Double.parseDouble(inputValue);
            }
            else if (field instanceof IDateField) {
                return isNullOrEmpty(inputValue) ? null
                        : parseDate(inputValue);
            }
            else if (field instanceof IIntegerField) {
                return isNullOrEmpty(inputValue) ? null
                        : Integer.parseInt(inputValue);
            }
            else if (field instanceof IIdField) {
                return isNullOrEmpty(inputValue) ? null
                        : Integer.parseInt(inputValue);
            }
            else if (field instanceof IFloatField) {
                return isNullOrEmpty(inputValue) ? null
                        : Double.parseDouble(inputValue);
            }
            else if (field instanceof IEnumField) {
                return isNullOrEmpty(inputValue) ? null
                        : parseEnum(inputValue);
            }
            else if (field instanceof IMultiEnumField) {
                return isNullOrEmpty(inputValue) ? null
                        : parseEnum(inputValue);
            }
            
            return null;
        }
        
        private static String[] parseEnum(String inputValue) {
            
            return inputValue.split(",");
        }
     
        private static boolean isNullOrEmpty(Object s) {
            if (s == null) {
                return true;
            }
            
            if (s instanceof String && ((String)s).isEmpty()) {
                return true;
            }
            return false;
        }
    }
    
    private String getAttributeValue(final String attributeName, 
            final String defaultValue) {
        return TriggerRuleUtil.getAttributeValue(this, attributeName, defaultValue);
    }

    private String getRequiredAttributeValue(final String attributeName) {
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
     * Evaluate if the list of ID match the rule.
     * @param event
     * @param Ids
     * @return
     * @throws Exception
     */
    private boolean evaluate(AbstractEvent event, List<Id> Ids)
            {
        IResourceService resourceService = ServiceFactory.getServiceFactory(event.getContext()).createResourceService();
        String contentType = getRequiredAttributeValue(ATTR_CONTENT_TYPE);
        boolean flag = false;
        for (Id id : Ids) {

            IGRCObject object = resourceService.getGRCObject(id);
            flag = evaluate(object, contentType);
            if (!flag) {
                return false;
            }
        }

        return flag;
    }

    /**
     * Evaluate if the id matches the rule.
     * @param event
     * @param id
     * @return
     * @throws Exception
     */
    private boolean evaluate(AbstractEvent event, Id id)
            {
        IResourceService resourceService = ServiceFactory.getServiceFactory(event.getContext()).createResourceService();
        String contentType = getRequiredAttributeValue(ATTR_CONTENT_TYPE);
        IGRCObject object = resourceService.getGRCObject(id);
        return evaluate(object, contentType);
    }
    
    /**
     * Evaluate if the object matches the rule.
     * @param object
     * @param contentType
     * @return
     * @throws Exception
     */
    private boolean evaluate(IGRCObject object, String contentType) {
        
        // validate content type
        if (!TriggerRuleUtil.evaluateContentType(object, contentType)) {
            return false;
        }
        
        // validate rules
        CheckForType checkFor = getRuleCheckFor();
        List<Object[]> ruleList = getRules(object);
        boolean isMatched = false;
        
        for (Object[] rule: ruleList) {
            IField field = (IField)rule[0];
            Object inputValue = rule[1];
            String operator = (String)rule[2];
            isMatched = RuleEvaluator.evaluate(field, operator, inputValue);
            
            if (checkFor == CheckForType.All) {
                if (!isMatched) {
                    return false;
                }
            } else {
                // is check for any
                if (isMatched) {
                    return true;
                }
            }
        }
        
        return isMatched;
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

    /**
     * Return rules.
     * @param object
     * @return
     */
    private List<Object[]> getRules(IGRCObject object) {
        Map<String, String> map = getAttributes();
        Map<String, String> ruleMap = new HashMap<String, String>();
        
        for (Map.Entry<String, String> entry : map.entrySet()) {
            
            String attributeName = entry.getKey();
            String attributeValue = entry.getValue();
            
            if (isRuleFieldAttribute(attributeName))
                ruleMap.put(attributeName, attributeValue);
        }
        
        List<Object[]> conditionList = getRuleList(ruleMap, map, object);

        return conditionList;
    }
    
    private List<Object[]> getRuleList(Map<String, String> ruleMap, 
            Map<String, String> attributes,
            IGRCObject object) {
        
        List<Object[]> ruleList = new ArrayList<Object[]>();
        List<DataType> dataTypeList = Arrays.asList(ALLOWED_DATA_TYPES);
        List<String> operatorList = Arrays.asList(ALLOWED_OPERATORS);
        
        for (Map.Entry<String, String> entry : ruleMap.entrySet()) {
            Object[] rule;
            
            String attributeName = entry.getKey();
            String attributeValue = entry.getValue();
            
            rule = new Object[3];
            
            // Field Name
            IField field = null;
            try {
                field = object.getField(attributeValue);
            } catch (RuntimeException e) {
                throw new IllegalStateException("The value of attribute '" + attributeName + "' is invalid.");
            }
            
            // check field data type
            if (!dataTypeList.contains(field.getDataType())) {
                throw new IllegalStateException("The value of attribute '" + attributeName + "' is invalid.");
            }
            rule[0] = field; 
            
            String ruleIndex = getRuleAttributeIndex(attributeName);
            
            // Field Value
            String fieldValueAttribute = ATTR_RULE_FIELD_VALUE_PREFIX + ruleIndex;
            try {
                rule[1] = RuleEvaluator.getInputFieldValue(field, getRequiredAttributeValue(fieldValueAttribute));
            } catch (RuntimeException e) {
                throw new IllegalStateException("The value of attribute '" + fieldValueAttribute + "' is invalid.", e);
            }
            
            // Operation
            String operatorAttribute = ATTR_RULE_OPERATOR_PREFIX + ruleIndex;
            String operator = getRequiredAttributeValue(operatorAttribute);
            if (!operatorList.contains(operator)) {
                throw new IllegalStateException("The value of attribute '" + operatorAttribute + "' is invalid.");
            }
            validateOperator(field, operator);
            
            rule[2] =  operator;
            
            ruleList.add(rule);
        }
        
        return ruleList;
    }
    
    private void validateOperator(IField field, String operator) {
        if (operator.equals(OPERATOR_LESS_THAN)
                || operator.equals(OPERATOR_LESS_THAN_EQUAL_TO)
                || operator.equals(OPERATOR_GREATER_THAN)
                || operator.equals(OPERATOR_GREATER_THAN_EQUAL_TO)) {
            if (!(field instanceof ICurrencyField
                    || field instanceof IDateField
                    || field instanceof IFloatField
                    || field instanceof IIntegerField
                    || field instanceof IStringField
                    || field instanceof IIdField))
            {
                throw new IllegalStateException("Field '" + field.getName() + "' does not support operator '" + operator + "'.");
            }
        }
    }

    private String getRuleAttributeIndex(String attribute) {
        
        return attribute.replace(ATTR_RULE_FIELD_PREFIX, "");
    }
    
    private boolean isRuleFieldAttribute(String attribute) {
        
        if (attribute.matches(ATTR_RULE_FIELD_PREFIX + "[1-9][0-9]*$")) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Check if the create resource event should be applied.
     * @param event a create resource event to be checked. 
     * @return true if the event should be applied, otherwise return false.
     */
    @Override
    public boolean isApplicable(CreateResourceEvent event) {
        
        IResource resource = event.getResource();
        if (resource.isFolder()) {
            return false;
        }
        String contentType = getRequiredAttributeValue(ATTR_CONTENT_TYPE);
        return evaluate((IGRCObject)resource, contentType);
    }

    /**
     * Check if the update resource event should be applied.
     * @param event an update resource event to be checked. 
     * @return true if the event should be applied, otherwise return false.
     */
    @Override
    public boolean isApplicable(UpdateResourceEvent event) {
        
        IResource resource = event.getResource();
        if (resource.isFolder()) {
            return false;
        }
        String contentType = getRequiredAttributeValue(ATTR_CONTENT_TYPE);
        return evaluate((IGRCObject)resource, contentType);
    }
    
    /**
     * Check if the associate resource event should be applied.
     * @param event an associate resource event to be checked. 
     * @return true if the event should be applied, otherwise return false.
     */
    @Override
    public boolean isApplicable(AssociateResourceEvent event) {
        
        AssociateCheckOnType checkOn = getAssociateCheckOnType();
        List<Id> objectIds = null;
        
        if (checkOn == AssociateCheckOnType.Parent) {
            objectIds = event.getParents();
        }
        else {
            objectIds = event.getChildren();
        }
        
        return evaluate((AbstractEvent)event, objectIds);
    }
    
    /**
     * Check if the disassociate resource event should be applied.
     * @param event a dissasociate resource event to be checked. 
     * @return true if the event should be applied, otherwise return false.
     */
    @Override
    public boolean isApplicable(DisassociateResourceEvent event) {
        
        AssociateCheckOnType checkOn = getAssociateCheckOnType();
        List<Id> objectIds = null;
        
        if (checkOn == AssociateCheckOnType.Parent) {
            objectIds = event.getParents();
        }
        else {
            objectIds = event.getChildren();
        }
        
        return evaluate((AbstractEvent)event, objectIds);
    }

    /**
     * Check if the copy resource event should be applied.
     * @param event a copy resource event to be checked. 
     * @return true if the event should be applied, otherwise return false.
     */
    @Override
    public boolean isApplicable(CopyResourceEvent event) {
        
        CopyCheckOnType checkOn = getCopyCheckOnType();
        
        Id id = null;
        
        if (checkOn == CopyCheckOnType.Source) {
            id = event.getSourceResouceId();
        }
        else {
            id = event.getTargetResourceId();
        }

        return evaluate((AbstractEvent)event, id);
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
    
        List<Id> Ids = event.getResourceIds();
        return evaluate((AbstractEvent)event, Ids);
    }
}
