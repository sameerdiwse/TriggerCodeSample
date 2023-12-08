/**
 * IBM Confidential Copyright IBM Corporation 2021 The source code for this program is not published
 * or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S.
 * Copyright Office.
 */
package com.ibm.openpages.ext.custom.workflow.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.log4j.Logger;
import com.ibm.openpages.api.application.CopyConflictBehavior;
import com.ibm.openpages.api.application.CopyObjectOptions;
import com.ibm.openpages.api.metadata.Id;
import com.ibm.openpages.api.questionnaire.AnswerType;
import com.ibm.openpages.api.questionnaire.IQuestion;
import com.ibm.openpages.api.questionnaire.IQuestionOption;
import com.ibm.openpages.api.questionnaire.IQuestionnaire;
import com.ibm.openpages.api.resource.IGRCObject;
import com.ibm.openpages.api.security.IUser;
import com.ibm.openpages.api.workflow.actions.AbstractCustomAction;
import com.ibm.openpages.api.workflow.actions.IWFCustomProperty;
import com.ibm.openpages.api.workflow.actions.IWFOperationContext;
import com.ibm.openpages.ext.custom.util.CommonUtils;
import com.ibm.openpages.ext.custom.util.EmailUtilities;
import com.openpages.ext.solutions.common.LoggerUtilExtended;

public class CreateKriAction extends AbstractCustomAction {

  // /Solutions/Custom/Create KRI Action/
  private static final String SETTINGS_FOLDER_WF_PROPERTY = "config.location";

  // SELECT [Vendor].[Resource ID], [Vendor].[Name]
  // FROM [Vendor] JOIN [QuestionnaireAssessment] ON PARENT([Vendor])
  // WHERE [QuestionnaireAssessment].[Resource ID] = {0}
  private static final String GET_VENDOR_PARENT_QUERY_SETTING = "Query-Get Vendor Parent";


  // SELECT [QuestionnaireTemplate].[Resource ID], [QuestionnaireTemplate].[Name]
  // FROM [QuestionnaireTemplate] JOIN [QuestionnaireAssessment] ON CHILD([QuestionnaireTemplate])
  // WHERE [QuestionnaireAssessment].[Resource ID] = {0}
  private static final String GET_QUESTIONNAIRE_TEMPLATE_PARENT_QUERY_SETTING =
      "Query-Get Questionnaire Template Parent";

  // SELECT [KeyRiskIndicator].[Resource ID], [KeyRiskIndicator].[Name],
  // [KeyRiskIndicator].[OPSS-Shared-Lib:Library ID],[KeyRiskIndicator].[OPSS-KRI-Shared:Breach
  // Status]
  // FROM [KeyRiskIndicator] JOIN [Vendor] ON CHILD([KeyRiskIndicator])
  // WHERE [KeyRiskIndicator].[OPSS-Shared-Lib:Library ID]= {0}
  // AND [Vendor].[Resource ID] = {1}
  private static final String GET_KRI_CHILD_OF_VENDOR_BY_KRI_ID_QUERY_SETTING =
      "Query-Get KRI Child of Vendor By KRI ID";

  // SELECT [KeyRiskIndicator].[Resource ID], [KeyRiskIndicator].[Name],
  // [KeyRiskIndicator].[OPSS-Shared-Lib:Library ID],[KeyRiskIndicator].[OPSS-KRI-Shared:Breach
  // Status]
  // FROM [KeyRiskIndicator] JOIN [Vendor] ON CHILD([KeyRiskIndicator])
  // WHERE [Vendor].[Resource ID] = {0}
  private static final String GET_ALL_VENDOR_CHILD_KRIS_QUERY_SETTING =
      "Query-Get All Vendor Child KRIs";

  // SELECT [KeyRiskIndicator].[Resource ID], [KeyRiskIndicator].[Name],
  // [KeyRiskIndicator].[OPSS-Shared-Lib:Library ID]
  // FROM [KeyRiskIndicator]
  // WHERE [KeyRiskIndicator].[OPSS-Shared-Lib:Library ID]={0}
  // AND [KeyRiskIndicator].[Location] LIKE ''/Library/KRI Library%''
  private static final String GET_KRI_FROM_LIBRARY_BY_KRI_ID_QUERY_SETTING =
      "Query-Get KRI From Library By KRI ID";

  // SELECT [KeyRiskIndicator].[Resource ID], [KeyRiskIndicator].[Name],
  // [KeyRiskIndicator].[OPSS-Shared-Lib:Library ID],[KeyRiskIndicator].[OPSS-KRI-Shared:Breach
  // Status]
  // FROM [KeyRiskIndicator]
  // WHERE [KeyRiskIndicator].[Location] LIKE ''/Library/KRI Library%''
  private static final String GET_ALL_KRIs_FROM_LIBRARY_QUERY_SETTING =
      "Query-Get All KRIs From Library";

  // OPSS-VRM:Vendor Owner,OPSS-VRM:Business Unit Owner
  private static final String VENDOR_SOURCE_FIELDS_CSV_LIST_TO_COPY_FROM =
      "Vendor-Source Fields CSV List To Copy From";

