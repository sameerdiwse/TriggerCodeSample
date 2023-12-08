/**
 * IBM Confidential Copyright IBM Corporation 2021 The source code for this program is not published
 * or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S.
 * Copyright Office.
 */
package com.ibm.openpages.ext.custom.workflow.action;

import java.util.List;
import org.apache.log4j.Logger;
import com.ibm.openpages.api.metadata.Id;
import com.ibm.openpages.api.resource.IGRCObject;
import com.ibm.openpages.api.workflow.actions.AbstractCustomAction;
import com.ibm.openpages.api.workflow.actions.IWFCustomProperty;
import com.ibm.openpages.api.workflow.actions.IWFOperationContext;
import com.ibm.openpages.ext.custom.util.CommonUtils;
import com.openpages.ext.solutions.common.LoggerUtilExtended;

public class CloseIssueAction extends AbstractCustomAction {

  private static final String SLASH = "/";

  // Close Issue Action on the Issue workflow: please contact the system
  // administrator about this error.
  private static final String GENERIC_ERROR_APP_TEXT_KEY =
      "com.posten.wf.custom.close.issue.action.generic.error.message";

  // /Solutions/Custom/Close Issue Action/
  private static final String SETTINGS_FOLDER_WF_PROPERTY = "config.location";

  private static final String REGISTRY_SETTING_PREFIX = "/OpenPages";

  // Green=
  private static final String GREEN_BREACH_VALUE_TO_ISSUE_PRIORITY_SETTING =
      "Green Breach Value to Issue Priority";

  // Yellow=Low
  private static final String YELLOW_BREACH_VALUE_TO_ISSUE_PRIORITY_SETTING =
      "Yellow Breach Value to Issue Priority";

  // Orange=Medium
  private static final String ORANGE_BREACH_VALUE_TO_ISSUE_PRIORITY_SETTING =
      "Orange Breach Value to Issue Priority";

  // Red=High
  private static final String RED_BREACH_VALUE_TO_ISSUE_PRIORITY_SETTING =
      "Red Breach Value to Issue Priority";

  // OPSS-KRI-Shared:Breach Status
  private static final String KRI_BREACH_STATUS_FIELD_SETTING = "KRI Breach Status Field";

  // false
  private static final String HONOR_PRIMARY_ONLY_ON_QUERIES_SETTING =
      "Honor primary only-on Queries";

  // SELECT [SOXIssue].[Resource ID],[SOXIssue].[Name], [SOXIssue].[OPSS-Iss:Priority],
  // [SOXIssue].[OPSS-Iss:Status]
  // FROM [SOXIssue]
  // JOIN [KeyRiskIndicator] ON CHILD([SOXIssue])
  // WHERE [KeyRiskIndicator].[Resource ID]= {0}
  // AND ([SOXIssue].[OPSS-Iss:Status] <> ''Closed''
  // OR [SOXIssue].[OPSS-Iss:Status] IS NULL)

  private static final String GET_ALL_UNCLOSED_ISSUES_UNDER_KRI =
      "Query-Get All Unclosed Issues under KRI";

  // SELECT [KeyRiskIndicator].[Resource ID], [KeyRiskIndicator].[Name],
  // [KeyRiskIndicatorValue].[OPSS-KRI-Shared:Breach Status],
  // [KeyRiskIndicator].[OPSS-Shared-Lib:Library ID] FROM [KeyRiskIndicator]
  // JOIN [SOXIssue] ON PARENT([KeyRiskIndicator]) WHERE [SOXIssue].[Resource ID]={0}
  private static final String QUERY_GET_PARENT_KRI_OF_ISSUE = "Query-Get Parent KRI of Issue";

  private Logger logger = LoggerUtilExtended.getLogger(CloseIssueAction.class.getSimpleName());
  private CommonUtils utils;

  public CloseIssueAction(IWFOperationContext context, List<IWFCustomProperty> properties) {
    super(context, properties);
  }

