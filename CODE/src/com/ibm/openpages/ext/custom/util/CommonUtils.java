/**
 * IBM Confidential Copyright IBM Corporation 2021 The source code for this program is not published
 * or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S.
 * Copyright Office.
 */
package com.ibm.openpages.ext.custom.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import com.ibm.openpages.api.application.CognosOutputFormat;
import com.ibm.openpages.api.application.IReportParameters;
import com.ibm.openpages.api.configuration.IConfigProperties;
import com.ibm.openpages.api.metadata.DataType;
import com.ibm.openpages.api.metadata.IEnumValue;
import com.ibm.openpages.api.metadata.IFieldDefinition;
import com.ibm.openpages.api.metadata.IFileTypeDefinition;
import com.ibm.openpages.api.metadata.ITypeDefinition;
import com.ibm.openpages.api.metadata.Id;
import com.ibm.openpages.api.query.IQuery;
import com.ibm.openpages.api.query.IResultSetRow;
import com.ibm.openpages.api.query.ITabularResultSet;
import com.ibm.openpages.api.resource.GRCObjectFilter;
import com.ibm.openpages.api.resource.IBooleanField;
import com.ibm.openpages.api.resource.ICurrencyField;
import com.ibm.openpages.api.resource.IDateField;
import com.ibm.openpages.api.resource.IDocument;
import com.ibm.openpages.api.resource.IEnumAnswer;
import com.ibm.openpages.api.resource.IEnumAnswersField;
import com.ibm.openpages.api.resource.IEnumField;
import com.ibm.openpages.api.resource.IField;
import com.ibm.openpages.api.resource.IFloatField;
import com.ibm.openpages.api.resource.IGRCObject;
import com.ibm.openpages.api.resource.IIdField;
import com.ibm.openpages.api.resource.IIntegerField;
import com.ibm.openpages.api.resource.IMultiEnumField;
import com.ibm.openpages.api.resource.IReferenceField;
import com.ibm.openpages.api.resource.IResourceFactory;
import com.ibm.openpages.api.resource.IStringField;
import com.ibm.openpages.api.resource.IncludeAssociations;
import com.ibm.openpages.api.resource.util.ResourceUtil;
import com.ibm.openpages.api.service.IApplicationService;
import com.ibm.openpages.api.service.IConfigurationService;
import com.ibm.openpages.api.service.IMetaDataService;
import com.ibm.openpages.api.service.IQueryService;
import com.ibm.openpages.api.service.IQuestionnaireService;
import com.ibm.openpages.api.service.IResourceService;
import com.ibm.openpages.api.service.ISecurityService;
import com.ibm.openpages.api.service.IServiceFactory;
import com.openpages.apps.session.OPAppSession;
import com.openpages.aurora.common.AuroraEnv;
import com.openpages.sdk.admin.AdminService;

public class CommonUtils {
  private static final String PDF_FILE_EXTENSION = "PDF";
  public static final SimpleDateFormat DATE_WITH_TIME_FORMAT =
      new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.sss");
  public static String SIMPLE_DATE_FORMAT_STRING = "yyyy-MM-dd";
  public static final SimpleDateFormat SIMPLE_DATE_FORMAT =
      new SimpleDateFormat(SIMPLE_DATE_FORMAT_STRING);

  public static final String RECORD_COLUMN_SEPARATOR = "###@@###";
  public static final String MULTIPLY = "MULTIPLY";
  public static final String DIVIDE = "DIVIDE";
  public static final String SUBTRACT = "SUBTRACT";
  public static final String ADD = "ADD";

  public static final String END_USER_PLACEHOLDER_VALUE = "$END_USER$";

  public static final String OPEN_SQUARE_BRACKET = "[";
  public static final String CLOSED_SQUARE_BRACKET = "]";

  public static final String REGEX_DOUBLE_PATTERN = "-?\\d+(\\.\\d+)?(E-?\\d+)?";
  public static final String REGEX_INTEGER_PATTERN = "-?\\d+";
  public static final String REGEX_UNSIGNED_INTEGER_PATTERN = "\\d+";
  public static final String REGEX_COMMA_PATTERN = "\\s*,\\s*";
  public static final String REGEX_EQUALS_PATTERN = "\\s*=\\s*";

  public static final String APP_TEXT_MARKER_CSV_FIELD_VALUES = "APP_TEXT_";
  private static final String SETTING_MARKER_CSV_FIELD_VALUES = "SETTING_";

  private static final String TODAY_PLACEHOLDER_PATTERN_STR =
      "^\\[?\\s*\\$\\s*TODAY\\s*\\$\\s*\\]?$";

  // e.g. [$TODAY$]-1 -> means yesterday
  // or $TODAY$+50 -> means 50 days from today

  // brackets "[" and "]" are optional
  public static final String DATE_EXPRESSION_RELATIVE_TO_TODAY_PATTERN_STR =
      "^\\s*(\\[?\\s*\\$TODAY\\$\\s*\\]?)\\s*(\\+|-)\\s*(\\d+)\\s*$";
  private static final Pattern DATE_EXPRESSION_RELATIVE_TO_TODAY_PATTERN =
      Pattern.compile(DATE_EXPRESSION_RELATIVE_TO_TODAY_PATTERN_STR);


  private Calendar calendar = Calendar.getInstance();

  // this regex captures 3 groups: left value to be compared
  // delimiter: > or >= or < or <= or == or != or string comparator delimiters:
  // right value to be compared
  // EQUAL TO, LESS_THAN etc.
  public static final String REGEX_OP1_DELIMITER_OP2 =
      "(.*?)((?:(?:>=?|<=?)|!=|==| EQUAL_TO | NOT_EQUAL_TO | LESS_THAN | LESS_THAN_EQUAL_TO | GREATER_THAN | GREATER_THAN_EQUAL_TO ))(.*)";
  public static final String NULL_STR = "null";
  private static final String SOX_DOCUMENT_TYPE_NAME = "SOXDocument";
  private static final String SLASH = "/";

  private Logger logger = null;

  private boolean enableDebugLogs = true;
  private IServiceFactory serviceFactory;
  private IResourceFactory resourceFactory;
  private IConfigurationService configurationService;
  private IQueryService queryService;
  private IResourceService resourceService;
  private IApplicationService applicationService;
  private ISecurityService securityService;
  private IQuestionnaireService questionnaireService;
  private IMetaDataService metaDataService;
  private String settingsRootPath;
  private String objTaskViewUrlPrefix;

  public CommonUtils(Logger logger, IServiceFactory serviceFactory) {
    this.serviceFactory = serviceFactory;
    this.logger = logger;
  }

  public CommonUtils(Logger logger, IServiceFactory serviceFactory, String settingsRootPath) {
    this.serviceFactory = serviceFactory;
    this.settingsRootPath = settingsRootPath;
    this.logger = logger;
    logger.debug("settingsRootPath: " + settingsRootPath);
  }

  public void setDebugLogsEnabled(boolean value) {
    enableDebugLogs = value;
  }


  public void setSettingsRootPath(String settingsRootPath) {
    this.settingsRootPath = settingsRootPath;
  }

  /**
   * Gets the field value as Object
   * 
   * @param field
   * @param logger
   * @return
   */

  public Object getFieldValueAsObject(final IField field) {
    return getFieldValueAsObject(field, false);
  }

  public Object getFieldValueAsObjectWithBaseValueIfCurrencyType(final IField field) {
    return getFieldValueAsObject(field, true);
  }