  // OPSS-KRI:Owner, OPSS-VRM:Business Unit Owner
  private static final String KRI_DESTINATION_FIELDS_CSV_LIST_TO_COPY_TO =
      "KRI-Destination Fields CSV List To Copy To";

  // OPSS-KRIVal:KRI Owner, POS-VRM-KRIV:Business Unit Owner
  private static final String KRI_VALUE_DESTINATION_FIELDS_CSV_LIST_TO_COPY_TO =
      "KRI Value-Destination Fields CSV List To Copy To";

  // OPSS-Shared-Lib:Library ID
  private static final String QUESTION_TEMPLATE_KRI_ID_FIELD_SETTING =
      "Question Template KRI ID Field";

  // OPSS-Shared-Lib:Library ID
  private static final String KRI__KRI_ID_FIELD_SETTING = "KRI-KRI ID Field";

  // OPSS-VRM:Vendor Owner
  private static final String USER_FIELD_TO_EMAIL_ON_VENDOR = "User Field on Vendor to Email";

  // 2
  private static final String WEIGHT_THRESHOLD_FOR_KRI_CREATION_SETTING =
      "Weight Threshold for KRI Creation";

  // 1
  private static final String UNFAVOURABLE_ANSWER_SCORE_SETTING = "Unfavourable Answer Score";

  // 2
  private static final String WEIGHT_VALUES_FOR_YELLOW_BREACH_SETTING =
      "Question Weight values for Yellow Breach";

  // 3
  private static final String WEIGHT_VALUES_FOR_ORANGE_BREACH_SETTING =
      "Question Weight values for Orange Breach";

  // 4,5
  private static final String WEIGHT_VALUES_FOR_RED_BREACH_SETTING =
      "Question Weight values for Red Breach";

  // Green
  private static final String GREEN_BREACH_VALUE_SETTING = "Green Breach Value";

  // Yellow
  private static final String YELLOW_BREACH_VALUE_SETTING = "Yellow Breach Value";

  // Orange
  private static final String ORANGE_BREACH_VALUE_SETTING = "Orange Breach Value";

  // Red
  private static final String RED_BREACH_VALUE_SETTING = "Red Breach Value";

  // OPSS-KRI-Shared:Breach Status
  private static final String KRI_BREACH_STATUS_FIELD_SETTING = "KRI Breach Status Field";

//true
  private static final String COPY_NON_ASSESSED_KRIS_FROM_LIBRARY =
      "Copy non assessed KRIs from library";

  // this setting doesn't exist by default; can be added if needed
  // if it exists and it's false then honorPrimary will be set to false for the query;
  private static final String HONOR_PRIMARY_ONLY_ON_QUERY_GET_KRI_CHILD_OF_VENDOR_BY_KRI_ID_SETTING =
      "Honor primary only on Query-Get KRI Child of Vendor By KRI ID";

  // Create KRI Action on the Questionnaire Assessment workflow: please contact the system
  // administrator about this error.
  private static final String GENERIC_ERROR_APP_TEXT_KEY =
      "com.posten.wf.custom.create.kri.action.generic.error.message";

  // Missing KRI Id on Question Template
  private static final String EMPTY_KRI_ID_ON_QT_EMAIL_SUBJECT_APP_TEXT =
      "com.posten.wf.custom.create.kri.action.email.subject.missing.kri.id.on.question.template";

  // <p>
  // Hi{0},<br>
  // There is no KRI ID set on Question Template Titled {1}, under Questionnaire Template titled
  // {2}.<br>
  // Because of that the Workflow Custom Action CA-02-KRI cannot create a KRI.<br>
  // </p>
  // <p>
  // Please ask the system administrator to set a KRI ID on the respective Question Template.<br>
  // </p>
  private static final String EMPTY_KRI_ID_ON_QT_EMAIL_BODY_APP_TEXT =
      "com.posten.wf.custom.create.kri.action.email.body.missing.kri.id.on.question.template";


  // KRI not found in KRI Library
  private static final String KRI_NOT_FOUND_IN_LIBRRAY_EMAIL_SUBJECT_APP_TEXT =
      "com.posten.wf.custom.create.kri.action.email.subject.kri.not.found.in.library";

  // <p>
  // Hi{0},<br>
  // There is an issue on the KRI Library: the KRI having KRI ID {1} was not found.<br>
  // This was detected by the Workflow Custom Action CA-02-KRI which was trying to create a KRI for
  // Question Template Titled {2}, under Questionnaire Template titled {3}. <br>
  // </p>
  // <p>
  // Please let the system administrator know about this missing KRI.
  // </p>
  private static final String KRI_NOT_FOUND_IN_LIBRRAY_EMAIL_BODY_APP_TEXT =
      "com.posten.wf.custom.create.kri.action.email.body.kri.not.found.in.library";

  private static final String SLASH = "/";
  private static final String REGISTRY_SETTING_PREFIX = "/OpenPages";


  private static final String KRI_VALUE_OBJECT_TYPE = "KeyRiskIndicatorValue";

