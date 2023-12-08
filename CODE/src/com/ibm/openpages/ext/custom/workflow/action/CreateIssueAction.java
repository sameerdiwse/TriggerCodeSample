/**
 * IBM Confidential Copyright IBM Corporation 2021 The source code for this program is not published
 * or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S.
 * Copyright Office.
 */
package com.ibm.openpages.ext.custom.workflow.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.log4j.Logger;
import com.ibm.openpages.api.metadata.Id;
import com.ibm.openpages.api.questionnaire.AnswerType;
import com.ibm.openpages.api.questionnaire.IQuestion;
import com.ibm.openpages.api.questionnaire.IQuestionOption;
import com.ibm.openpages.api.questionnaire.IQuestionnaire;
import com.ibm.openpages.api.resource.IGRCObject;
import com.ibm.openpages.api.workflow.actions.AbstractCustomAction;
import com.ibm.openpages.api.workflow.actions.IWFCustomProperty;
import com.ibm.openpages.api.workflow.actions.IWFOperationContext;
import com.ibm.openpages.ext.custom.util.CommonUtils;
import com.openpages.ext.solutions.common.LoggerUtilExtended;

public class CreateIssueAction extends AbstractCustomAction {

  private static final String QUERY_GET_ISSUES_OF_KRI_VALUE_TO_CHECK_COUNT_0 =
      "SELECT [SOXIssue].[Resource ID], [SOXIssue].[Name] FROM [SOXIssue] JOIN [KeyRiskIndicatorValue] ON CHILD([SOXIssue]) WHERE [KeyRiskIndicatorValue].[Resource ID]={0}";

  // this setting doesn't exist by default; can be added if needed
  // if it exists and it's false then honorPrimary will be set to false for the query;
  private static final String HONOR_PRIMARY_ONLY_ON_QUERY_GET_KRI_CHILD_OF_VENDOR_BY_KRI_ID_SETTING =
      "Honor primary only on Query-Get KRI Child of Vendor By KRI ID";

  // /Solutions/Custom/Create Issue Action/
  private static final String SETTINGS_FOLDER_WF_PROPERTY = "config.location";

  // Create Issue Action on the Questionnaire Assessment workflow: please contact the system
  // administrator about this error.
  private static final String GENERIC_ERROR_APP_TEXT_KEY =
      "com.posten.wf.custom.create.issue.action.generic.error.message";

  // No matching KRI Value with KRI ID {0} was found for this Vendor.
  private static final String NO_KRI_FOUND_ERROR_APP_TEXT_KEY =
      "com.posten.wf.custom.create.issue.action.no.kri.found.error.message";

  // SELECT [Vendor].[Resource ID], [Vendor].[Name]
  // FROM [Vendor] JOIN [QuestionnaireAssessment] ON PARENT([Vendor])
  // WHERE [QuestionnaireAssessment].[Resource ID] = {0}
  private static final String GET_VENDOR_PARENT_QUERY_SETTING = "Query-Get Vendor Parent";


  // SELECT [QuestionnaireTemplate].[Resource ID], [QuestionnaireTemplate].[Name]
  // FROM [QuestionnaireTemplate] JOIN [QuestionnaireAssessment] ON CHILD([QuestionnaireTemplate])
  // WHERE [QuestionnaireAssessment].[Resource ID] = {0}
  private static final String GET_QUESTIONNAIRE_TEMPLATE_PARENT_QUERY_SETTING =
      "Query-Get Questionnaire Template Parent";

  // SELECT [KeyRiskIndicatorValue].[Resource ID], [KeyRiskIndicatorValue].[Name],
  // [KeyRiskIndicatorValue].[OPSS-Shared-Lib:Library ID]
  // FROM [KeyRiskIndicatorValue]
  // JOIN [KeyRiskIndicator] ON CHILD([KeyRiskIndicatorValue])
  // JOIN [Vendor] ON CHILD([KeyRiskIndicator])
  // WHERE [KeyRiskIndicator].[OPSS-Shared-Lib:Library ID]= {0}
  // AND [Vendor].[Resource ID] = {1}
  // AND [KeyRiskIndicatorValue].[POS-VRM-KRIV:Question_ID] = {2}
  private static final String GET_KRI_VALUE_QUERY_SETTING =
      "Query-Get KRI Value Under Vendor By KRI Id and Question ID";

  // POS-Qtemp:Type
  private static final String QUESTIONNAIRE_TEMPLATE_TYPE_FIELD_SETTING =
      "Questionnaire Template Type Field";