  public Object getFieldValueAsObject(final IField field, boolean getBaseValueIfCurrencyType) {
    Object value = null;
    if (field != null && !field.isNull()) {
      if (field instanceof IDateField) {
        IDateField dateField = (IDateField) field;
        if (dateField.getValue() != null) {
          value = dateField.getValue();
        }
      } else if (field instanceof IStringField) {
        IStringField stringField = (IStringField) field;
        value = stringField.getValue();
      } else if (field instanceof IEnumField) {
        IEnumField enumField = (IEnumField) field;
        value = enumField.getEnumValue().getName();
      } else if (field instanceof IIdField) {
        IIdField idField = (IIdField) field;
        value = idField.getValue().toString();
      } else if (field instanceof ICurrencyField) {
        ICurrencyField idField = (ICurrencyField) field;
        Object[] currValues = new Object[2];
        if (!getBaseValueIfCurrencyType) {
          currValues[0] = idField.getLocalCurrency();
          currValues[1] = idField.getLocalAmount().toString();
        } else {
          currValues[0] = idField.getBaseCurrency();
          currValues[1] = idField.getBaseAmount().toString();
        }
        value = currValues;
      } else if (field instanceof IFloatField) {
        IFloatField floatField = (IFloatField) field;
        value = floatField.getValue();
      } else if (field instanceof IIntegerField) {
        IIntegerField integerField = (IIntegerField) field;
        value = integerField.getValue();
      }
    }

    debug("getFieldValueAsObject(field name:" + (field != null ? field.getName() : null)
        + ", field type: " + (field != null ? field.getDataType() : null)
        + ", getBaseValueIfCurrencyType: " + getBaseValueIfCurrencyType + ") -> " + value);
    return value;
  }

  /**
   * Gets the field value as Object
   * 
   * @param field
   * @param logger
   * @return
   */
  public Object getFieldValue(String fieldStr, IGRCObject resource) {
    Object fieldValue = null;
    fieldStr = fieldStr.trim();
    if (fieldStr.equalsIgnoreCase("Resource ID")) {
      fieldValue = "" + resource.getId();
    } else if (fieldStr.equalsIgnoreCase("System Fields:Name")
        || fieldStr.equalsIgnoreCase("System Field:Name") || fieldStr.equalsIgnoreCase("Name")) {
      fieldValue = resource.getName();
    } else if (fieldStr.equalsIgnoreCase("System Fields:Description")
        || fieldStr.equalsIgnoreCase("System Field:Description")
        || fieldStr.equalsIgnoreCase("Description")) {
      fieldValue = resource.getDescription();
    } else {
      IField field = resource.getField(fieldStr);
      fieldValue = getFieldValueAsObject(field);
    }
    debug("getFieldValue(field:" + fieldStr + ", resource: named " + resource.getName()
        + ", having ID " + resource.getId() + ") -> " + fieldValue);
    return fieldValue;

  }

  /**
   * Gets the field value as Double for calculation
   * 
   * @param resource
   * @param operandOneInfo
   * @return
   */
  public Double getFieldValueAsDouble(IGRCObject resource, String operandOneInfo) {
    Double theValue = null;
    Double number = isNumber(operandOneInfo);
    if (number != null) {
      theValue = number;
      return theValue;
    }
    IField field = resource.getField(operandOneInfo);

    if (field != null) {
      switch (field.getDataType()) {
        case CURRENCY_TYPE: {
          ICurrencyField source = (ICurrencyField) field;

          theValue = source.getBaseAmount();
          debug("getDoubleFieldValue CURRENCY_TYPE :" + theValue);
          break;
        }
        case FLOAT_TYPE: {
          IFloatField source = (IFloatField) field;

          theValue = source.getValue();
          debug("getDoubleFieldValue FLOAT_TYPE :" + theValue);
          break;
        }
        case INTEGER_TYPE: {
          IIntegerField source = (IIntegerField) field;
          try {
            theValue = Double.parseDouble(source.getValue().toString());
            debug("getDoubleFieldValue INTEGER_TYPE :" + theValue);
          } catch (Exception e) {
            theValue = null;
          }
          break;
        }
        case ENUM_TYPE: {
          IEnumField source = (IEnumField) field;
          try {
            theValue = isNumber(source.getEnumValue().getName());
            debug("getDoubleFieldValue INTEGER_TYPE :" + theValue);
          } catch (Exception e) {
            theValue = null;
          }
          break;
        }
        default:
          debug("Unsupported Data type for calculation " + field.getDataType().name());
          break;
      }
      debug("fieldStr :" + operandOneInfo + " : fieldValue :" + theValue);
    } else {
      error("Field is not defined : " + operandOneInfo);
    }

    return theValue;
  }

  /**
   * Checks if the fieldValue is number
   * 
   * @param operandInfo
   * @return
   */
  private static Double isNumber(String operandInfo) {
    Double fieldValue = null;
    Scanner numberScanner = new Scanner(operandInfo);
    if (numberScanner.hasNextDouble()) {
      fieldValue = numberScanner.nextDouble();
    }
    numberScanner.close();
    return fieldValue;
  }


  public void setFieldValueOnResource(String fieldName, Object valueForField, IGRCObject resource)
      throws Exception {
    setFieldValueOnResource(fieldName, valueForField, resource, getConfigurationService());
  }

  /**
   * Sets the field value on Resource
   * 
   * @param fieldName
   * @param value
   * @param resource
   * @param ics
   * @throws Exception
   */
  @Deprecated
  public void setFieldValueOnResource(String fieldName, Object value, IGRCObject resource,
      IConfigurationService ics) throws Exception {
    IField field = resource.getField(fieldName);
    DataType dataType = field != null ? field.getDataType() : null;
    debug("Entered setFieldValueOnResource(fieldName:" + fieldName + ", valueForField:" + value
        + ", resource: name " + resource.getName() + ", having ID " + resource.getId()
        + "\n Field IS " + (field == null ? "" : "NOT") + " NULL \n Field Data Type: "
        + (field == null ? "can't get it from null field" : dataType));
    if (field == null) {
      debug("Field is null - nothing to do - Exiting setFieldValueOnResource()");
      return;
    }

    if (value == null || value.equals("")) {
      ResourceUtil.setFieldValue(field, null);
      debug("Exiting setFieldValueOnResource() - field value was set to null");
      return;
    }

    switch (dataType) {
      case FLOAT_TYPE:
        if (value instanceof String && ((String) value).matches(REGEX_DOUBLE_PATTERN)) {
          value = Float.valueOf((String) value);
        } else if (value instanceof BigDecimal) {
          value = ((BigDecimal) value).floatValue();
        }
        break;
      case DATE_TYPE:
        if (value instanceof String && ((String) value).trim().matches(SIMPLE_DATE_FORMAT_STRING)) {
          value = (Date) SIMPLE_DATE_FORMAT.parse((String) value);
        }
        break;
      case CURRENCY_TYPE:
        if (value instanceof Object[]) {
          debug("currency field value is array: " + Arrays.toString((Object[]) value));
        }
        // TODO improve this over time if needed
        break;
      case INTEGER_TYPE:
        value = value instanceof String ? Integer.valueOf((String) value) : (Integer) value;
        break;
      default:
        break;
    }

    ResourceUtil.setFieldValue(field, value);
    debug("Exiting setFieldValueOnResource() - field value was set.");
  }

  /**
   * Performs the evaluation of the operator, add, subtract, multiply, divide on Field Values
   * 
   * @param operandOneValue
   * @param operandTwoValue
   * @param operatorInfo
   * @return
   */
  public Double performEvaluation(Double operandOneValue, Double operandTwoValue,
      String operatorInfo) {

    Double resultValue = null;

    if ((operandOneValue != null) && (operandTwoValue != null)) {
      // Apply the operation on 2 operands.
      if (operatorInfo.equalsIgnoreCase(ADD)) {
        resultValue = operandOneValue + operandTwoValue;
        debug("performEvaluation  operator is 'ADD' result=" + resultValue);
      } else if (operatorInfo.equalsIgnoreCase(SUBTRACT)) {
        resultValue = operandOneValue - operandTwoValue;
        debug("performEvaluation  operator is 'SUBTRACT' result=" + resultValue);
      } else if (operatorInfo.equalsIgnoreCase(DIVIDE)) {
        // This is the only case where we need to check an operand value
        if (operandTwoValue.longValue() != 0) {
          BigDecimal divideValue = new BigDecimal(operandOneValue)
              .divide(new BigDecimal(operandTwoValue), 2, RoundingMode.FLOOR);
          debug("performEvaluation  operator is 'DIVIDE' result=" + divideValue);
          resultValue = divideValue.doubleValue();
          debug("performEvaluation  operator is 'DIVIDE' result=" + resultValue);
        } else {
          resultValue = Double.NaN;
          debug("performEvaluation  attempted 'DIVIDE' by ZERO!! gives " + resultValue);
        }
        debug("performEvaluation  operator is 'DIVIDE' result=" + resultValue);
      } else if (operatorInfo.equalsIgnoreCase(MULTIPLY)) {
        resultValue = operandOneValue * operandTwoValue;
        debug("performEvaluation: Test  operator is 'MULTIPLY' result=" + resultValue);
      } else {
        debug("performEvaluation: Test  has an unknown/unimplemented operator! (" + operatorInfo
            + ")");
      }
    } else {
      debug("performEvaluation: Test  One of the operands (" + operandOneValue + "),("
          + operandTwoValue + ") fields has null value)");
    }
    return resultValue;
  }