  // POS-VRM-KRIV:Question_ID
  private static final String KRI_VALUE_QUESTION_ID_FIELD_SETTING = "KRI Value Question Id Field";

  // POS-VRM-KRIV:Questionnaire Type
  private static final String KRI_VALUE_TYPE_FIELD_SETTING = "KRI Value Type Field";

  // POS-Qtemp:Type
  private static final String QUESTIONNAIRE_TEMPLATE_TYPE_FIELD_SETTING =
      "Questionnaire Template Type Field";

  // false
  private static final String ENABLE_EMAIL_NOTIFICATION_ON_KRI_ID_PROBLEMS_SETTING =
      "Enable Email Notification On KRI ID Problems";

  // SELECT [SOXBusEntity].[Resource ID], [SOXBusEntity].[Name] FROM [SOXBusEntity] WHERE
  // [SOXBusEntity].[Name]='Vendors' AND [SOXBusEntity].[Location]='/Vendors'
  private static final String QUERY_GET_VENDORS_ENTITY_SETTING = "Query-Get Vendors Entity";

  private Logger logger = LoggerUtilExtended.getLogger(CreateKriAction.class.getSimpleName());
  private CommonUtils utils;

  private IQuestionnaire qa = null;
  private IQuestionnaire qt = null;

  private IGRCObject qaGrcObj = null;
  private IGRCObject vendorParent = null;
  private IGRCObject qtGrcObj = null;

  private int weightThreshold;
  private int unfavourableAnswerValue;

  private String kriId;

  private EmailUtilities emailUtilities;

  private String vendordEntityId;

  List<String> allKrisInLibrary = new ArrayList<>();
  private List<String> libraryIdsOfAllKrisUnderVendor = new ArrayList<>();
  private List<String> libraryIdsOfAllKrisReferencedByQuestionnaire = new ArrayList<>();

  public CreateKriAction(IWFOperationContext context, List<IWFCustomProperty> properties) {
    super(context, properties);
  }