  // OPSS-VRM:Vendor Owner
  private static final String VENDOR__VENDOR_OWNER_FIELD_SETTING = "Vendor-Vendor Owner Field";

  // OPSS-Qtemp-Shared:Rationale
  private static final String QUESTION_TEMPLATE_RATIONALE_FIELD_SETTING =
      "Question Template Rationale Field";

  // OPSS-Shared-Lib:Library ID
  private static final String QUESTION_TEMPLATE_KRI_ID_FIELD_SETTING =
      "Question Template KRI ID Field";

  // OPSS-Iss:Issue Type
  private static final String ISSUE_TYPE_FIELD_SETTING = "Issue Type Field";

  // OPSS-Iss:Priority
  private static final String ISSUE_PRIORITY_FIELD_NAME_SETTING = "Issue Priority Field";

  // OPSS-Iss:Assignee
  private static final String ISSUE_OWNER_FIELD_SETTING = "Issue Owner Field";

  // Low
  private static final String LOW_PRIORITY_VALUE_SETTING = "Issue Low Priority Value";

  // Medium
  private static final String MEDIUM_PRIORITY_VALUE_SETTING = "Issue Medium Priority Value";

  // High
  private static final String HIGH_PRIORITY_VALUE_SETTING = "Issue High Priority Value";

  // 2
  private static final String WEIGHT_VALUES_FOR_LOW_PRIORITY_SETTING =
      "Question Weight values for Issue Low Priority";

  // 3
  private static final String WEIGHT_VALUES_FOR_MEDIUM_PRIORITY_SETTING =
      "Question Weight values for Issue Medium Priority";

  // 4,5
  private static final String WEIGHT_VALUES_FOR_HIGH_PRIORITY_SETTING =
      "Question Weight values for Issue High Priority";

  // 2
  private static final String WEIGHT_THRESHOLD_FOR_ISSUE_CREATION_SETTING =
      "Weight Threshold for Issue Creation";

  // 1
  private static final String UNFAVOURABLE_ANSWER_SCORE_SETTING = "Unfavourable Answer Score";

  private static final String ISSUE_OBJECT_TYPE = "SOXIssue";
  private static final String SLASH = "/";
  private static final String REGISTRY_SETTING_PREFIX = "/OpenPages";

  // POS-VRM-Issue:Business Unit Owner
  private static final String ISSUE_BUSINESS_UNIT_OWNER_FIELD_NAME_SETTING =
      "Issue Business Unit Owner Field";

  // OPSS-VRM:Business Unit Owner
  private static final String VENDOR_BUSINESS_UNIT_OWNER_FIELD_SETTING =
      "Vendor Business Unit Owner Field";

  // OPSS-Shared-Basel:Risk Category
  private static final String KRI_RISK_CATEGORY_FIELD_SETTING = "KRI Risk Category Field";

  // OPSS-Shared-Basel:Risk Category
  private static final String ISSUE_RISK_CATEGORY_FIELD_SETTING = "Issue Risk Category Field";

  // Yes
  private static final String LINK_TO_KRI_ALSO_SETTING = "Link to KRI also ?";

  private Logger logger = LoggerUtilExtended.getLogger(CreateIssueAction.class.getSimpleName());
  private CommonUtils utils;

  private IQuestionnaire qa = null;
  private IQuestionnaire qt = null;

  private IGRCObject qaGrcObj = null;
  private IGRCObject vendorParent = null;
  private IGRCObject qtGrcObj = null;

  private int weightThreshold;
  private int unfavourableAnswerValue;

  public CreateIssueAction(IWFOperationContext context, List<IWFCustomProperty> properties) {
    super(context, properties);
  }