  /**
   * Performs the evaluation of the operator, add, subtract, multiply, divide on Field Values in a
   * string format fieldValue1 operator fieldValue2
   * 
   * @param operandOneValue
   * @param operandTwoValue
   * @param operatorInfo
   * @return
   */

  public Double getCalculateValue(String fieldValueStr, IGRCObject resource) {
    String operator = null;
    if (fieldValueStr.contains(ADD)) {
      operator = ADD;
    } else if (fieldValueStr.contains(SUBTRACT)) {
      operator = SUBTRACT;
    } else if (fieldValueStr.contains(MULTIPLY)) {
      operator = MULTIPLY;
    } else if (fieldValueStr.contains(DIVIDE)) {
      operator = DIVIDE;
    }
    debug("operator : " + operator);
    Double evaluatedValue = null;
    String[] operands = fieldValueStr.split(operator);
    Double[] operandVals = new Double[2];
    for (int i = 0; i < operands.length; i++) {
      operandVals[i] = getFieldValueAsDouble(resource, operands[i].trim());
    }
    debug("field value : " + operandVals[0] + " and " + operandVals[1]);
    if (operandVals[0] != null && operandVals[1] != null) {
      evaluatedValue = performEvaluation(operandVals[0], operandVals[1], operator);
    }
    debug("evaluatedValue : " + evaluatedValue);
    return evaluatedValue;
  }

  /**
   * Creating list of groups returned by the query
   * 
   * @param context
   * @param queryStmt
   * @param iQueryService
   * @param grcObject
   */
  public ArrayList<String> getQueryResults(String queryStmt, IQueryService iQueryService,
      Object[] arr) {
    debug("Starting getResults method " + queryStmt + " arr " + arr);
    ArrayList<String> prefList = new ArrayList<String>();

    queryStmt = MessageFormat.format(queryStmt, arr);

    debug("About to execute Query " + queryStmt);
    try {
      IQuery query = iQueryService.buildQuery(queryStmt);
      ITabularResultSet resultset = query.fetchRows(0);
      for (IResultSetRow row : resultset) {
        IField field = row.getField(0);
        if (field instanceof IIdField) {
          IIdField idField = (IIdField) field;
          if (idField.getValue() != null) {
            String prefId = idField.getValue().toString();
            prefList.add(prefId);
          }
        } else if (field instanceof IFloatField) {
          IFloatField idField = (IFloatField) field;
          if (idField.getValue() != null) {
            String prefId = idField.getValue().toString();
            prefList.add(prefId);
          }
        } else if (field instanceof ICurrencyField) {
          ICurrencyField idField = (ICurrencyField) field;
          if (idField.getBaseAmount() != null) {
            String prefId = idField.getBaseAmount().toString();
            prefList.add(prefId);
          }
        } else if (field instanceof IIntegerField) {
          IIntegerField idField = (IIntegerField) field;
          if (idField.getValue() != null) {
            String prefId = idField.getValue().toString();
            prefList.add(prefId);
          }
        }
      }
    } catch (Exception ex) {
      error("Error in Query Service execution" + ex, ex);
    }
    debug("Query returned count=" + prefList.size());
    return prefList;
  }

  /**
   * Gets the Registry Values
   * 
   * @param registryEntry
   * @param m_cs
   * @param logger
   * @return
   */
  @Deprecated
  public String getRegistryValue(String registryEntry, IConfigurationService m_cs) {
    String registryEntryVal = null;
    try {
      IConfigProperties settings = m_cs.getConfigProperties();
      registryEntryVal = settings.getProperty(registryEntry);
      if (registryEntryVal != null) {
        registryEntryVal = registryEntryVal.trim();
      }
      debug("value of registry entry " + registryEntry + " is " + registryEntryVal);
      return registryEntryVal;
    } catch (Exception e) {
      error("Error while reading registry", e);
      return null;
    }
  }

  /**
   * Sets null value for field
   * 
   * @param dstField
   */
  public void setNullValue(IField dstField) {
    switch (dstField.getDataType()) {
      case DATE_TYPE: {
        IDateField target = (IDateField) dstField;
        if (target != null)
          target.setValue(null);
        break;
      }
      case ENUM_TYPE: {
        IEnumField target = (IEnumField) dstField;
        if (target != null)
          target.setEnumValue(null);
        break;
      }

      case STRING_TYPE: {
        IStringField target = (IStringField) dstField;
        if (target != null)
          target.setValue(null);
        break;
      }
      case INTEGER_TYPE: {
        IIntegerField target = (IIntegerField) dstField;
        if (target != null)
          target.setValue(null);

        break;
      }
      case BOOLEAN_TYPE: {
        IBooleanField target = (IBooleanField) dstField;
        if (target != null)
          target.setValue(null);
        break;
      }
      case CURRENCY_TYPE: {

        ICurrencyField target = (ICurrencyField) dstField;
        if (target != null) {
          target.setLocalCurrency(null);
          target.setLocalAmount(null);
          target.setExchangeRate(null);
        }
        break;
      }
      case FLOAT_TYPE: {
        IFloatField target = (IFloatField) dstField;
        target.setValue(null);

        break;
      }
      case MULTI_VALUE_ENUM: {

        IMultiEnumField target = (IMultiEnumField) dstField;
        target.setEnumValues(null);

        break;
      }
      default: {
        debug("setNullValue for " + dstField.getDataType() + " datatype not supported!");

        break;
      }
    }
  }

  /**
   * 
   * @param key
   * @param m_cs
   * @return
   */
  @Deprecated
  public String getAppText(String key, IConfigurationService m_cs) {
    try {
      return m_cs.getLocalizedApplicationText(key);
    } catch (Exception e) {
      error(" Error while reading app text ", e);
      return null;
    }
  }

  /**
   * This will only work if current obj instance has serviceFactory!=null<br>
   * to ensure you have ServiceFactory not null use this constructor
   * <code>CommonUtils(Logger logger, IServiceFactory serviceFactory)</code>
   */
  public ArrayList<String> getQueryResultsMultipleReturnFields(String queryStmt, Object[] arr,
      int recordColumnsCount) throws Exception {
    return getQueryResultsMultipleReturnFields(queryStmt, arr, recordColumnsCount,
        getQueryService(), null);
  }

  /**
   * This will only work if current obj instance has serviceFactory!=null<br>
   * to ensure you have ServiceFactory not null use this constructor
   * <code>CommonUtils(Logger logger, IServiceFactory serviceFactory)</code>
   */
  public ArrayList<String> getQueryResultsMultipleReturnFields(String queryStmt, Object[] arr,
      int recordColumnsCount, Boolean honorPrimaryOnly) throws Exception {
    return getQueryResultsMultipleReturnFields(queryStmt, arr, recordColumnsCount,
        getQueryService(), honorPrimaryOnly);
  }

  @Deprecated
  public ArrayList<String> getQueryResultsMultipleReturnFields(String queryStmt, Object[] arr,
      int recordColumnsCount, IQueryService queryService) throws Exception {
    return getQueryResultsMultipleReturnFields(queryStmt, arr, recordColumnsCount, queryService,
        null);
  }