  /**
   * Technical Specifications are defined in <i> Posten
   * CA-KRI-02-KRI_Creation_Technical_Specification.xls</i> <br>
   * 
   * When a Vendor completes the Questionnaire Assessment of type Audit, Background Check,
   * Inspection or Self Assessment and advances the workflow, this workflow custom action will
   * execute. <br>
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

      emailUtilities =
          new EmailUtilities(utils.getConfigurationService(), utils.getCurrentUserLocale(), logger);

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
          Integer.parseInt(utils.getSetting(WEIGHT_THRESHOLD_FOR_KRI_CREATION_SETTING).trim());

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
                    + ", ORDER: " + question.getOrder() //
                    + ", REFERENCE: " + question.getReference());

        kriId = getKriIdFromQuestionTemplate(i);
        logger.error("Sumit: kriId = "+kriId);
        if (kriMustBeUpdatedOrCreated(question)) {
          if (kriId == null || kriId.trim().isEmpty()) {
            logger.error(
                "KRI ID is empty on the current question's template; an email notification will be sent sent - skipping KRI creation/update.");
            if (utils
                .getSettingWithDefaultValueIfNull(
                    ENABLE_EMAIL_NOTIFICATION_ON_KRI_ID_PROBLEMS_SETTING, "false")
                .equalsIgnoreCase("true")) {
              String recipientUserFieldSetting = utils.getSetting(USER_FIELD_TO_EMAIL_ON_VENDOR);
              String recipientUserFieldValue =
                  utils.getFieldValueAsString(recipientUserFieldSetting, vendorParent);
              if (recipientUserFieldValue == null || recipientUserFieldValue.trim().isEmpty()) {
                logger.warn("Can't send email because recipientUserField value is empty");
                continue;
              }

              IUser recipientUser = utils.getSecurityService().getUser(recipientUserFieldValue);
              String name = " " + (recipientUser.getFirstName() != null
                  && !recipientUser.getFirstName().trim().isEmpty() ? recipientUser.getFirstName()
                      : recipientUser.getName());
              IQuestion questionTemplate = qt.getQuestions().get(i);
              IGRCObject questionTemplateGrcObj = utils.getObjectById(questionTemplate.getId());
              String questionTemplateIdWithLink = "<a href=\"" + utils.getObjectTaskViewUrlPrefix()
                  + questionTemplate.getId() + "\">" + questionTemplateGrcObj.getName() + "</a>";

              String questionnaireTemplateIdWithLink =
                  "<a href=\"" + utils.getObjectTaskViewUrlPrefix() + qt.getId() + "\">"
                      + qtGrcObj.getName() + "</a>";

              List<String> placeholdersValues = new ArrayList<String>();
              placeholdersValues.add(name);
              placeholdersValues.add(questionTemplateIdWithLink);
              placeholdersValues.add(questionnaireTemplateIdWithLink);

              emailUtilities.notifyByEmail(recipientUser, EMPTY_KRI_ID_ON_QT_EMAIL_SUBJECT_APP_TEXT,
                  EMPTY_KRI_ID_ON_QT_EMAIL_BODY_APP_TEXT, placeholdersValues);
            }
            continue;
          }

          createOrUpdateKri(question, i);
        }

        if (kriId != null && !kriId.trim().isEmpty()
            && !libraryIdsOfAllKrisReferencedByQuestionnaire.contains(kriId)) {
          libraryIdsOfAllKrisReferencedByQuestionnaire.add(kriId);
        }
      }

      libraryIdsOfAllKrisUnderVendor = getLibraryIdsOfAllKrisUnderVendor();
      allKrisInLibrary = getAllKriRecordsFromLibrary();

      logger.error("libraryIdsOfAllKrisUnderVendor:" + libraryIdsOfAllKrisUnderVendor
          + "\n\n allKrisInLibrary: " + allKrisInLibrary
          + "\n\n libraryIdsOfAllKrisReferencedByQuestionnaire: "
          + libraryIdsOfAllKrisReferencedByQuestionnaire);
      for (String kriRecord : allKrisInLibrary) {
        String id = utils.getIdOfRecord(kriRecord);
        String kriLibraryId = utils.getRecordValueAtIndex(2, kriRecord);

        // copy from library KRIs referenced by questionnaire by KRI ID and set their Breach Status
        // to Green
        if (!libraryIdsOfAllKrisUnderVendor.contains(kriLibraryId)
            && libraryIdsOfAllKrisReferencedByQuestionnaire.contains(kriLibraryId)) {
          CopyObjectOptions copyObjectOptions = CopyObjectOptions.getDefaultOptions();
          copyObjectOptions.setConflictBehavior(CopyConflictBehavior.USEEXISTING);

          List<IGRCObject> copyResult = utils.getApplicationService().copyToParent(
              new Id(getVendorsEntityId()), Arrays.asList(new Id(id)), copyObjectOptions);
          IGRCObject copiedKri = copyResult.get(0);
          logger.error(
              "Copied the green breach status KRI from library to Vendor's primary parent(/Vendors). Library KRI record: "
                  + kriRecord + "; kriLibraryId:" + kriLibraryId
                  + ". Will next make the vendor its primary parent; current(copied) KRI path: "
                  + copiedKri.getPath() + "; id: " + copiedKri.getId());

          utils.getResourceService().dissociate(copiedKri.getId(),
              Arrays.asList(new Id(getVendorsEntityId())), Collections.emptyList());
          logger.error("Dissociated green breach status KRI from parent entity '/Vendors'(id "
              + getVendorsEntityId() + ")");

          copiedKri.setPrimaryParent(vendorParent);
          utils.saveResorce(copiedKri);
          logger.error("set current Vendor(" + vendorParent.getName()
              + ") as primary parent of the green breach status KRI");

          utils.setFieldValuesOnGrcObjectWithOnlyTheseFieldsAndSave(
              utils.getSetting(KRI_BREACH_STATUS_FIELD_SETTING),
              utils.getSetting(GREEN_BREACH_VALUE_SETTING), "" + copiedKri.getId());

          libraryIdsOfAllKrisUnderVendor.add(kriLibraryId);
        }
      }

      String copyNonAssessedKrisFromLibrarySetting =
          utils.getSettingWithDefaultValueIfNull(COPY_NON_ASSESSED_KRIS_FROM_LIBRARY, "true");
      boolean copyNonAssessedKrisFromLibrary =
          copyNonAssessedKrisFromLibrarySetting.equalsIgnoreCase("true") ? true : false;

      logger.error("libraryIdsOfAllKrisUnderVendor(after associating Green Breach status ones): "
          + libraryIdsOfAllKrisUnderVendor + "\n\n copyNonAssessedKrisFromLibrary: "
          + copyNonAssessedKrisFromLibrary);

      if (copyNonAssessedKrisFromLibrary) {
        for (String kriRecord : allKrisInLibrary) {
        	 logger.error("kriRecord= "+kriRecord);
          String id = utils.getIdOfRecord(kriRecord);
          logger.error("id= "+id);
          String kriLibraryId = utils.getRecordValueAtIndex(2, kriRecord);
          logger.error("kriLibraryId= "+kriLibraryId);
          // copy from library KRIs that are not assessed, and that don't exist under the vendor
          // already
          if (!libraryIdsOfAllKrisUnderVendor.contains(kriLibraryId)
              && !libraryIdsOfAllKrisReferencedByQuestionnaire.contains(kriLibraryId)) {
            CopyObjectOptions copyObjectOptions = CopyObjectOptions.getDefaultOptions();
            copyObjectOptions.setConflictBehavior(CopyConflictBehavior.USEEXISTING);

            List<IGRCObject> copyResult = utils.getApplicationService().copyToParent(
                new Id(getVendorsEntityId()), Arrays.asList(new Id(id)), copyObjectOptions);
            IGRCObject copiedKri = copyResult.get(0);
            logger.error("Copied the non-assessed KRI " + kriRecord
                + " from library to Vendor's primary parent(/Vendors).kriRecord: " + kriRecord
                + ";kriLibraryId: " + kriLibraryId
                + ". Will next make the vendor its primary parent; current KRI path: "
                + copiedKri.getPath() + "; id: " + copiedKri.getId());

            utils.getResourceService().dissociate(copiedKri.getId(),
                Arrays.asList(new Id(getVendorsEntityId())), Collections.emptyList());
            logger.error("Dissociated  non-assessed KRI from parent entity '/Vendors'(id "
                + getVendorsEntityId() + ")");

            copiedKri.setPrimaryParent(vendorParent);
            utils.saveResorce(copiedKri);
            logger.error("set current Vendor(" + vendorParent.getName()
                + ") as primary parent of the non-assessed KRI");
          }
        }
      } else {

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

  private List<String> getLibraryIdsOfAllKrisUnderVendor() throws Exception {
    String[] queryParams = new String[1];
    queryParams[0] = "'" + vendorParent.getId() + "'";
    List<String> krisUnderVendor = utils.getQueryResultsMultipleReturnFields(
        utils.getSetting(GET_ALL_VENDOR_CHILD_KRIS_QUERY_SETTING), queryParams, 4);

    List<String> libraryIdsOfAllKrisUnderVendor = new ArrayList<>();
    for (String record : krisUnderVendor) {
      libraryIdsOfAllKrisUnderVendor.add(utils.getRecordValueAtIndex(2, record));
    }
    return libraryIdsOfAllKrisUnderVendor;
  }

  private List<String> getAllKriRecordsFromLibrary() throws Exception {
    List<String> libraryKriRecords = utils.getQueryResultsMultipleReturnFields(
        utils.getSetting(GET_ALL_KRIs_FROM_LIBRARY_QUERY_SETTING), null, 4);

    return libraryKriRecords;
  }

  private String getKriIdFromQuestionTemplate(int currentQuestionIdx) {
    logger.error("Entered getKriIdFromQuestionTemplate(" + currentQuestionIdx + ")");
    List<IQuestion> questionTemplates = qt.getQuestions();
    if (questionTemplates == null) {
      logger.error(
          "questionnaire template doesn't contain any questions.\nExiting getKriIdFromQuestionTemplate() - returning");
      return null;
    }

    IQuestion questionTemplate = questionTemplates.get(currentQuestionIdx);

    if (questionTemplate == null) {
      logger.error("questionTemplate at index " + currentQuestionIdx
          + " is null. \nExiting getKriIdFromQuestionTemplate() - returning null");
      return null;
    }

    IGRCObject questionTemlateObject =
        utils.getResourceService().getGRCObject(questionTemplate.getId());

    kriId = utils.getFieldValueAsString(utils.getSetting(QUESTION_TEMPLATE_KRI_ID_FIELD_SETTING),
        questionTemlateObject);

    logger.error("Exiting getKriIdFromQuestionTemplate() - returning " + kriId);
    return kriId;
  }

  private void createOrUpdateKri(IQuestion question, int currentQuestionIdx) throws Exception {
    logger.error("Entered createOrUpdateKri(question with ID " + question.getId()
        + ", currentQuestionIdx: " + currentQuestionIdx + ")");

    String calculatedBreachStatus = calculateBreachStatus(question.getWeight());

    String kri_kriIdFieldName = utils.getSetting(KRI__KRI_ID_FIELD_SETTING);
    String kriBreachStatusFieldName = utils.getSetting(KRI_BREACH_STATUS_FIELD_SETTING);
    String sourceFieldsOnVendor = utils.getSetting(VENDOR_SOURCE_FIELDS_CSV_LIST_TO_COPY_FROM);
    String destinationFieldsOnKri = utils.getSetting(KRI_DESTINATION_FIELDS_CSV_LIST_TO_COPY_TO);
    String destinationFieldsOnKriValue =
        utils.getSetting(KRI_VALUE_DESTINATION_FIELDS_CSV_LIST_TO_COPY_TO);

    IGRCObject kri = null;

    String kriDatabaseId = getKriUnderVendorByKriId(kriId, "" + vendorParent.getId());
    boolean kriExistsAlreadyUnderVendor = kriDatabaseId.length() > 0 ? true : false;

    logger.error("Sumit: kriDatabaseId = "+kriDatabaseId);
    if (kriExistsAlreadyUnderVendor) {
      kri = utils.getObjectById(kriDatabaseId);
      logger.error("Sumit: kri = "+kri);
      logger.error("Found existing KRI under Vendor. Will next update its fields.");
    } else {
      logger.error(
          "Did not find already existing KRI under Vendor. Will get it from the KRI Library");

      String idOfKriFromLibrary = getKriFromLibrary(kriId);
      logger.error("Sumit: idOfKriFromLibrary = "+idOfKriFromLibrary);
      if (idOfKriFromLibrary.isEmpty()) { logger.error("Sumit: @528");
        String recipientUserFieldSetting = utils.getSetting(USER_FIELD_TO_EMAIL_ON_VENDOR);
        String recipientUserFieldValue =
            utils.getFieldValueAsString(recipientUserFieldSetting, vendorParent);
        logger.error("Sumit: recipientUserFieldValue = "+recipientUserFieldValue);
        if (recipientUserFieldValue == null || recipientUserFieldValue.trim().isEmpty()) {
          logger.warn("Can't send email because recipientUserField is empty");logger.error("Sumit: @534");
        } else {logger.error("Sumit: @535");
          logger.warn(
              "No KRI was found in the library(for copying on the parent Vendor).An email notification will be sent out if the corresponding setting is enabled. \nExiting createOrUpdateKri()");
          if (utils
              .getSettingWithDefaultValueIfNull(
                  ENABLE_EMAIL_NOTIFICATION_ON_KRI_ID_PROBLEMS_SETTING, "false")
              .equalsIgnoreCase("true")) {
            IUser recipientUser = utils.getSecurityService().getUser(recipientUserFieldValue);
            String name = " " + (recipientUser.getFirstName() != null
                && !recipientUser.getFirstName().trim().isEmpty() ? recipientUser.getFirstName()
                    : recipientUser.getName());

            IQuestion questionTemplate = qt.getQuestions().get(currentQuestionIdx);
            IGRCObject questionTemplateGrcObj = utils.getObjectById(questionTemplate.getId());

            String questionTemplateIdWithLink = "<a href=\"" + utils.getObjectTaskViewUrlPrefix()
                + questionTemplate.getId() + "\">" + questionTemplateGrcObj.getName() + "</a>";

            String questionnaireTemplateIdWithLink =
                "<a href=\"" + utils.getObjectTaskViewUrlPrefix() + qt.getId() + "\">"
                    + qtGrcObj.getName() + "</a>";

            List<String> placeholdersValues = new ArrayList<String>();
            placeholdersValues.add(name);
            placeholdersValues.add("<b>" + kriId + "</b>");
            placeholdersValues.add(questionTemplateIdWithLink);
            placeholdersValues.add(questionnaireTemplateIdWithLink);

            emailUtilities.notifyByEmail(recipientUser,
                KRI_NOT_FOUND_IN_LIBRRAY_EMAIL_SUBJECT_APP_TEXT,
                KRI_NOT_FOUND_IN_LIBRRAY_EMAIL_BODY_APP_TEXT, placeholdersValues);

          }
          return;
        }
      }

      CopyObjectOptions copyObjectOptions = CopyObjectOptions.getDefaultOptions();
      copyObjectOptions.setConflictBehavior(CopyConflictBehavior.CREATECOPYOF);

      logger.error("Sumit: copyObjectOptions = "+copyObjectOptions);
      logger.error("Sumit: idOfKriFromLibrary = "+idOfKriFromLibrary);
      
      List<IGRCObject> copyResult =
          utils.getApplicationService().copyToParent(new Id(getVendorsEntityId()),
              Arrays.asList(new Id(idOfKriFromLibrary)), copyObjectOptions);
      IGRCObject copiedKri = copyResult.get(0);
      logger.error(
          "Copied the KRI from library to Vendor's primary parent(/Vendors). Will next make the vendor its primary parent; current KRI path: "
              + copiedKri.getPath() + "; id: " + copiedKri.getId());

      utils.getResourceService().dissociate(copiedKri.getId(),
          Arrays.asList(new Id(getVendorsEntityId())), Collections.emptyList());
      logger
          .error("Dissociated KRI from parent entity '/Vendors'(id " + getVendorsEntityId() + ")");

      copiedKri.setPrimaryParent(vendorParent);
      logger
          .error("set current Vendor(" + vendorParent.getName() + ") as primary parent of the KRI");

      kri = copiedKri;
    }

    String currentKriBreachStatus =
        utils.getFieldValueAsString(kriBreachStatusFieldName, vendorParent);

    // String description = kri.getDescription() == null ? "" : kri.getDescription();
    // kri.setDescription(description + "\n" + question.getRationale());
    utils.setFieldValueOnResource(kri_kriIdFieldName, kriId, kri);
    boolean shouldBreachStatusBeUpdated =
        shouldBreachStatusBeUpdated(currentKriBreachStatus, calculatedBreachStatus);
    if (shouldBreachStatusBeUpdated) {
      utils.setFieldValueOnResource(kriBreachStatusFieldName, calculatedBreachStatus, kri);
    }
    kri = utils.copyFieldsAndSave(sourceFieldsOnVendor, destinationFieldsOnKri, vendorParent, kri);
    logger.error("KRI was saved after copying of all fields; ID: " + kri.getId());

    {
    	logger.error("Sumit Here is KRIV creation");
      // create KRI Value
      IGRCObject kriValue = utils.getResourceFactory()
          .createAutoNamedGRCObject (utils.getMetaDataService().getType(KRI_VALUE_OBJECT_TYPE));
     
      logger.error("Newly created KRI Value");
      kriValue.setPrimaryParent(kri);

      kriValue.setDescription(question.getRationale());

      utils.setFieldValueOnResource(utils.getSetting(KRI_VALUE_QUESTION_ID_FIELD_SETTING),
          "" + question.getId(), kriValue);

      utils.setFieldValueOnResource(utils.getSetting(KRI_VALUE_TYPE_FIELD_SETTING),
          utils.getFieldValueAsString(utils.getSetting(QUESTIONNAIRE_TEMPLATE_TYPE_FIELD_SETTING),
              qtGrcObj),
          kriValue);

      utils.setFieldValueOnResource(kri_kriIdFieldName, kriId, kriValue);
      utils.setFieldValueOnResource(kriBreachStatusFieldName, calculatedBreachStatus, kriValue);
//Sumit KRI value
      kriValue = utils.getResourceService().saveResource(kriValue);
      logger.error("Sumit saved newly created KRI Value \"" + kriValue.getName()  + "\" with ID " + kriValue.getId());
      kriValue = utils.copyFieldsAndSave(sourceFieldsOnVendor, destinationFieldsOnKriValue,
          vendorParent, kriValue);

      logger.error(
          "Created KRI Value and set/copied all its fields" + "\nExiting createOrUpdateKri()");
    }
  }

  private String getVendorsEntityId() throws Exception {
	  logger.error("Sumit: Inside getVendorsEntityId(): vendordEntityId-1 = "+vendordEntityId);
    if (vendordEntityId == null) {
      List<String> vendorsEntity = utils.getQueryResultsMultipleReturnFields(
          utils.getSettingWithDefaultValueIfNull(QUERY_GET_VENDORS_ENTITY_SETTING,
              "SELECT [SOXBusEntity].[Resource ID], [SOXBusEntity].[Name] FROM [SOXBusEntity] WHERE [SOXBusEntity].[Name]=''Vendors'' AND  [SOXBusEntity].[Location]=''/Vendors''"),
          null, 2);
      if (vendorsEntity.size() == 0 || vendorsEntity.size() > 1) {
        logger.error(
            "vendorsEntity.size()==0||vendorsEntity.size()>1; Something is incorrect. If vendorsEntity.size()> 1 will pick up first one in the list.");
      }
      vendordEntityId = utils.getIdOfRecord(vendorsEntity.get(0));
      logger.error("Sumit: vendordEntityId-2 = "+vendordEntityId);
    }
    logger.error("getVendorsEntityId() - returning " + vendordEntityId);

    return vendordEntityId;
  }

  private boolean shouldBreachStatusBeUpdated(String current, String calculated) {
    boolean result = true;

    current = current.trim();
    calculated = calculated.trim();
    String red = utils.getSetting(RED_BREACH_VALUE_SETTING).trim();
    String orange = utils.getSetting(ORANGE_BREACH_VALUE_SETTING).trim();
    String yellow = utils.getSetting(YELLOW_BREACH_VALUE_SETTING).trim();
    String green = utils.getSetting(GREEN_BREACH_VALUE_SETTING).trim();

    if (current.equalsIgnoreCase(calculated)) {
      result = false;
    } else if (current.equalsIgnoreCase(red)) {
      result = false;// it's already highest value so it shouldn't be updated;
    } else if (current.equalsIgnoreCase(orange) && !calculated.equalsIgnoreCase(red)) {
      result = false;// current value is higher than new calculated value ;so don't update
    } else if (current.equalsIgnoreCase(yellow) && !calculated.equalsIgnoreCase(red)
        && !calculated.equalsIgnoreCase(orange)) {
      result = false;
    } else if (current.equalsIgnoreCase(green) && !calculated.equalsIgnoreCase(red)
        && !calculated.equalsIgnoreCase(orange) && !calculated.equalsIgnoreCase(yellow)) {
      result = false;
    }

    logger.error("shouldBreachStatusBeUpdated(current: " + current + ", calculated: " + calculated
        + ") -> returning " + result);

    return result;
  }

  private boolean kriMustBeUpdatedOrCreated(IQuestion question) {
    logger.error("Entered kriMustBeUpdatedOrCreated(question: title " + question.getTitle()
        + ", id: " + question.getId() + ")");

    Object answerObj = question.getAnswer();
    if (answerObj == null) {
      logger.error("answerObj is null;\nExiting kriMustBeUpdatedOrCreated() - returning false");
      return false;
    }

    if (question.getAnswerType() != AnswerType.RADIO
        && question.getAnswerType() != AnswerType.MULTI_CHECK) {
      logger.error("answer type is NOT RADIO and NOT MULTI_CHECK eiher. It's "
          + question.getAnswerType()
          + ". Spec doesn't cover this case..\nExiting kriMustBeUpdatedOrCreated() - returning false");
      return false;
    }

    List<IQuestionOption> questionOptions = question.getOptions();
    if (questionOptions == null) {
      logger.error(
          "questionOptions is null for the current question.\nExiting kriMustBeUpdatedOrCreated() - returning false");
      return false;
    }
    logger.error("Sumit- questionOptions.size()= "+questionOptions.size());
    for (int j = 0; j < questionOptions.size(); j++) {
    	logger.error("Sumit j ="+j);
      IQuestionOption questionOption = questionOptions.get(j);
      logger.error(questionOption == null ? "questionOption at idx " + j + " is null"
          : "questionOption at idx " + j//
              + " -> label: " + questionOption.getLabel()//
              + ", description: " + questionOption.getDescription() //
              + ", score: " + questionOption.getScore() //
              + ", value: " + ", " + questionOption.getValue()//
              + ", getRequires:" + questionOption.getRequires());
    }

    boolean foundAnUnfavourableAnswer = false;

    if (question.getAnswerType() == AnswerType.RADIO) { logger.error("Sumit answer is RADIO");
      int answer = ((Integer) answerObj).intValue();
      logger.error("Sumit answer = "+answer);
      int score = questionOptions.get(answer).getScore();
      logger.error("Sumit score = "+score);
      if (score == unfavourableAnswerValue) {
        foundAnUnfavourableAnswer = true;
        logger.error("the single selection answer is unfavourable");
      }
    } else if (question.getAnswerType() == AnswerType.MULTI_CHECK) { logger.error("Sumit answer is MULTI_CHECK");
      // if AnswerType is MULTI_CHECK we know answerObj is List<Integer>
      @SuppressWarnings("unchecked")
      List<Integer> answerList = (List<Integer>) answerObj;
      logger.error("Sumit answerList.size() = "+ answerList.size());
      for (int k = 0; k < answerList.size(); k++) { logger.error("Sumit K = "+k);
        if (answerList.get(k) == null) {
          logger.error("answer at index " + k + " is null; skipping it");
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

    logger.error("Exiting kriMustBeUpdatedOrCreated() - returning " + result);
    return result;
  }

  private String calculateBreachStatus(int weight) {
    logger.error("Entered calculateBreachStatus(weight: " + weight + ")");
    String breachStatus = utils.getSetting(GREEN_BREACH_VALUE_SETTING);

    List<String> redValues = utils.getSettingCsvAsList(WEIGHT_VALUES_FOR_RED_BREACH_SETTING);
    List<String> orangeValues = utils.getSettingCsvAsList(WEIGHT_VALUES_FOR_ORANGE_BREACH_SETTING);
    List<String> yellowValues = utils.getSettingCsvAsList(WEIGHT_VALUES_FOR_YELLOW_BREACH_SETTING);

    if (redValues.contains("" + weight)) {
      breachStatus = utils.getSetting(RED_BREACH_VALUE_SETTING);
    } else if (orangeValues.contains("" + weight)) {
      breachStatus = utils.getSetting(ORANGE_BREACH_VALUE_SETTING);
    } else if (yellowValues.contains("" + weight)) {
      breachStatus = utils.getSetting(YELLOW_BREACH_VALUE_SETTING);
    }

    logger.error("Exiting calculateBreachStatus() -> returning " + breachStatus);
    return breachStatus;
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

  private String getKriUnderVendorByKriId(String libraryId, String vendorId) throws Exception {
    logger.error("Entered getKriUnderVendorByKriId(libraryId(aka KRI ID):" + libraryId
        + ", vendorId: " + vendorId + ")");

    String[] queryParams = new String[2];
    queryParams[0] = "'" + libraryId + "'";
    queryParams[1] = "'" + vendorId + "'";

    boolean honorPrimaryOny = true;
    if (utils
        .getSettingTrimmedEmptyStringIfNull(
            HONOR_PRIMARY_ONLY_ON_QUERY_GET_KRI_CHILD_OF_VENDOR_BY_KRI_ID_SETTING)
        .equalsIgnoreCase("false")) {
      honorPrimaryOny = false;
    }
    String query = utils.getSetting(GET_KRI_CHILD_OF_VENDOR_BY_KRI_ID_QUERY_SETTING);
    List<String> kriRecords =
        utils.getQueryResultsMultipleReturnFields(query, queryParams, 4, honorPrimaryOny);

    if (kriRecords.size() > 1) {
      logger.warn("Found more than one KRI. Will get first one. This should not happen.");
    }

    String kriDatabaseId = kriRecords.size() > 0 ? utils.getIdOfRecord(kriRecords.get(0)) : "";

    logger.error("Exiting getKriUnderVendorByKriId() - returning \"" + kriDatabaseId + "\"");
    return kriDatabaseId;
  }

  private String getKriFromLibrary(String libraryId) throws Exception {
    logger.error("Entered getKriFromLibrary(libraryId - aka KRI ID:" + libraryId + ")");

    String[] queryParams = new String[1];
    queryParams[0] = "'" + libraryId + "'";

    String query = utils.getSetting(GET_KRI_FROM_LIBRARY_BY_KRI_ID_QUERY_SETTING);
    logger.error("Sumit: query = "+query);
    List<String> kriRecords = utils.getQueryResultsMultipleReturnFields(query, queryParams, 3);
    logger.error("Sumit: kriRecords.size() = "+kriRecords.size());
    if (kriRecords.size() > 1) {
      logger.warn("Found more than one KRI. Will get first one. This should not happen.");
    }

    String kriDatabaseId = kriRecords.size() > 0 ? utils.getIdOfRecord(kriRecords.get(0)) : "";
    logger.error("Sumit: kriDatabaseId = "+kriDatabaseId);
    logger.error("Exiting getKriFromLibrary() - returning \"" + kriDatabaseId + "\"");
    return kriDatabaseId;
  }
}