  /**
   * Technical Specifications are defined in <i>Posten
   * Posten CA-Issue-01-Issue_Creation_Technical_Specification.xls</i> <br>
   *
   * When a Vendor completes the Questionnaire Assessment of type Audit, Background Check,
   * Inspection or Self Assessment and advances the workflow, after the CreateKriAction executes(CA-KRI-02), this custom action will be invoked.
   * <br>
   */
  @Override
  protected void process() throws Exception {
    logger.error("Entered process()");

    try {
      String settingsFolder = REGISTRY_SETTING_PREFIX
          + (getPropertyValue(SETTINGS_FOLDER_WF_PROPERTY).trim().startsWith(SLASH) ? "" : SLASH)
          + getPropertyValue(SETTINGS_FOLDER_WF_PROPERTY).trim()
          + (getPropertyValue(SETTINGS_FOLDER_WF_PROPERTY).trim().endsWith(SLASH) ? "" : SLASH);
      logger.error("settingsFolder: " + settingsFolder);

      utils = new CommonUtils(logger, this.getContext().getServiceFactory(), settingsFolder);

      qaGrcObj = this.getContext().getResource();
      logger.error("questionnaireAssessmentGrcObject id: " + qaGrcObj.getId() + ", name: "
          + qaGrcObj.getName());

      qa = utils.getQuestionnaireService().getQuestionnaire(qaGrcObj.getId());
      logger.error("questionnaireAssessment - Description: " + qa.getDescription() + ", title: "
          + qa.getTitle() + ", ID: " + qa.getId() + ", completionRequired: "
          + qa.getCompletionRequired() + ", rationale: " + qa.getRationale());

      List<IQuestion> questions = qa.getQuestions();
      if (questions == null || questions.size() == 0) {
        logger.error("No questions found. Nothing to do.\nExiting process()");
        return;
      }

      vendorParent = getVendorParentOfQuestionnaireAssessment(qaGrcObj.getId());
      qtGrcObj = getLinkedQuestionnaireTemplate(qaGrcObj.getId());
      qt = utils.getQuestionnaireService().getQuestionnaire(qtGrcObj.getId());
      logger.error("questionnaireTemplate - Description: " + qt.getDescription() + ", title: "
          + qt.getTitle() + ", ID: " + qt.getId() + ", completionRequired: "
          + qt.getCompletionRequired() + ", rationale: " + qt.getRationale());

      weightThreshold =
          Integer.parseInt(utils.getSetting(WEIGHT_THRESHOLD_FOR_ISSUE_CREATION_SETTING).trim());

      unfavourableAnswerValue =
          Integer.parseInt(utils.getSetting(UNFAVOURABLE_ANSWER_SCORE_SETTING).trim());

      for (int i = 0; i < questions.size(); i++) {
        IQuestion question = questions.get(i);
        logger.error(//
            question == null ? ("Skipping questions at index " + i + " because it's null")//
                : "index " + i //
                    + " question -> ID: " + question.getId()//
                    + ", DESCRIPTION: \"" + question.getDescription() + "\"" //
                    + ", \nTITLE: " + question.getTitle() //
                    + ", WEIGHT: " + question.getWeight()//
                    + ", SCORE: " + question.getScore() //
                    + ", STATUS: " + question.getStatus()//
                    + ", ANSWER TYPE: " + question.getAnswerType() //
                    + ", ANSWER: " + question.getAnswer() //
                    + ", RATIONALE: " + question.getRationale() //
                    + ", REFERENCE: " + question.getReference());

        if (issueMustBeCreated(question)) {
          createIssue(question, i);
        }
      }
    } catch (Exception e) {
      logger.error("Exception occurred: " + e.getMessage() + "\n Exception cause: " + e.getCause(),
          e);
      throwException(utils.getAppText(GENERIC_ERROR_APP_TEXT_KEY)
          + (e.getMessage() == null ? "" : "\nMessage: " + e.getMessage())
          + (e.getCause() == null ? "" : "\nCause: " + e.getCause()), e);
    } finally {
      logger.error("Exiting process()");
    }
  }