  @Deprecated
  public ArrayList<String> getQueryResultsMultipleReturnFields(String queryStmt, Object[] arr,
      int recordColumnsCount, IQueryService queryService, Boolean honorPrimaryOnly)
      throws Exception {
    ArrayList<String> results = new ArrayList<String>();
    debug("Entered getQueryResultsMultipleReturnFields(queryStmt: " + queryStmt + ",queryParams: "
        + (arr == null ? null : Arrays.asList(arr)) + ",recordColumnsCount: " + recordColumnsCount
        + ",honorPrimaryOnly: " + honorPrimaryOnly + ")");

    if (queryStmt == null || queryStmt.trim().isEmpty()) {
      throw new Exception("Query statement must exist.");
    }
    queryStmt = MessageFormat.format(queryStmt, arr);
    debug("About to execute query \"" + queryStmt + "\"");
    try {
      IQuery query = queryService.buildQuery(queryStmt);
      if (honorPrimaryOnly != null) {
        query.setHonorPrimary(honorPrimaryOnly);
      }

      ITabularResultSet resultset = query.fetchRows(0);

      for (IResultSetRow row : resultset) {
        String returnRecord = "";
        for (int i = 0; i < recordColumnsCount; i++) {
          IField field = row.getField(i);
          String value = null;
          if (field instanceof IIdField) {
            value = ((IIdField) field).getValue() + "";
          } else if (field instanceof IStringField) {
            value = ((IStringField) field).getValue() + "";
          } else if (field instanceof IFloatField) {
            value = ((IFloatField) field).getValue() + "";
          } else if (field instanceof ICurrencyField) {
            value = ((ICurrencyField) field).getBaseAmount() + "";
          } else if (field instanceof IEnumField) {
            IEnumValue enumValue = ((IEnumField) field).getEnumValue();
            if (enumValue != null) {
              value = enumValue.getName();
            }
          } else if (field instanceof IDateField) {
            IDateField dateField = (IDateField) field;
            Date date = dateField.getValue();
            value = "";
            if (date != null) {
              value = SIMPLE_DATE_FORMAT.format(date);
            }
          }
          returnRecord += RECORD_COLUMN_SEPARATOR
              + ((value == null || value.equalsIgnoreCase("null")) ? "" : value);
        }

        if (returnRecord.length() > 0) {
          returnRecord = returnRecord.substring(RECORD_COLUMN_SEPARATOR.length());
        }
        results.add(returnRecord);
      }
    } catch (Exception e) {
      error("Error in Query Service execution" + e);
    }
    debug("Query returned " + results.size() + " records: " + results);

    return results;
  }

  public Double getComplexFieldValueAsDouble(IGRCObject resource, String operandInfo) {
    Double result = null;
    debug("operandInfo=" + operandInfo);

    if (operandInfo.contains(MULTIPLY)) {
      String[] operands = operandInfo.split(MULTIPLY);
      Double operandOne = getFieldValueAsDouble(resource, operands[0].trim());
      Double operandTwo = getFieldValueAsDouble(resource, operands[1].trim());
      result = performEvaluation(operandOne, operandTwo, MULTIPLY);
    } else {
      return getFieldValueAsDouble(resource, operandInfo);
    }
    return result;

  }

  public void copyField(IField srcField, IField dstField) {
    debug("entered copyField(source field name: " + srcField.getName()
        + ", destination field name: " + dstField.getName() + ")");
    switch (dstField.getDataType()) {
      case BOOLEAN_TYPE: {
        debug("destination field type: IBooleanField");
        IBooleanField source = (IBooleanField) srcField;
        IBooleanField target = (IBooleanField) dstField;

        target.setValue(source.getValue());
        debug("set value: " + source.getValue());
        break;
      }
      case CURRENCY_TYPE: {
        debug("destination field type: ICurrencyField");
        ICurrencyField source = (ICurrencyField) srcField;
        ICurrencyField target = (ICurrencyField) dstField;
        if (source.getLocalAmount() != null) {
          target.setLocalCurrency(source.getLocalCurrency());
          target.setLocalAmount(source.getLocalAmount());
          target.setExchangeRate(source.getExchangeRate());
          debug("set value: \nlocalCurrency: " + source.getLocalCurrency() + ", localAmount: "
              + source.getLocalAmount() + ", exchangeRate: " + source.getExchangeRate());
        }

        break;
      }
      case DATE_TYPE: {
        debug("destination field type: IDateField");
        IDateField source = (IDateField) srcField;
        IDateField target = (IDateField) dstField;

        target.setValue(source.getValue());
        debug("set value: " + source.getValue());
        break;
      }
      case ENUM_ANSWERS_TYPE: {
        debug("destination field type: IEnumAnswersField");
        IEnumAnswersField source = (IEnumAnswersField) srcField;
        IEnumAnswersField target = (IEnumAnswersField) dstField;

        target.clearEnumAnswers();
        List<IEnumAnswer> cheatList = source.getEnumAnswers();
        for (IEnumAnswer cheat : cheatList) {
          target.appendEnumAnswer(cheat.getName(), cheat.getScore().intValue(), cheat.isHidden(),
              cheat.getDescription(), cheat.getScore(), cheat.getRequires());
          debug("cheat.getName(): " + cheat.getName() + ", cheat.getScore().intValue():"
              + cheat.getScore().intValue() + ", cheat.isHidden():" + cheat.isHidden()
              + ", cheat.getDescription(): " + cheat.getDescription() + ", cheat.getScore():"
              + cheat.getScore() + ", cheat.getRequires(): " + cheat.getRequires());
        }
        debug("copyField assign for ENUM_ANSWERS may give incorrect value!");
        break;
      }
      case ENUM_TYPE: {
        debug("destination field type: IEnumField");
        IEnumField source = (IEnumField) srcField;
        IEnumField target = (IEnumField) dstField;

        target.setEnumValue(source.getEnumValue());
        debug("set value: " + source.getEnumValue());
        break;
      }
      case FLOAT_TYPE: {
        debug("destination field type: IFloatField");
        IFloatField source = (IFloatField) srcField;
        IFloatField target = (IFloatField) dstField;

        target.setValue(source.getValue());
        debug("set value: " + source.getValue());
        break;
      }
      case ID_TYPE: {
        debug("copyField assign for ID is strictly forbidden!");

        break;
      }
      case INTEGER_TYPE: {
        debug("destination field type: IIntegerField");
        IIntegerField source = (IIntegerField) srcField;
        IIntegerField target = (IIntegerField) dstField;

        target.setValue(source.getValue());
        debug("set value: " + source.getValue());
        break;
      }
      case MULTI_VALUE_ENUM: {
        debug("destination field type: IMultiEnumField");
        IMultiEnumField source = (IMultiEnumField) srcField;
        IMultiEnumField target = (IMultiEnumField) dstField;

        target.setEnumValues(source.getEnumValues());
        debug("set value: " + source.getEnumValues());
        break;
      }
      case REFERENCE_TYPE: {
        debug("destination field type: IReferenceField");
        IReferenceField source = (IReferenceField) srcField;
        IReferenceField target = (IReferenceField) dstField;

        target.setValue(source.getValue());
        debug("set value: " + source.getValue());
        break;
      }
      case STRING_TYPE: {
        debug("destination field type: IStringField ; STRING_TYPE");
        IStringField source = (IStringField) srcField;
        IStringField target = (IStringField) dstField;

        target.setValue(source.getValue());
        debug("set value: " + source.getValue());
        break;
      }
      case MEDIUM_STRING_TYPE: {
        debug("destination field type: IStringField ; MEDIUM_STRING_TYPE");
        IStringField source = (IStringField) srcField;
        IStringField target = (IStringField) dstField;

        target.setValue(source.getValue());
        debug("set value: " + source.getValue());
        break;
      }
      case LARGE_STRING_TYPE: {
        debug("destination field type: IStringField ; LARGE_STRING_TYPE");
        IStringField source = (IStringField) srcField;
        IStringField target = (IStringField) dstField;

        target.setValue(source.getValue());
        debug("set value: " + source.getValue());
        break;
      }
      case UNLIMITED_STRING_TYPE: {
        debug("destination field type: IStringField ; UNLIMITED_STRING_TYPE");
        IStringField source = (IStringField) srcField;
        IStringField target = (IStringField) dstField;

        target.setValue(source.getValue());
        debug("set value: " + source.getValue());
        break;
      }
      default: {
        logger.info("copyField from Parent to child for " + srcField.getDataType()
            + " datatype has been requested!"
            + " Only known datatypes are:- BOOLEAN, CURRENCY, DATE, ENUM_ANSWERS, ENUM, FLOAT, INTEGER, MULTI_ENUM, REFERENCE, STRING, MEDIUM_STRING, LARGE_STRING AND UNLIMITED_STRING");
        break;
      }
    }
  }