  /**
   * Implement new CA on Issue WF - close action (re-calculate breach status on KRI - highest from
   * remaining KRI Values with still open issues)
   */
  @Override
  protected void process() throws Exception {
    logger.debug("Entered process()");

    try {
      String settingsFolder = REGISTRY_SETTING_PREFIX
          + (getPropertyValue(SETTINGS_FOLDER_WF_PROPERTY).trim().startsWith(SLASH) ? "" : SLASH)
          + getPropertyValue(SETTINGS_FOLDER_WF_PROPERTY).trim()
          + (getPropertyValue(SETTINGS_FOLDER_WF_PROPERTY).trim().endsWith(SLASH) ? "" : SLASH);
      logger.debug("settingsFolder: " + settingsFolder);

      utils = new CommonUtils(logger, this.getContext().getServiceFactory(), settingsFolder);

      IGRCObject issue = this.getContext().getResource();
      logger.debug("issue id: " + issue.getId() + ", name: " + issue.getName() + "; type: "
          + issue.getType().getName());

      String[] prms = new String[1];
      prms[0] = "'" + issue.getId() + "'";
      String qry = utils.getSetting(QUERY_GET_PARENT_KRI_OF_ISSUE);
      List<String> parentKriRecordAsList = utils.getQueryResultsMultipleReturnFields(qry, prms, 4);

      if (parentKriRecordAsList == null || parentKriRecordAsList.size() == 0) {
        logger.debug("This Issue is not associated to any KRI. Nothing to do - exiting.");
        return;
      }
      
      if (parentKriRecordAsList.size() > 1) {
        logger.debug(
            "Found more than one KRI. Will get first one. This should not have occurred as per the solution(an issue should have just one parent KRI.");
      }

      String kriRecord = parentKriRecordAsList.get(0);
      String kriDbId = utils.getIdOfRecord(kriRecord);
      String kriName = utils.getRecordValueAtIndex(1, kriRecord);
      String currentKriBreachStatus = utils.getRecordValueAtIndex(2, kriRecord);
      String kriLibraryId = utils.getRecordValueAtIndex(3, kriRecord);

      String redSetting = utils
          .getSettingWithDefaultValueIfNull(RED_BREACH_VALUE_TO_ISSUE_PRIORITY_SETTING, "Red=High");
      String orangeSetting = utils.getSettingWithDefaultValueIfNull(
          ORANGE_BREACH_VALUE_TO_ISSUE_PRIORITY_SETTING, "Orange=Medium");
      String yellowSetting = utils.getSettingWithDefaultValueIfNull(
          YELLOW_BREACH_VALUE_TO_ISSUE_PRIORITY_SETTING, "Yellow=Low");
      String greenSetting = utils
          .getSettingWithDefaultValueIfNull(GREEN_BREACH_VALUE_TO_ISSUE_PRIORITY_SETTING, "Green=");

      String red = redSetting.split(CommonUtils.REGEX_EQUALS_PATTERN, -1)[0].trim();
      String orange = orangeSetting.split(CommonUtils.REGEX_EQUALS_PATTERN, -1)[0].trim();
      String yellow = yellowSetting.split(CommonUtils.REGEX_EQUALS_PATTERN, -1)[0].trim();
      String green = greenSetting.split(CommonUtils.REGEX_EQUALS_PATTERN, -1)[0].trim();

      String high = redSetting.split(CommonUtils.REGEX_EQUALS_PATTERN, -1)[1].trim();
      String medium = orangeSetting.split(CommonUtils.REGEX_EQUALS_PATTERN, -1)[1].trim();
      String low = yellowSetting.split(CommonUtils.REGEX_EQUALS_PATTERN, -1)[1].trim();

      logger.debug("kriParent id: " + kriDbId + ", name: " + kriName + ", currentKriBreachStatus: "
          + currentKriBreachStatus + ", kri library id: " + kriLibraryId + "\nredSetting: "
          + redSetting + ", orangeSetting: " + orangeSetting + ", yellowSetting: " + yellowSetting
          + ", greenSetting: " + greenSetting + "\n red: " + red + ", orange: " + orange + ", yellow: "
          + yellow + ", green: " + green + ", high: " + high + ", medium: " + medium + ", low: "
          + low);

      String[] queryParams = new String[1];
      queryParams[0] = "'" + kriDbId + "'";

      boolean honorPrimaryOny =
          utils.getSettingWithDefaultValueIfNull(HONOR_PRIMARY_ONLY_ON_QUERIES_SETTING, "false")
              .equalsIgnoreCase("true");
      String query = utils.getSetting(GET_ALL_UNCLOSED_ISSUES_UNDER_KRI);
      List<String> records =
          utils.getQueryResultsMultipleReturnFields(query, queryParams, 4, honorPrimaryOny);

      String highestBreach = green;
      for (String record : records) {
        if (utils.getIdOfRecord(record).equals("" + issue.getId())) {
          logger.debug(
              "Skipping issue " + record + " from list of open issues because it's being closed.");
          continue;
        }
        String[] recordColumns = record.split(CommonUtils.RECORD_COLUMN_SEPARATOR, -1);

        // NOTE: this must always be in the 3rd col in the query
        String issuePriority = recordColumns[2];

        if (issuePriority.equals(high)) {
          highestBreach = red;
        } else if (issuePriority.equals(medium) && !highestBreach.equals(red)) {
          highestBreach = orange;
        } else if (issuePriority.equals(low) && !highestBreach.equals(red)
            && !highestBreach.equals(orange)) {
          highestBreach = yellow;
        }
      }

      String breachFieldOnKRI = utils.getSettingWithDefaultValueIfNull(
          KRI_BREACH_STATUS_FIELD_SETTING, "OPSS-KRI-Shared:Breach Status");

      logger.debug("highestBreachStatusOnKriValuesWithOpenIssues = " + highestBreach
          + "; breachFieldOnKRI: " + breachFieldOnKRI);

      IGRCObject kriGrcObj = utils.getResourceService().getGRCObject(new Id(kriDbId));
      if (!highestBreach.equals(currentKriBreachStatus)) {
        utils.setFieldValueOnResource(breachFieldOnKRI, highestBreach, kriGrcObj);
        utils.getResourceService().saveResource(kriGrcObj);
        logger.debug("updated Breach Status on KRI to " + highestBreach);
      } else {
        logger.debug(
            "highestBreachStatusOnKriValuesWithOpenIssues is equal to currentKriBreachStatus: "
                + highestBreach + "; nothing to do");
      }
    } catch (Exception e) {
      logger.error("Exception occurred: " + e.getMessage() + "\n Exception cause: " + e.getCause(),
          e);
      throwException(utils.getAppText(GENERIC_ERROR_APP_TEXT_KEY)
          + (e.getMessage() == null ? "" : "\nMessage: " + e.getMessage())
          + (e.getCause() == null ? "" : "\nCause: " + e.getCause()), e);
    } finally {
      logger.debug("Exiting process()");
    }
  }

}