  private void createIssue(IQuestion question, int currentQuestionIdx) throws Exception {
    logger.error("Entered createIssue(question with ID " + question.getId() + ",currentQuestionIdx:"
        + currentQuestionIdx + ")");
    List<IQuestion> questionTemplates = qt.getQuestions();
    if (questionTemplates == null) {
      logger.error("questionnaire template doesn't contain any questions");
    }

    IQuestion questionTemplate = questionTemplates.get(currentQuestionIdx);
    // TODO - ensure more if this is the matching QT for current QA by comparing title or
    // description ?

    if (questionTemplate == null) {
      logger.error("questionTemplate at index " + currentQuestionIdx + " is null.");
    }

    // get related objects to copy from, to the issue ; Vendor is not fetched here, it was fetched
    // earlier and it's stored in an instance Java class variable
    IGRCObject questionTemlateObject =
        utils.getResourceService().getGRCObject(questionTemplate.getId());
    String kriId = utils.getFieldValueAsString(
        utils.getSetting(QUESTION_TEMPLATE_KRI_ID_FIELD_SETTING), questionTemlateObject);
    logger.error("Sumit: kriId = "+kriId);
    String kriValueId = getRelatedKriValue(kriId, "" + question.getId(), "" + vendorParent.getId());
    logger.error("Sumit: kriValueId = "+kriValueId);
    IGRCObject relatedKriValue = utils.getResourceService().getGRCObject(new Id(kriValueId));
    IGRCObject relatedKri =
        utils.getResourceService().getGRCObject(relatedKriValue.getPrimaryParent());

    // get Issue field names to copy to, from other objects
    String issueOwnerFieldName = utils.getSetting(ISSUE_OWNER_FIELD_SETTING);
    String issueTypeFieldName = utils.getSetting(ISSUE_TYPE_FIELD_SETTING);
    String issuePriorityFieldName = utils.getSetting(ISSUE_PRIORITY_FIELD_NAME_SETTING);
    String issueBusinessOwnerFieldName =
        utils.getSetting(ISSUE_BUSINESS_UNIT_OWNER_FIELD_NAME_SETTING);
    String issueRiskCategoryFieldName = utils.getSetting(ISSUE_RISK_CATEGORY_FIELD_SETTING);

    // get values from related objects, to copy to fields on the new Issue
    String rationale = utils.getFieldValueAsString(
        utils.getSetting(QUESTION_TEMPLATE_RATIONALE_FIELD_SETTING), questionTemlateObject);
    String vendorOwner = utils
        .getFieldValueAsString(utils.getSetting(VENDOR__VENDOR_OWNER_FIELD_SETTING), vendorParent);
    String assessmentType = utils.getFieldValueAsString(
        utils.getSetting(QUESTIONNAIRE_TEMPLATE_TYPE_FIELD_SETTING), qtGrcObj);
    String priority = calculatePriority(question.getWeight());
    String businessUnitOwner = utils.getFieldValueAsString(
        utils.getSetting(VENDOR_BUSINESS_UNIT_OWNER_FIELD_SETTING), vendorParent);
    String kriRiskCategory =
        utils.getFieldValueAsString(utils.getSetting(KRI_RISK_CATEGORY_FIELD_SETTING), relatedKri);

    IGRCObject issue = utils.getResourceFactory()
        .createAutoNamedGRCObject(utils.getMetaDataService().getType(ISSUE_OBJECT_TYPE));
    issue.setPrimaryParent(vendorParent.getId());
    issue.setDescription(rationale);
    utils.setFieldValueOnResource(issueOwnerFieldName, vendorOwner, issue);
    utils.setFieldValueOnResource(issueTypeFieldName, assessmentType, issue);
    utils.setFieldValueOnResource(issuePriorityFieldName, priority, issue);
    utils.setFieldValueOnResource(issueBusinessOwnerFieldName, businessUnitOwner, issue);
    utils.setFieldValueOnResource(issueRiskCategoryFieldName, kriRiskCategory, issue);

    issue = utils.getResourceService().saveResource(issue);
    logger.error("Saved issue named " + issue.getName() + ", having ID " + issue.getId());

    if (utils.getSettingWithDefaultValueIfNull(LINK_TO_KRI_ALSO_SETTING, "Yes")
        .equalsIgnoreCase("Yes")) {
      utils.getResourceService().associate(issue.getId(), Arrays.asList(relatedKri.getId()),
          new ArrayList<Id>());
      logger.error("Associated the new issue to KRI " + relatedKri.getName() + "having id "
          + relatedKri.getId());
    }

    utils.getResourceService().associate(issue.getId(), Arrays.asList(relatedKriValue.getId()),
        new ArrayList<Id>());

    logger.error("Associated the new issue to KRI Value " + relatedKriValue.getName() + "having id "
        + relatedKriValue.getId() + ". Exiting createIssue()");
  }