  /**
   * 
   * @param string
   * @param resource
   * @return
   */
  public Object[] getParams(String string, IGRCObject resource) {
    Object[] params = new Object[] {};
    if (string != null && !string.trim().isEmpty()) {
      String[] fields = string.split("\\,");
      params = new Object[fields.length];
      for (int i = 0; i < fields.length; i++) {
        if (fields[i].equals("Name")) {
          params[i] = resource.getName();
        } else if (fields[i].equals("Resource ID")) {
          params[i] = resource.getId().toString();
        } else {
          Object fldValue = null;
          if (fields[i].startsWith("REMOVELINK:")) {
            fields[i] = fields[i].replace("REMOVELINK:", "");
            fldValue = getFieldValue(fields[i], resource);
            debug("REMOVELINK " + fldValue);
            if (fldValue != null) {
              String fieldvalue = (String) fldValue;
              fieldvalue =
                  fieldvalue.substring(fieldvalue.indexOf("'>") + 2, fieldvalue.indexOf("</a>"));
              debug("fieldvalue : " + fieldvalue);
              fldValue = fieldvalue;
            }
            params[i] = fldValue;
          } else {
            params[i] = getFieldValue(fields[i], resource);
          }
        }
        debug("params[" + i + "] " + params[i]);
      }
    }
    return params;
  }

  private void debug(String string) {
    if (!enableDebugLogs) {
      return;
    }

    logger.debug(string);
  }

  private void error(String string, Exception e) {
    logger.error(string, e);
  }

  private void error(String string) {
    logger.error(string);
  }

  public String getFieldValueAsString(String fieldName, IGRCObject object) {
    String value = null;
    Object valueAsObj = getFieldValue(fieldName, object);
    if (valueAsObj != null) {
      if (valueAsObj instanceof Object[] && ((Object[]) valueAsObj).length > 1) {
        // if it's currency field get value at idx 1 (base amount)
        value = (String) ((Object[]) valueAsObj)[1] + "-" + ((Object[]) valueAsObj)[0] + "-"
            + ((Object[]) valueAsObj)[2];

        // e.g. 100 EUR 1.3
      } else if ((valueAsObj instanceof Integer || valueAsObj instanceof Double)
          && valueAsObj.toString().matches(CommonUtils.REGEX_DOUBLE_PATTERN)) {
        // create BigDecimal and get plain string to avoid getting Double values in
        // scientific notation like e.g. 3.0E4 (3 to the power of 4)
        value = new BigDecimal(valueAsObj.toString()).toPlainString();
      } else if (valueAsObj instanceof String) {
        value = (String) valueAsObj;
      } else if (valueAsObj instanceof Date) {
        value = SIMPLE_DATE_FORMAT.format((Date) valueAsObj);
      }
    }
    if (value == null) {
      value = "";
    }

    debug("getFieldValueAsString(fieldName: " + fieldName + ", grcObject:" + object.getName()
        + ", having ID(" + object.getId() + ") -> \"" + value + "\"");
    return value;
  }

  public void setRegistrySetting(String registrySetting, String registryValue,
      OPAppSession opSession) throws Exception {
    debug("Entered setRegistrySetting(" + registrySetting + "," + registryValue + ")");

    AdminService adminService = opSession.getAdminService();
    adminService.setEntryValue(registrySetting, registryValue);

    debug("Exiting setRegistrySetting()");
  }

  public void clearFieldValueOnResource(String fieldName, IGRCObject object,
      IConfigurationService configurationService) {
    debug("Entered clearFieldValueOnResource(" + fieldName + "," + object.getName() + "(having id "
        + object.getId() + ")");
    IField field = object.getField(fieldName);
    DataType dataType = null;
    if (field != null) {
      dataType = field.getDataType();

      switch (dataType) {
        case DATE_TYPE:
          ((IDateField) field).setValue(null);
          break;
        default:
          break;
      }
    }
    // TODO in future - add other field types as they're required
    debug("Exiting clearFieldValueOnResource()");
  }

  public IGRCObject setFieldValuesAndSave(String csvFieldNames, List<Object> fieldValues,
      IGRCObject object) throws Exception {
    debug("Entered setFieldValuesAndSave()");
    boolean wasAnyFieldUpdated = setFieldValues(csvFieldNames, fieldValues, object);

    object = getResourceService().saveResource(object);
    debug("Exiting setFieldValuesAndSave(); wasAnyFieldUpdated: " + wasAnyFieldUpdated
        + "; object name: " + object.getName() + ", having ID " + object.getId());
    return object;
  }

  public boolean setFieldValuesOnGrcObjectWithOnlyTheseFields(String csvFieldNames,
      String csvFieldValues, String resourceId) throws Exception {
    return setFieldValuesOnGrcObjectWithOnlyTheseFields(csvFieldNames, csvFieldValues, resourceId,
        false);
  }

  public boolean setFieldValuesOnGrcObjectWithOnlyTheseFieldsAndSave(String csvFieldNames,
      String csvFieldValues, String resourceId) throws Exception {
    return setFieldValuesOnGrcObjectWithOnlyTheseFields(csvFieldNames, csvFieldValues, resourceId,
        true);
  }

  public boolean setFieldValuesOnGrcObjectWithOnlyTheseFields(String csvFieldNames,
      String csvFieldValues, String resourceId, boolean doSave) throws Exception {
    debug("Entered setFieldValuesOnGrcObjectWithOnlyTheseFields(csvFieldNames:" + csvFieldNames
        + "; csvFieldValues: " + csvFieldValues + "; resourceId: " + resourceId + ", doSave:"
        + doSave + ")");

    List<String> fieldNames = Arrays.asList(csvFieldNames.split(REGEX_COMMA_PATTERN));
    logger.debug("fieldNames: " + fieldNames);
    IFieldDefinition[] fieldDefinitions = new IFieldDefinition[fieldNames.size()];
    for (int i = 0; i < fieldNames.size(); i++) {
      fieldDefinitions[i] = getMetaDataService().getField(fieldNames.get(i));
    }

    GRCObjectFilter filter =
        new GRCObjectFilter(getConfigurationService().getCurrentReportingPeriod());
    filter.setFieldFilters(fieldDefinitions);
    filter.getAssociationFilter().setIncludeAssociations(IncludeAssociations.NONE);

    IGRCObject grcObjOnlyWithTheCsvFields =
        getResourceService().getGRCObject(new Id(resourceId), filter);

    boolean wasAnyFieldUpdated =
        setFieldValues(csvFieldNames, csvFieldValues, grcObjOnlyWithTheCsvFields);
    if (doSave) {
      getResourceService().saveResource(grcObjOnlyWithTheCsvFields);
      logger.debug("saved");
    }

    return wasAnyFieldUpdated;
  }

  public boolean setFieldValues(String csvFieldNames,
      String possiblyNotFinalValuesWhichMightNeedProcessing, IGRCObject object) throws Exception {

    List<Object> values = new ArrayList<Object>();

    for (String valueStr : getListFromCsvString(possiblyNotFinalValuesWhichMightNeedProcessing)) {
      Object value = valueStr;
      if (((String) valueStr).startsWith(SETTING_MARKER_CSV_FIELD_VALUES)) {
        value = ((String) valueStr).substring(SETTING_MARKER_CSV_FIELD_VALUES.length());
        value = getSettingByRelativePath((String) valueStr);
        debug("value is from a setting: " + valueStr);
      }

      if (((String) valueStr).matches(TODAY_PLACEHOLDER_PATTERN_STR)) {
        value = getToday();
        debug("value is today's date: " + SIMPLE_DATE_FORMAT.format(valueStr));
      } else if (((String) valueStr).matches(DATE_EXPRESSION_RELATIVE_TO_TODAY_PATTERN_STR)) {
        value = getDateFromPossiblePlusMinusExpression((String) valueStr);
        debug("value is date: " + value);
      } else if (valueStr.equals(END_USER_PLACEHOLDER_VALUE) || valueStr
          .equals(OPEN_SQUARE_BRACKET + END_USER_PLACEHOLDER_VALUE + CLOSED_SQUARE_BRACKET)) {
        // TODO: check if & or ; must be added to user field value ; it's single
        value = getSecurityService().getCurrentUser().getName();
        debug("value is a username: " + value);
      } else if (((String) valueStr).startsWith(APP_TEXT_MARKER_CSV_FIELD_VALUES)) {
        value = ((String) valueStr).substring(APP_TEXT_MARKER_CSV_FIELD_VALUES.length());
        value = getAppText((String) valueStr);
        debug("value is from app text: " + value);
      } else {
        debug("value is: " + value);
      }

      values.add(value);
    }

    return setFieldValues(csvFieldNames, values, object);

  }

  public boolean setFieldValues(String csvFieldNames, List<Object> values, IGRCObject object)
      throws Exception {
    debug("Entered setFieldValues(csvFieldNames:" + csvFieldNames + "; fieldValues: " + values
        + "; object: name " + object.getName() + ", having ID " + object.getId());

    boolean wasAnyFieldUpdated = false;
    if (csvFieldNames == null || csvFieldNames.trim().isEmpty() || values == null
        || values.size() == 0) {
      logger.warn(
          "field names or field values list is null or empty. Exiting setFieldValues(); wasAnyFieldUpdated: "
              + wasAnyFieldUpdated);
      return wasAnyFieldUpdated;
    }

    List<String> fieldNames = getListFromCsvString(csvFieldNames);
    if (fieldNames.size() != values.size()) {
      logger.warn("size of fieldNames list(" + fieldNames.size()
          + ") is different than size of field values list(" + values.size()
          + "). Exiting setFieldValues(); wasAnyFieldUpdated: " + wasAnyFieldUpdated);
      return wasAnyFieldUpdated;
    }

    for (int i = 0; i < fieldNames.size(); i++) {
      String fieldName = fieldNames.get(i);
      Object newValueToSet = values.get(i);
      Object existingValue = getFieldValue(fieldName, object);

      boolean isNewValueDifferentThanExistingOne = false;
      if ((existingValue != null && !existingValue.equals(newValueToSet))
          || (existingValue == null && newValueToSet != null)) {
        isNewValueDifferentThanExistingOne = true;
        if (!wasAnyFieldUpdated) {
          wasAnyFieldUpdated = true;
        }
      }

      if (isNewValueDifferentThanExistingOne) {
        if (fieldName.equalsIgnoreCase("Name") || fieldName.equalsIgnoreCase("System Fields:Name")
            || fieldName.equalsIgnoreCase("System Field:Name")) {
          object.setName((String) newValueToSet);
        } else if (fieldName.equalsIgnoreCase("Description")
            || fieldName.equalsIgnoreCase("System Fields:Description")
            || fieldName.equalsIgnoreCase("System Field:Description")) {
          object.setDescription((String) newValueToSet);
        } else {
          setFieldValueOnResource(fieldName, newValueToSet, object);
        }

        debug("have set value of field " + fieldName + " to " + newValueToSet + "; previous value: "
            + existingValue);
      }
    }

    debug("Exiting setFieldValues(); wasAnyFieldUpdated: " + wasAnyFieldUpdated);

    return wasAnyFieldUpdated;
  }

  public IGRCObject getObjectById(String id) {
    debug("Entered getObjectById(" + id + ")");
    return getObjectById(new Id(id));
  }

  public IGRCObject getObjectById(Id id) {
    debug("Entered getObjectById(" + id + ")");
    IGRCObject object = getResourceService().getGRCObject(id);
    debug("Exiting getObjectById() - returning object named " + object.getName());
    return object;
  }

  public String getIdOfRecord(String record) {
    return getIdOfRecord(record, false);
  }

  public String getIdOfRecord(String record, boolean doDebug) {
    String recordId = record.split(RECORD_COLUMN_SEPARATOR, -1)[0];
    if (doDebug) {
      debug("getIdOfRecord() -> returning " + recordId);
    }
    return recordId;
  }

  public boolean doTheseFieldsHaveTheseValues(String csvFieldNames, String csvFieldValues,
      IGRCObject object) {
    debug("Entered doTheseFieldsHaveTheseValues(" + csvFieldNames + "," + csvFieldValues
        + "csvFieldValues: " + csvFieldValues + ", object named " + object.getName() + "(having id "
        + object.getId() + ")");

    if (csvFieldNames == null || csvFieldNames.trim().isEmpty() || csvFieldValues == null
        || csvFieldValues.isEmpty()) {
      logger.error("EXCEPTION: Exiting because field names or field values list is null or empty");
      return false;
    }

    List<String> fieldNames =
        new ArrayList<String>(Arrays.asList(csvFieldNames.split(REGEX_COMMA_PATTERN)));
    List<String> fieldValues =
        new ArrayList<String>(Arrays.asList(csvFieldValues.split(REGEX_COMMA_PATTERN)));

    if (fieldNames.size() != fieldValues.size()) {
      error("EXCEPTION: Exiting because size of fieldNames list(" + fieldNames.size()
          + ") is different than size of field values list(" + fieldValues.size() + ")");
      return false;
    }

    for (int i = 0; i < fieldNames.size(); i++) {
      String actualValue = getFieldValueAsString(fieldNames.get(i), object);
      debug("Checking if field actual value " + actualValue + " is equal to expected value "
          + fieldValues.get(i));
      if (actualValue.equals(fieldValues.get(i))) {
        debug("Exiting doTheseFieldsHaveTheseValues()- returning false");
        return false;
      }
    }

    debug("Exiting doTheseFieldsHaveTheseValues()- returning true");
    return true;
  }

  public boolean doesFieldHaveAnyOfTheseValues(String fieldName, String csvFieldValues,
      IGRCObject object) {
    debug("Entered doesFieldHaveAnyOfTheseValues(fieldName:" + fieldName + ", csvFieldValues: "
        + csvFieldValues + "; object named " + object.getName() + "(having id " + object.getId()
        + "))");

    if (fieldName == null || fieldName.trim().isEmpty() || csvFieldValues == null
        || csvFieldValues.isEmpty()) {
      error("Exiting because field name or field values list is null or empty- returning false");
      return false;
    }

    List<String> fieldValues =
        new ArrayList<String>(Arrays.asList(csvFieldValues.split(REGEX_COMMA_PATTERN)));
    String actualValue = getFieldValueAsString(fieldName, object);
    debug("Checking if field actual value " + actualValue + " is in list of values " + fieldValues);

    boolean result = fieldValues.contains(actualValue);

    debug("Exiting doesFieldHaveAnyOfTheseValues() - returning " + result);
    return result;
  }

  public List<String> getListFromCsvString(String csvString) {
    List<String> list = new ArrayList<String>(Arrays.asList(csvString.split(REGEX_COMMA_PATTERN)));
    debug("getListFromCsvString(" + csvString + ") -> returning " + list);
    return list;
  }

  public String getRecordValueAtIndex(int index, String record) throws Exception {
    return getRecordValueAtIndex(index, record, false);
  }

  public String getRecordValueAtIndex(int index, String record, boolean doDebug) throws Exception {
    if (record == null) {
      throw new NullPointerException("record must not be null");
    }

    if (index < 0) {
      throw new NullPointerException("index must be greatert than or equal to 0");
    }

    List<String> list = Arrays.asList(record.split(RECORD_COLUMN_SEPARATOR, -1));

    if (index >= list.size()) {
      throw new Exception("Cannot get value at inexistent index");
    }
    if (doDebug) {
      debug("getRecordValueAtIndex(" + record + ") -> returning \"" + list.get(index) + "\"");
    }
    return list.get(index);
  }

  public String getSetting(String settingPath) {
    return getSetting(settingPath, false);
  }