  private String getRelatedKriValue(String libraryId, String questionId, String vendorId)
      throws Exception {
    logger.error("Entered getRelatedKriValue(libraryId(aka KRI ID):" + libraryId + ",questionId:"
        + questionId + ", vendorId: " + vendorId + ")");

    String[] queryParams = new String[3];
    queryParams[0] = "'" + libraryId + "'";
    queryParams[1] = "'" + vendorId + "'";
    queryParams[2] = "'" + questionId + "'";

    boolean honorPrimaryOny = true;
    if (utils
        .getSettingTrimmedEmptyStringIfNull(
            HONOR_PRIMARY_ONLY_ON_QUERY_GET_KRI_CHILD_OF_VENDOR_BY_KRI_ID_SETTING)
        .equalsIgnoreCase("false")) {
      honorPrimaryOny = false;
    }
    String query = utils.getSetting(GET_KRI_VALUE_QUERY_SETTING);
    logger.error("Sumit: query = "+query);
    List<String> kriValueRecord =
        utils.getQueryResultsMultipleReturnFields(query, queryParams, 3, honorPrimaryOny);
    if (kriValueRecord.size() == 0) {
      throw new Exception(utils.replacePlaceholdersWithValues(
          utils.getAppText(NO_KRI_FOUND_ERROR_APP_TEXT_KEY), new Object[] {"'" + libraryId + "'"}));
    }

    int idx = 0;
    if (kriValueRecord.size() > 1) {
      logger.error(
          "Found more than one KRI Value. Will get first one one with no linked issues(ie, the newest one).");
      for (int i = 0; i < kriValueRecord.size(); i++) {
        String id = utils.getIdOfRecord(kriValueRecord.get(i));
        logger.error("Sumit: id = "+id);
        String[] prms = new String[1];
        prms[0] = "'" + id + "'";
        String qry = QUERY_GET_ISSUES_OF_KRI_VALUE_TO_CHECK_COUNT_0;
        logger.error("Sumit: qry = "+qry);
        List<String> childrenIssues =
            utils.getQueryResultsMultipleReturnFields(qry, prms, 2);

        logger.error("children issues: " + childrenIssues);
        if (childrenIssues.size() == 0) {
          idx = i;
          break;
        }
      }
    }

    String id = kriValueRecord.get(idx).split(CommonUtils.RECORD_COLUMN_SEPARATOR, -1)[0];

    logger.error("Exiting getRelatedKriValue() - returning " + id);
    return id;
  }

  @SuppressWarnings("unchecked")
  private boolean issueMustBeCreated(IQuestion question) {
    logger.error("Entered issueMustBeCreated(question: title " + question.getTitle() + ", id: "
        + question.getId() + ", description: " + question.getDescription() + ", answer type: "
        + question.getAnswerType() + ", weight: " + question.getWeight() + ", rationale: "
        + question.getRationale() + ", answer: " + question.getAnswer() + ")");

    Object answerObj = question.getAnswer();
    if (answerObj == null) {
      logger.error(
          "answerObj is null; skipping to next question\nExiting issueMustBeCreated() - returning false");
      return false;
    }

    if (question.getAnswerType() != AnswerType.RADIO
        && question.getAnswerType() != AnswerType.MULTI_CHECK) {
      logger.error(
          "answer type is NOT RADIO and NOT MULTI_CHECK eiher. It's " + question.getAnswerType()
              + ". Spec doesn't cover this case..\nExiting issueMustBeCreated() - returning false");
      return false;
    }

    List<IQuestionOption> questionOptions = question.getOptions();
    if (questionOptions == null) {
      logger.error(
          "questionOptions is null for the current question.\nExiting issueMustBeCreated() - returning false");
      return false;
    }

    for (int j = 0; j < questionOptions.size(); j++) {
      IQuestionOption questionOption = questionOptions.get(j);
      logger.error(questionOption == null ? "questionOption at idx " + j + " is null"
          : "questionOption at idx " + j + " -> label: " + questionOption.getLabel()
              + ", description: " + questionOption.getDescription() + ", score: "
              + questionOption.getScore() + ", value: " + ", " + questionOption.getValue()
              + ", getRequires:" + questionOption.getRequires());
    }

    boolean foundAnUnfavourableAnswer = false;

    if (question.getAnswerType() == AnswerType.RADIO) {
      int answer = ((Integer) answerObj).intValue();
      int score = questionOptions.get(answer).getScore();
      if (score == unfavourableAnswerValue) {
        foundAnUnfavourableAnswer = true;
        logger.error("the single selection answer is unfavourable");
      }

    } else if (question.getAnswerType() == AnswerType.MULTI_CHECK) {
      // if AnswerType is MULTI_CHECK we know answerObj is List<Integer>
      List<Integer> answerList = (List<Integer>) answerObj;
      for (int k = 0; k < answerList.size(); k++) {
        if (answerList.get(k) == null) {
          logger.error("asnwer at index " + k + " is null; skipping it");
          continue;
        }

        int score = questionOptions.get(answerList.get(k)).getScore();

        if (score == unfavourableAnswerValue) {
          foundAnUnfavourableAnswer = true;
          logger.error("at least one of the multi selection answers is unfavourable: index " + k
              + "; breaking answers loop");
          break;
        }
      }
    }

    boolean result = question.getWeight() >= weightThreshold && foundAnUnfavourableAnswer;

    logger.error("Exiting issueMustBeCreated() - returning " + result);
    return result;
  }