  /**
   * This will only work if current obj instance has ServiceFactory!=null; to ensure you have
   * ServiceFactory not null use this constructor
   * <code>CommonUtils(Logger logger, IServiceFactory serviceFactory)</code>
   */
  public String getSetting(String settingPath, boolean doDebug) {
    String setting = null;
    try {
      if (!settingPath.contains(SLASH) && settingsRootPath != null) {
        settingPath = settingsRootPath.trim()
            + (settingsRootPath.trim().endsWith(SLASH) ? "" : SLASH)
            + (settingPath.startsWith(SLASH) ? settingPath.substring(SLASH.length()) : settingPath);
      }

      IConfigProperties settings = getConfigurationService().getConfigProperties();
      setting = settings.getProperty(settingPath);
      if (setting != null) {
        setting = setting.trim();
      }
    } catch (Exception e) {
      error("Error while reading setting", e);
      return null;
    }


    if (doDebug) {
      debug("getSetting(" + settingPath + ") -> returning \"" + setting + "\".");
    }
    return setting;
  }

  public String getSettingTrimmedEmptyStringIfNull(String settingPath) {
    String setting = getSetting(settingPath);
    if (setting == null)
      setting = "";
    return setting.trim();
  }

  public String getSettingWithDefaultValueIfNull(String settingPath, String defaultValue) {
    String value = getSetting(settingPath);
    return value != null ? value : defaultValue;
  }

  public String[] getSettingCsvAsArray(String setting) {
    String settingCsv = getSetting(setting);
    if (settingCsv == null) {
      return null;
    }

    String[] array = settingCsv.split(REGEX_COMMA_PATTERN, -1);
    debug("getSettingCsvAsArray(" + setting + ") -> returning array with values "
        + Arrays.asList(array) + ".");
    return array;
  }

  public List<String> getSettingCsvAsList(String setting) {
    String settingCsv = getSetting(setting);
    if (settingCsv == null) {
      return null;
    }

    String[] array = settingCsv.split(REGEX_COMMA_PATTERN, -1);
    List<String> list = Arrays.asList(array);
    debug("getSettingCsvAsList(" + setting + ") -> returning list" + list + ".");
    return list;
  }

  public String getSettingByRelativePath(String settingRelativePath) throws Exception {
    if (settingsRootPath == null || settingRelativePath.isEmpty()) {
      logger.debug(
          "getSettingByRelativePath() -> will throw exception: Setting root path must be defined");
      throw new Exception("Setting root path must be defined");
    }
    String value = getSetting(settingsRootPath + settingRelativePath);

    debug("getSettingByRelativePath(" + settingRelativePath + ") -> returning \"" + value + "\".");
    return value;
  }


  public Date getToday() {
    Date date = new Date();
    debug("getToday() -> returning \"" + date + "\".");
    return date;
  }


  public Date getDatePlusMinusXDays(Date date, int daysOffset) {
    calendar.setTime(date);
    calendar.add(Calendar.DATE, daysOffset);
    date = calendar.getTime();

    debug("getDatePlusMinusXDays(" + date + ", " + daysOffset + ") -> returning \"" + date + "\".");
    return date;
  }

  public String getDatePlusMinusXDaysFormatted(Date date, int daysOffset) {
    String dateStr = SIMPLE_DATE_FORMAT.format(getDatePlusMinusXDays(date, daysOffset));

    debug("getDatePlusMinusXDaysFormatted(" + date + ", " + daysOffset + ") -> returning \""
        + dateStr + "\".");
    return dateStr;
  }

  public Date getDateFromPossiblePlusMinusExpression(String dateExpression) throws Exception {
    debug("Entered getDateFromPossiblePlusMinusExpression(" + dateExpression + ")");

    Matcher mat = DATE_EXPRESSION_RELATIVE_TO_TODAY_PATTERN.matcher(dateExpression);
    Date date = null;
    if (mat.matches()) {
      // e.g. +3 or -5
      String daysOffsetStr = mat.group(2) + mat.group(3);
      if (daysOffsetStr.startsWith("+")) {
        daysOffsetStr = daysOffsetStr.substring(1);
      }

      date = getDatePlusMinusXDays(getToday(), Integer.parseInt(daysOffsetStr));
    } else if (dateExpression.matches(TODAY_PLACEHOLDER_PATTERN_STR)) {
      date = getToday();
    } else {
      throw new Exception("Expression " + dateExpression + " doesn't match pattern "
          + DATE_EXPRESSION_RELATIVE_TO_TODAY_PATTERN_STR);
    }

    debug("Exiting getDateFromPossiblePlusMinusExpression(" + dateExpression + ") - returning \""
        + date + "\".");
    return date;
  }

  public String getFormattedDateFromPossiblePlusMinusExpression(String dateExpression)
      throws Exception {
    debug("Entered getFormattedDateFromPossiblePlusMinusExpression(" + dateExpression + ")");
    Date date = getDateFromPossiblePlusMinusExpression(dateExpression);

    String dateStr = SIMPLE_DATE_FORMAT.format(date);
    debug("Exiting getFormattedDateFromPossiblePlusMinusExpression(" + dateExpression
        + ") - returning \"" + dateStr + "\".");
    return dateStr;
  }

  /**
   * This will only work if current obj instance has ServiceFactory!=null; to ensure you have
   * ServiceFactory not null use this constructor
   * <code>CommonUtils(Logger logger, IServiceFactory serviceFactory)</code>
   */
  public String getAppText(String appTextKey) {
    String appText;
    try {
      appText = getConfigurationService().getLocalizedApplicationText(appTextKey);
    } catch (Exception e) {
      error("Error while reading app text ", e);
      return null;
    }

    if (appText == null) {
      appText = "";
    }
    debug("getAppText(" + appTextKey + ") -> returning \"" + appText + "\".");
    return appText;
  }

  public IGRCObject saveResorce(IGRCObject object) {
    object = getResourceService().saveResource(object);
    logger.debug(
        " saveResource -> saved object name: " + object.getName() + "id: " + object.getId());
    return object;
  }

  public IConfigurationService getConfigurationService() {
    if (configurationService == null) {
      if (serviceFactory == null) {
        throw new NullPointerException(
            "serviceFactory must not be null ; use CommonUtils(Logger logger, IServiceFactory serviceFactory) constructor");
      }
      configurationService = serviceFactory.createConfigurationService();
    }
    return configurationService;
  }

  public IQueryService getQueryService() {
    if (queryService == null) {
      if (serviceFactory == null) {
        throw new NullPointerException(
            "serviceFactory must not be null ; use CommonUtils(Logger logger, IServiceFactory serviceFactory) constructor");
      }
      queryService = serviceFactory.createQueryService();
    }
    return queryService;
  }

  public IResourceService getResourceService() {
    if (resourceService == null) {
      if (serviceFactory == null) {
        throw new NullPointerException(
            "serviceFactory must not be null ; use CommonUtils(Logger logger, IServiceFactory serviceFactory) constructor");
      }
      resourceService = serviceFactory.createResourceService();
    }
    return resourceService;
  }

  public IApplicationService getApplicationService() {
    if (applicationService == null) {
      if (serviceFactory == null) {
        throw new NullPointerException(
            "serviceFactory must not be null ; use CommonUtils(Logger logger, IServiceFactory serviceFactory) constructor");
      }
      applicationService = serviceFactory.createApplicationService();
    }
    return applicationService;
  }

  public ISecurityService getSecurityService() {
    if (securityService == null) {
      if (serviceFactory == null) {
        throw new NullPointerException(
            "serviceFactory must not be null ; use CommonUtils(Logger logger, IServiceFactory serviceFactory) constructor");
      }
      securityService = serviceFactory.createSecurityService();
    }
    return securityService;
  }

  public IMetaDataService getMetaDataService() {
    if (metaDataService == null) {
      if (serviceFactory == null) {
        throw new NullPointerException(
            "serviceFactory must not be null ; use CommonUtils(Logger logger, IServiceFactory serviceFactory) constructor");
      }
      metaDataService = serviceFactory.createMetaDataService();
    }
    return metaDataService;
  }

  public IResourceFactory getResourceFactory() {
    if (resourceFactory == null) {
      if (serviceFactory == null) {
        throw new NullPointerException(
            "serviceFactory must not be null ; use CommonUtils(Logger logger, IServiceFactory serviceFactory) constructor");
      }
      resourceFactory = getResourceService().getResourceFactory();
    }
    return resourceFactory;
  }


  public IQuestionnaireService getQuestionnaireService() {
    if (questionnaireService == null) {
      if (serviceFactory == null) {
        throw new NullPointerException(
            "serviceFactory must not be null ; use CommonUtils(Logger logger, IServiceFactory serviceFactory) constructor");
      }
      questionnaireService = serviceFactory.createQuestionnaireService();

    }
    return questionnaireService;
  }

  public SimpleDateFormat getSimpleDateFormat() {
    return SIMPLE_DATE_FORMAT;
  }

  public byte[] runReportAsPdf(String reportPath, Map<String, String> reportParams)
      throws IOException {
    return runReport(reportPath, reportParams, PDF_FILE_EXTENSION);
  }

  public byte[] runReport(String reportPath, Map<String, String> params, String outputFormat)
      throws IOException {
    byte[] reportBytes;

    debug("Entered runReport(reportPath:" + reportPath + ", reportParams: " + params
        + ", outputFormat: " + outputFormat + ")");

    IReportParameters parameters = getApplicationService().getParametersForReport(reportPath);
    for (String paramName : params.keySet()) {
      parameters.setParameterValue(paramName, params.get(paramName));
    }

    InputStream inputStream = getApplicationService().invokeCognosReport(reportPath,
        CognosOutputFormat.valueOf(outputFormat), parameters);
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    try {
      byte[] buffer = new byte[65536];
      int l;
      while ((l = inputStream.read(buffer)) > 0) {
        output.write(buffer, 0, l);
      }
    } finally {
      if (inputStream != null) {
        inputStream.close();
      }
    }

    reportBytes = output.toByteArray();

    debug("Exiting runReport(..) - returning byte array of size "
        + (reportBytes == null ? "null" : reportBytes.length));
    return reportBytes;
  }

  public void runAndAttachPdfReport(String reportPath, Map<String, String> reportParams,
      String documentName, Id parentObjectId) throws Exception {
    runAndAttachReportAndSetFields(reportPath, reportParams, PDF_FILE_EXTENSION, documentName,
        parentObjectId, null, null);
  }

  public void runAndAttachPdfReport(String reportPath, Map<String, String> reportParams,
      String documentName, String parentObjectId) throws Exception {
    runAndAttachReportAndSetFields(reportPath, reportParams, PDF_FILE_EXTENSION, documentName,
        new Id(parentObjectId), null, null);
  }

  public void runAndAttachReport(String reportPath, Map<String, String> reportParams,
      String outputFileType, String documentName, Id parentObjectId) throws Exception {
    runAndAttachReportAndSetFields(reportPath, reportParams, outputFileType, documentName,
        parentObjectId, null, null);
  }

  public void runAndAttachPdfReportAndSetFields(String reportPath, Map<String, String> reportParams,
      String documentName, Id parentObjectId, String csvFieldNames, String csvFieldValues)
      throws Exception {
    runAndAttachReportAndSetFields(reportPath, reportParams, PDF_FILE_EXTENSION, documentName,
        parentObjectId, csvFieldNames, csvFieldValues);
  }

  public void runAndAttachReportAndSetFields(String reportPath, Map<String, String> reportParams,
      String outputFileType, String documentName, Id parentObjectId, String csvFieldNames,
      String csvFieldValues) throws Exception {
    debug("Entered runAndAttachReportAndSetFields(reportPath: " + reportPath + ", reportParams: "
        + reportParams + "outputFileType: " + outputFileType + ", documentName: " + documentName
        + ", parentObjectId: " + parentObjectId + ", csvFieldNames: " + csvFieldNames
        + ", csvFieldValues: " + csvFieldValues);

    byte[] reportBytes = runReport(reportPath, reportParams, outputFileType);

    ITypeDefinition objectTypeDef = getMetaDataService().getType(SOX_DOCUMENT_TYPE_NAME);
    IFileTypeDefinition fileTypeDef = null;
    for (IFileTypeDefinition iftd : objectTypeDef.getFileTypes()) {
      if (iftd.getFileExtension().equalsIgnoreCase(PDF_FILE_EXTENSION)) {
        fileTypeDef = iftd;
        break;
      }
    }

    IDocument newDocumentObj =
        getResourceFactory().createDocument(documentName, objectTypeDef, fileTypeDef);
    newDocumentObj.setPrimaryParent(parentObjectId);
    newDocumentObj.setContent(reportBytes);
    setFieldValues(csvFieldNames, csvFieldValues, newDocumentObj);
    newDocumentObj = getResourceService().saveResource(newDocumentObj);

    // set reportBytes = null to try to improve performance (now it's > 1 min to generate report)
    reportBytes = null;
    debug("Created document named " + newDocumentObj.getName() + "(having ID "
        + newDocumentObj.getId() + ")");

    debug("Exiting runAndAttachReportAndSetFields(..)");
  }

  public IGRCObject copyFields(String sourceFieldsCsv, String destinationFieldsCsv,
      IGRCObject sourceObj, IGRCObject destinationObj) throws Exception {
    logger.debug("Entered copyFields(sourceFieldsCsv: " + sourceFieldsCsv
        + ", destinationFieldsCsv: " + destinationFieldsCsv + "; sourceObj: name "
        + sourceObj.getName() + ", having ID " + sourceObj.getId() + "; destinationObj: "
        + destinationObj.getName() + ", having ID " + destinationObj.getId() + ")");

    List<Object> destinationFieldValues = getFieldsValues(sourceFieldsCsv, sourceObj);
    setFieldValues(destinationFieldsCsv, destinationFieldValues, destinationObj);

    logger.debug("Exiting copyFields()");

    return destinationObj;
  }

  public IGRCObject copyFieldsAndSave(String sourceFieldsCsv, String destinationFieldsCsv,
      IGRCObject sourceObj, IGRCObject destinationObj) throws Exception {

    copyFields(sourceFieldsCsv, destinationFieldsCsv, sourceObj, destinationObj);
    destinationObj = getResourceService().saveResource(destinationObj);

    return destinationObj;
  }

  private List<Object> getFieldsValues(String fieldsCsv, IGRCObject object) {
    List<Object> values = new ArrayList<Object>();
    for (String field : getListFromCsvString(fieldsCsv)) {
      Object value = getFieldValue(field, object);
      values.add(value);
    }

    logger.debug("getFieldsValues(fieldsCsv:" + fieldsCsv + ", object: name " + object.getName()
        + ", having ID " + object.getId() + ") -> returning " + values);
    return values;
  }

  public Locale getCurrentUserLocale() {
    Locale locale = getSecurityService().getCurrentUser().getLocale();
    logger.debug("getCurrentUserLocale() -> returning locale: name "
        + (locale != null ? locale.getDisplayName() : "null") + ", language "
        + (locale != null ? locale.getLanguage() : "null"));
    return locale;
  }


  public String getObjectTaskViewUrlPrefix() {
    if (objTaskViewUrlPrefix == null) {
      String appUrlPath = AuroraEnv.getAttribute("application.url.path");
      if (!appUrlPath.trim().endsWith(SLASH)) {
        appUrlPath = appUrlPath + SLASH;
      }

      String objectTaskViewRelativePath = "app/jspview/react/grc/task-view/";

      objTaskViewUrlPrefix = appUrlPath + objectTaskViewRelativePath;
    }
    debug("getObjectTaskViewUrlPrefix() -> returning " + objTaskViewUrlPrefix);
    return objTaskViewUrlPrefix;
  }

  public String replacePlaceholdersWithValues(String textWithPlaceholders, Object[] values) {
    for (int i = 0; i < values.length; i++) {
      textWithPlaceholders = textWithPlaceholders.replace("{" + i + "}", (String) values[i]);
    }

    debug("replacePlaceholdersWithValues(textWithPlaceholders: " + textWithPlaceholders
        + ", values: " + Arrays.toString(values) + " -> returning " + objTaskViewUrlPrefix);
    return textWithPlaceholders;
  }
}