  private String calculatePriority(int weight) {
    logger.error("Entered calculatePriority(weight: " + weight + ")");
    String priority = "";

    List<String> highWeightValues =
        utils.getSettingCsvAsList(WEIGHT_VALUES_FOR_HIGH_PRIORITY_SETTING);
    List<String> mediumWeightValues =
        utils.getSettingCsvAsList(WEIGHT_VALUES_FOR_MEDIUM_PRIORITY_SETTING);
    List<String> lowWeightValues =
        utils.getSettingCsvAsList(WEIGHT_VALUES_FOR_LOW_PRIORITY_SETTING);

    if (highWeightValues.contains("" + weight)) {
      priority = utils.getSetting(HIGH_PRIORITY_VALUE_SETTING);
    } else if (mediumWeightValues.contains("" + weight)) {
      priority = utils.getSetting(MEDIUM_PRIORITY_VALUE_SETTING);
    } else if (lowWeightValues.contains("" + weight)) {
      priority = utils.getSetting(LOW_PRIORITY_VALUE_SETTING);
    }

    logger.error("Exiting calculatePriority() -> returning " + priority);
    return priority;
  }

  private IGRCObject getLinkedQuestionnaireTemplate(Id qaId) throws Exception {
    logger.error("Entered getLinkedQuestionnaireTemplate(questionnaireAssessmentId: " + qaId + ")");

    String questionnaireTemplateId = null;
    String[] queryParams = new String[1];
    queryParams[0] = "'" + qaId + "'";

    String query = utils.getSetting(GET_QUESTIONNAIRE_TEMPLATE_PARENT_QUERY_SETTING);
    List<String> records = utils.getQueryResultsMultipleReturnFields(query, queryParams, 2);
    if (records.size() < 1) {
      throw new Exception(
          "Didn't find any Questionnaire Template linked to this questionnaire assessment.");
    } else {
      if (records.size() > 1) {
        logger.warn(
            "This should never happen. Found more than one linked Questionnaire Template. Will get first one. ");
      }
      questionnaireTemplateId = records.get(0).split(CommonUtils.RECORD_COLUMN_SEPARATOR, -1)[0];
    }

    IGRCObject questionnaireTemplateGrcObject =
        utils.getResourceService().getGRCObject(new Id(questionnaireTemplateId));
    if (questionnaireTemplateGrcObject == null) {
      throw new Exception("questionnaireTemplateObject is null");
    }

    logger.error(
        "Exiting getLinkedQuestionnaireTemplate() -> returning questionnaireTemplateGrcObject named "
            + questionnaireTemplateGrcObject.getName() + ", having ID "
            + questionnaireTemplateGrcObject.getId());
    return utils.getResourceService().getGRCObject(new Id(questionnaireTemplateId));
  }

  private IGRCObject getVendorParentOfQuestionnaireAssessment(Id id) throws Exception {
    logger.error("Entered getVendorParentOfQuestionnaireAssessment(" + id + ")");

    String[] queryParams = new String[1];
    queryParams[0] = "'" + id + "'";
    boolean honorPrimaryOny = true;

    String query = utils.getSetting(GET_VENDOR_PARENT_QUERY_SETTING);
    List<String> vendorParentAsList =
        utils.getQueryResultsMultipleReturnFields(query, queryParams, 2, honorPrimaryOny);
    if (vendorParentAsList.size() == 0) {
      String errorMessage = "The Questionnaire Assessment doesn't have any Vendor parent.";
      throw new Exception(errorMessage);
    }

    if (vendorParentAsList.size() > 1) {
      logger.warn(
          "Found more than one primary parent vendor. Will get first one. This should not happen.");
    }

    String vendorId = vendorParentAsList.get(0).split(CommonUtils.RECORD_COLUMN_SEPARATOR, -1)[0];
    IGRCObject vendor = utils.getResourceService().getGRCObject(new Id(vendorId));

    logger.error("Exiting getVendorParentOfQuestionnaireAssessment() - returning vendor named "
        + vendor.getName() + ", having ID " + vendor.getId());
    return vendor;
  }
}
