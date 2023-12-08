package sample.code.questionnaires;

/*
 * {
 * 
 * "$schema":{"$ref":"TS_Taxonomy_vMay132009"},
 * 
 * "author":"Liviu Ignat",
 * 
 * "customer":"GTS",
 * 
 * "date":"08/10/2020",
 * 
 * "summary":"VRMtoQAResponseRolloverBatchRealizer",
 * 
 * "technology":"java",
 * 
 * "feature":"Java Utility",
 * 
 * "rt_num":"1963"
 * 
 * }
 */

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

//import javax.mail.Authenticator;
//import javax.mail.Message;
//import javax.mail.PasswordAuthentication;
//import javax.mail.Session;
//import javax.mail.Transport;
//import javax.mail.internet.InternetAddress;
//import javax.mail.internet.MimeMessage;
//import javax.servlet.http.HttpServletRequest;

import com.ibm.openpages.api.ObjectNotFoundException;
import com.ibm.openpages.api.OpenPagesException;
import com.ibm.openpages.api.metadata.IEnumValue;
import com.ibm.openpages.api.metadata.IFieldDefinition;
import com.ibm.openpages.api.metadata.Id;
import com.ibm.openpages.api.query.IPage;
import com.ibm.openpages.api.query.IQuery;
import com.ibm.openpages.api.query.IResultSetRow;
import com.ibm.openpages.api.questionnaire.IQuestion;
import com.ibm.openpages.api.questionnaire.IQuestionnaire;
import com.ibm.openpages.api.questionnaire.ISection;
import com.ibm.openpages.api.resource.GRCObjectFilter;
import com.ibm.openpages.api.resource.IAssociationNode;
import com.ibm.openpages.api.resource.IBooleanField;
import com.ibm.openpages.api.resource.ICurrencyField;
import com.ibm.openpages.api.resource.IDateField;
import com.ibm.openpages.api.resource.IField;
import com.ibm.openpages.api.resource.IFloatField;
import com.ibm.openpages.api.resource.IGRCObject;
import com.ibm.openpages.api.resource.IIdField;
import com.ibm.openpages.api.resource.IIntegerField;
import com.ibm.openpages.api.resource.IMultiEnumField;
import com.ibm.openpages.api.resource.IStringField;
import com.ibm.openpages.api.resource.IEnumField;
import com.ibm.openpages.api.resource.IncludeAssociations;
import com.ibm.openpages.api.service.IApplicationService;
import com.ibm.openpages.api.service.IConfigurationService;
import com.ibm.openpages.api.service.IMetaDataService;
import com.ibm.openpages.api.service.IQueryService;
import com.ibm.openpages.api.service.IQuestionnaireService;
import com.ibm.openpages.api.service.IResourceService;
import com.ibm.openpages.api.service.ISecurityService;
import com.ibm.openpages.api.service.IServiceFactory;
import com.ibm.openpages.api.service.ServiceFactory;
import com.openpages.apps.sosa.util.SoxPropertyConstants;
import com.openpages.aurora.common.AuroraEnv;
import com.openpages.ext.solutions.common.LoggerUtil;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;



public class VRMtoQAResponseRolloverBatchRealizer {


  /*
   * ..etc...
   */
  private static String kPREFIX = "[[VRMtoQAResponseRolloverBatchRealizer]]: ";

  /**
   * ------------------------------------------------------- TYPES
   * -------------------------------------------------------
   */
  public class XFileComparator implements Comparator<File> {
    @Override
    public int compare(File x, File y) {
      ASSERT(x != null & y != null, "Check logic!");
      return (FileUtils.isFileOlder(x, y) ? 1 : -1);
    }
  }



  /**
   * ------------------------------ fields ------------------------------
   */
 // private HttpServletRequest m_req = null;

  @SuppressWarnings("unused")
  private IApplicationService m_as = null;
  private IConfigurationService m_cs = null;
  private ISecurityService m_ss = null;
  private IQueryService m_qs = null;
  private IResourceService m_rs = null;
  private IMetaDataService m_ms = null;
  private IQuestionnaireService m_qns = null;

  private Logger m_L = null;

  static boolean m_kRunningAlready = false;

  // some cached stuff
  private String m_sStatusInfo = "";

  // misc
  Date m_dStartTimestamp = null;
  int m_nSuccessCounter, m_nErrorCounter;

  /**
   * ------------------------------ Constructor ------------------------------
   */
  public VRMtoQAResponseRolloverBatchRealizer(/*HttpServletRequest*/String req) throws Exception {

  //  m_req = req;
    try {
      IServiceFactory sf = null;//ServiceFactory.getServiceFactory(/* m_req */ null);
      m_as = sf.createApplicationService();
      m_cs = sf.createConfigurationService();
      m_ss = sf.createSecurityService();
      m_rs = sf.createResourceService();
      m_ms = sf.createMetaDataService();
      m_qs = sf.createQueryService();
      m_qns = sf.createQuestionnaireService();
      m_L = LoggerUtil.getLogger(this.getClass());

    } catch (Exception e) {
      LoggerUtil.getLogger().error(kPREFIX + e.getMessage());
      e.printStackTrace();
      throw e;
    }
  }


  /**
   * ------------------------------ partiallyResetState ------------------------------
   */
  private void partiallyResetState() {
    VRMtoQAResponseRolloverBatchRealizer.m_kRunningAlready = true;
    m_sStatusInfo = "";
    ksdfAnswerDateFormat =
        new SimpleDateFormat(m_cs.getConfigProperties().getProperty(STRREG_DATE_FORMAT));
  }


  /**
   * ------------------------------ resetCounters ------------------------------
   */
  private void resetCounters() {
    m_dStartTimestamp = Calendar.getInstance().getTime();
    m_nSuccessCounter = m_nErrorCounter = 0;
  }



  /**
   * ------------------------------------------------------- makeATag() - shorthand
   * -------------------------------------------------------
   */
  // final private static String ksA_TAG = "<a href='#'
  // onClick=\"window.open('%URL%').focus();\">%NAME%</a>";
  final private static String ksA_TAG = "<a href='%URL%'>%NAME%</a>";

  private String makeATag(final Id id, final String sName) throws Exception {
    ASSERT(id != null && sName != null, "Check logic!");
    // ->String sURL = AuroraEnv.getProperty(SoxPropertyConstants.APPLICATION_URL_PATH) +
    // "/app/jspview/react/grc/task-view/" + id;
    String sURL = AuroraEnv.getProperty(SoxPropertyConstants.APPLICATION_URL_PATH)
        + "/view.resource.do?fileId=" + id;
    return ksA_TAG.replace("%URL%", sURL).replace("%NAME%", sName);
  }

  private String makeATag(final IGRCObject o) throws Exception {
    return this.makeATag(o.getId(), o.getName());// !!!!yes, description
  }


  /*
   * ------------------------------------------------------------------------------------------ The
   * methods below take care of registering the status
   * ------------------------------------------------------------------------------------------
   */
  private void registerOneItem(final IGRCObject oQA, final String sStatus,
      final String sErrorDetail) throws Exception {

    if (this.isBare(m_sStatusInfo))
      m_sStatusInfo = ksTABLE_STYLE + ksSTATUS_TABLE_PROLOGUE;

    m_sStatusInfo += ksSTATUS_TABLE_ROW.replace("%QA%", this.makeATag(oQA))
        .replace("%STATUS%", sStatus).replace("%ERROR_DETAILS%", sErrorDetail);
  }

  private void wrapUpStatus() {
    if (m_sStatusInfo.contains(ksSTATUS_TABLE_PROLOGUE))
      m_sStatusInfo += ksSTATUS_TABLE_EPILOGUE;
  }

  private void registerOneSuccess(final IGRCObject oQA) throws Exception {
    this.registerOneItem(oQA, "Success", "");
    m_L.debug("QuestionnaireAssessment object succesfully processed: " + oQA.getName());
    m_nSuccessCounter++;
  }

  private void registerOneError(final IGRCObject oQA, final Exception e) throws Exception {
    this.registerOneItem(oQA, "Error", e.getMessage());
    m_L.error(
        "Error processing QuestionnaireAssessment object : " + oQA.getName() + ksNewLine + "CAUSE: "
            + e.getMessage() + ksNewLine + VRMtoQAResponseRolloverBatchRealizer.printStack(e));
    m_nErrorCounter++;
  }



  /**
   * ------------------------------------------------------------ ASSERT() -- no longer necessary to
   * turn on the "-ea" flag (slows things down)
   * ------------------------------------------------------------
   */
  public void ASSERT(boolean bCondition, String sExplanation) throws AssertionError {
    if (bCondition == false) {
      m_L.error("ASSERTION ERROR AT LINE : "
          + Thread.currentThread().getStackTrace()[3].getLineNumber() + " REASON: " + sExplanation);
      throw new AssertionError(sExplanation);
    }

  }

  protected void _L(final String sMess) {
    m_L.error("+++>" + sMess);
  }


  /**
   * ------------------------------------------------------------- loadAppStr()
   * -------------------------------------------------------------
   */
  protected String loadAppStr(final String sKey) throws Exception {
    ASSERT(m_cs != null, "Bad logic!");
    try {
      String sRet = m_cs.getLocalizedApplicationText(sKey);
      if (this.isBare(sRet))
        return "MISSING APPLICATION STRING: " + sKey;
      return sRet;
    } catch (Exception e) {
      return sKey;
    }


  }

  protected String loadAppStr(final String sKey, Object[] aSubstVals) throws Exception {
    ASSERT(m_cs != null, "Bad logic!");
    try {
      String sRet = MessageFormat.format(m_cs.getLocalizedApplicationText(sKey), aSubstVals);
      if (this.isBare(sRet))
        return "MISSING APPLICATION STRING: " + sKey;
      return sRet;
    } catch (Exception e) {
      return sKey;
    }
  }

  protected String loadEN_USAppStr(final String sKey) throws Exception {
    ASSERT(m_cs != null, "Bad logic!");
    try {
      String sRet = m_cs.getLocalizedApplicationText(sKey, new Locale("en_US"));
      if (this.isBare(sRet))
        return "MISSING APPLICATION STRING: " + sKey;
      return sRet;
    } catch (Exception e) {
      return sKey;
    }
  }


  /*
   * ----------------------------------------------------- ODDS AND ENDS
   * -----------------------------------------------------
   */
  protected boolean isBare(final String s) {
    if (s == null || "".equals(s.trim()))
      return true;
    return false;
  }

  protected boolean isBare(@SuppressWarnings("rawtypes") final List l) {
    if (l == null || l.size() == 0)
      return true;
    return false;
  }

  protected boolean isBare(@SuppressWarnings("rawtypes") final Map m) {
    if (m == null || m.size() == 0)
      return true;
    return false;
  }

  protected String capitalize(final String s) {
    if (this.isBare(s))
      return s;
    return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
  }

  protected String entityDisplayPathToFullPath(final String sP) {
    ASSERT(!this.isBare(sP), "Invalid path" + sP);
    ASSERT(sP.substring(0, 1).contentEquals("/"), "Invalid display path" + sP);

    return m_ms.getType(ksEntityType).getRootFolderPath() + sP
        + sP.substring(sP.lastIndexOf("/", sP.length())) + ".txt";
  }

  protected String entityFullPathToDisplayPath(final String sP) {
    ASSERT(!this.isBare(sP), "Invalid path" + sP);
    ASSERT(sP.startsWith(m_ms.getType(ksEntityType).getRootFolderPath()), "Invalid full path" + sP);

    String sX = sP.replace(m_ms.getType(ksEntityType).getRootFolderPath(), "");
    return sX.substring(0, sX.lastIndexOf("/"));
  }

  private void signalSettingError(final String sSetting) throws Exception {
    String sM = this.loadAppStr(IDS_ERROR_INVALID_SETTING) + sSetting;
    _L(sM);
    m_sStatusInfo += ("<h2>" + sM + "</h2><BR/>");
  }

  protected IGRCObject fetchGRCObject(final String sPath) {
    ASSERT(m_rs != null, "Uninitialized state!");
    if (this.isBare(sPath))
      return null;

    IGRCObject oRet = null;
    try {
      oRet = m_rs.getGRCObject(sPath);
    } catch (ObjectNotFoundException e) {
      return null;
    } catch (StringIndexOutOfBoundsException ee) {// no "/" in the path...
      return null;
    }
    return oRet;
  }


  protected String getEnumValueAsString(final IGRCObject o, final String sFN) {
    ASSERT(o != null && !this.isBare(sFN), "Invalid usage!");

    IEnumField f = (IEnumField) o.getField(sFN);
    ASSERT(f != null, "Invalid usage!");

    if (f.isNull())
      return "";
    return f.getEnumValue().getName();
  }

  protected void setEnumValueAsString(IGRCObject o, final String sFN, final String sV) {
    ASSERT(o != null && !this.isBare(sFN), "Invalid usage!");

    IFieldDefinition f = null;
    try {
      f = m_ms.getField(sFN);
    } catch (ObjectNotFoundException e) {
      ASSERT(false, "Invalid usage!");
    }
    ASSERT(f != null, "Invalid usage!");

    ((IEnumField) o.getField(sFN)).setEnumValue(this.isBare(sV) ? null : f.getEnumValue(sV));
  }

  protected void setMultiEnumValue(IGRCObject o, final String sFN, final String[] asV) {
    ASSERT(o != null && !this.isBare(sFN), "Invalid usage!");

    IMultiEnumField f = null;
    try {
      f = (IMultiEnumField) o.getField(sFN);
    } catch (ObjectNotFoundException e) {
      ASSERT(false, "Invalid usage!");
    }
    ASSERT(f != null, "Invalid usage!");

    if (asV == null || asV.length == 0) {
      f.setEnumValues(new ArrayList<IEnumValue>());
      return;
    }

    List<IEnumValue> lVals = new ArrayList<IEnumValue>();
    for (String s : asV) {
      IEnumValue v = f.getFieldDefinition().getEnumValue(s);
      ASSERT(v != null, "Invalid usage!");
      lVals.add(v);
    }

    f.setEnumValues(lVals);
  }


  static protected String printStack(final Throwable t) {
    Writer w = new StringWriter();
    t.printStackTrace(new PrintWriter(w));
    return w.toString();
  }

  private String putTogetherQTQueryInClause(final Set<String> lsQTNames) throws Exception {
    ASSERT(lsQTNames != null, "Bad logic!");
    if (lsQTNames.size() == 0)
      return "()";

    String sRet = "(";
    for (String s : lsQTNames)
      sRet += ("'" + s + "', ");

    return sRet.substring(0, sRet.length() - 2) + ")";
  }



  final private static String ksTABLE_STYLE =
      "<style> table{ border-width: 1px; border-spacing: 0px; border-style: solid; border-color: black; border-collapse: collapse; width:100%; } th { border-width: 1px; border-style: solid; border-color: black; background-color:lightgreen; } td { border-width: 1px; border-style: solid; border-color: black; } </style>";

  final private static String ksSTATUS_TABLE_PROLOGUE =
      "<table><tbody><tr> <th>Questionnaire Assessment</th> <th>STATUS</th> <th>ERROR DETAILS</th> </tr>";
  final private static String ksSTATUS_TABLE_ROW =
      "<tr>  <td>%QA%</td> <td>%STATUS%</td> <td>%ERROR_DETAILS%</td> </tr>";
  final private static String ksSTATUS_TABLE_EPILOGUE = "</tbody></table>";


  /**
   * ------------------------------------------------------- PUBLIC: proceed() - entry point
   * -------------------------------------------------------
   */
  public String proceed_____() throws Exception {
    try {
      // -> Map<String,IGRCObject> mapXXX = this.gulpUpPairings();
      // -> String sss = com.openpages.aurora.common.Environment.getReleaseNumber();

      IQuestionnaire oQN = m_qns.getQuestionnaire(new Id("674036"));
      for (IQuestion q : oQN.getQuestions()) {
        if ("Cost".equals(q.getTitle()))// text
          q.setAnswer(null);
        if ("Risk Rating".equals(q.getTitle()))// radio
          q.setAnswer(null);
        if ("Domain".equals(q.getTitle())) {// multi
          ArrayList<Integer> lll = new ArrayList<Integer>();
          lll.add(1);
          lll.add(3);
          q.setAnswer(lll);
        }
      }
      m_qns.saveQuestionnaire(oQN);

      return "[" + this.getCurrentTimeStamp() + "] - Bine!";
    } catch (Throwable e) {
      return "[" + this.getCurrentTimeStamp() + "] - Rau!<BR/>" + e.getMessage() + "<BR/>"
          + VRMtoQTMigrationBatchRealizer.printStack(e);
    }
  }


  public String proceed() throws Exception {

    try {
      // the very first thing to do
      this.resetCounters();

      // we certainly need to initialize our state first
      if (!this.weAreOkayWithThePreliminaries()) {
        String sT = this.loadAppStr(IDS_ERROR_PROLOGUE_FAILED);
        if (m_cs.getConfigProperties().getProperty(STRREG_OVERALL_EMAIL_RECIPIENTS) != null)
          _L(sT);
        return this.getCurrentTimeStamp() + "<BR>" + sT + "<BR>" + m_sStatusInfo;
      }

      // running already?
      if (VRMtoQAResponseRolloverBatchRealizer.m_kRunningAlready) {
        String sT =
            this.loadAppStr(IDS_ERROR_ALREADY_RUNNING, new Object[] {this.getCurrentTimeStamp()});
        this.sendEmail(sT, "",
            m_cs.getConfigProperties().getProperty(STRREG_OVERALL_EMAIL_RECIPIENTS).split(","));
        _L(sT);
        return this.getCurrentTimeStamp() + " : Already running!";
      }

      // reset state
      this.partiallyResetState();

      boolean bNothingToProcess = true;

      Map<IGRCObject, IGRCObject> mapQtToVRMPairs = gulpUpPairings();

      Map<String, IGRCObject> mapQTNamesToVRMs = null;
      List<IGRCObject> lQTs = null;
      if (this.isBare(mapQtToVRMPairs)) {// the pairings file has precedence
        mapQTNamesToVRMs = this.slurpUpVRMsToBeProcessed();
        lQTs = this.slurpUpNecessaryQTs(mapQTNamesToVRMs.keySet());
      }

      for (IGRCObject oQT : (!this.isBare(mapQtToVRMPairs) ? mapQtToVRMPairs.keySet() : lQTs))
        for (IGRCObject oQA : this.getAllParentsOfType(oQT.getId(), ksQAType)) {
          bNothingToProcess = false;
          this.processOneItem(oQA, (!this.isBare(mapQtToVRMPairs) ? mapQtToVRMPairs.get(oQT)
              : mapQTNamesToVRMs.get(oQT.getName())));// hehe.....
        }


      if (bNothingToProcess) {
        String sM = this.loadAppStr(IDS_NOTHING_TO_PROCESS);
        _L(sM);
        m_sStatusInfo += ("<h2>" + sM + "</h2><BR/>");
      }

      return this.putTogetherFeedback();
    } catch (Exception e) {
      final String sMess = "UNRECOVERABLE ERROR: " + e.getMessage() + ksNewLine + "<BR/>"
          + VRMtoQAResponseRolloverBatchRealizer.printStack(e);
      _L(sMess);
      m_sStatusInfo += sMess;
      return this.putTogetherFeedback() + "<BR/>" + sMess;
    } catch (AssertionError e) {
      final String sStack = VRMtoQAResponseRolloverBatchRealizer.printStack(e);
      _L(e.getMessage() + ksNewLine + sStack);
      m_sStatusInfo += (e.getMessage() + "<BR/>" + sStack);
      return this.putTogetherFeedback() + "<BR/>" + e.getMessage() + "<BR/>" + sStack;
    } finally {
      VRMtoQAResponseRolloverBatchRealizer.m_kRunningAlready = false;
      this.wrapUpStatus();// meh...
      this.sendOverallStatusEmail();
    }


  }


  /**
   * ------------------------------------------------------------- gulpUpPairings() - reads in the
   * contents of the most recent pairs file in the STRREG_PAIRING_FILE_DIR location
   * -------------------------------------------------------------
   */
  private Map<IGRCObject, IGRCObject> gulpUpPairings() throws Exception {
    ASSERT(m_cs != null, "Uninitialized state!");

    File fDir = new File(m_cs.getConfigProperties().getProperty(STRREG_PAIRING_FILE_DIR));
    @SuppressWarnings("unchecked")
    List<File> lPairFiles =
        (List<File>) FileUtils.listFiles(fDir, new String[] {"pairs"}, false/* not recursive */);
    if (lPairFiles == null || lPairFiles.size() == 0)
      return null;
    Collections.sort(lPairFiles, new XFileComparator());// sort by date

    Map<IGRCObject, IGRCObject> mapRet = new HashMap<IGRCObject, IGRCObject>();
    for (String sX : FileUtils.readFileToString(lPairFiles.get(0)).split(",")) {
      String[] aPairs = sX.trim().split("=");
      IGRCObject oVRM = m_rs.getGRCObject(new Id(aPairs[0].trim()));
      ASSERT(ksVRMType.equals(oVRM.getType().getName()), "Bad, bad pairing file!");
      IGRCObject oQT = m_rs.getGRCObject(new Id(aPairs[1].trim()));
      ASSERT(ksQTType.equals(oQT.getType().getName()), "Bad, bad pairing file!");
      mapRet.put(m_rs.getGRCObject(new Id(aPairs[1].trim())), oVRM);// revert; also....let it blow
                                                                    // up
    }

    return mapRet;
  }

  /**
   * ----------------------------------------------------------------------- getAllParentsOfType()
   * -----------------------------------------------------------------------
   */
  protected Set<IGRCObject> getAllParentsOfType(final Id idForThisGuy, final String sParentType) {
    ASSERT(idForThisGuy != null, "Check logic!");
    ASSERT(!this.isBare(sParentType), "Check logic!");
    ASSERT(m_rs != null, "Uninitialized state.");

    Set<IGRCObject> setR = new HashSet<IGRCObject>();

    GRCObjectFilter of = new GRCObjectFilter(m_cs.getCurrentReportingPeriod());
    of.getAssociationFilter().setIncludeAssociations(IncludeAssociations.PARENT);
    of.getAssociationFilter().setTypeFilters(m_ms.getType(sParentType));
    for (IAssociationNode an : m_rs.getGRCObject(idForThisGuy, of).getParents())
      setR.add(m_rs.getGRCObject(an.getId()));

    return setR;
  }

  /**
   * --------------------------------------------------------------------- getAllChildrenOfType()
   * ---------------------------------------------------------------------
   */
  protected List<IGRCObject> getAllChildrenOfType(final Id idForThisGuy, final String sChildType) {
    ASSERT(idForThisGuy != null, "Check logic!");
    ASSERT(!this.isBare(sChildType), "Check logic!");
    ASSERT(m_rs != null, "Uninitialized state!");

    List<IGRCObject> lR = new ArrayList<IGRCObject>();

    GRCObjectFilter of = new GRCObjectFilter(m_cs.getCurrentReportingPeriod());
    of.getAssociationFilter().setIncludeAssociations(IncludeAssociations.CHILD);
    of.getAssociationFilter().setTypeFilters(m_ms.getType(sChildType));
    for (IAssociationNode an : m_rs.getGRCObject(idForThisGuy, of).getChildren())
      lR.add(m_rs.getGRCObject(an.getId()));

    return lR;
  }


  /**
   * ------------------------------------------------------------- processOneItem()
   * -------------------------------------------------------------
   */
  private void processOneItem(IGRCObject oTargetQA, final IGRCObject oSourceVRM) throws Exception {
    ASSERT(oTargetQA != null && oSourceVRM != null, "Bad logic!");
    ASSERT(ksQAType.equals(oTargetQA.getType().getName()), "Bad usage!");
    ASSERT(ksVRMType.equals(oSourceVRM.getType().getName()), "Bad usage!");
    m_L.debug("Processing one item.  *TARGET=" + oTargetQA.getPath() + "  *SOURCE="
        + oSourceVRM.getPath());
    try {
      IQuestionnaire oQN = m_qns.getQuestionnaire(oTargetQA.getId());

      // - main section

      this.populateRemarkableSection(oQN, oSourceVRM);

      // - the other sections
      for (IGRCObject oVRMQsection : this.getAllChildrenOfType(oSourceVRM.getId(),
          ksVRMQsectionType)) {
        ISection oSection = this.getSectionByTitle(oQN, oVRMQsection.getName());
        for (IGRCObject oVRMQuest : this.getAllChildrenOfType(oVRMQsection.getId(), ksVRMQuestType))
          this.populateSomeOrdinarySubsection(oQN,
              this.getSubsectionByTitleAndParent(oQN, oSection.getId(), oVRMQuest.getName()),
              oVRMQuest);
      }

      m_qns.saveQuestionnaire(oQN);
      // final touch...
      this.registerOneSuccess(oTargetQA);
    } catch (OpenPagesException e) {
      this.registerOneError(oTargetQA, e);
    }
  }

  /**
   * ------------------------------------------------------------- populateRemarkableSection()
   * -------------------------------------------------------------
   */
  private void populateRemarkableSection(final IQuestionnaire oQN, final IGRCObject oSourceVRM)
      throws Exception {
    final String sT = oSourceVRM.getName()
        .replace(m_cs.getConfigProperties().getProperty(STRREG_SELECTION_SUFFIX), "");// same naming
                                                                                      // logic as in
                                                                                      // the
                                                                                      // VRMToQTMigrationBatch
    final String sX = m_cs.getConfigProperties().getProperty(STRREG_MASTER_SECTION_SUFFIX);
    ISection oMainSubsection =
        this.getSubsectionByTitleAndParent(oQN, this.getSectionByTitle(oQN, sT + sX).getId(), sX);// same
                                                                                                  // naming
                                                                                                  // logic
                                                                                                  // as
                                                                                                  // in
                                                                                                  // the
                                                                                                  // VRMToQTMigrationBatch

    for (String sF : m_cs.getConfigProperties().getProperty(STRREG_VRM_FIELDS).split(";"))
      this.populateQuestion(oSourceVRM.getField(sF.trim()), oQN, oMainSubsection);
  }

  /**
   * ------------------------------------------------------------- populateSomeOrdinarySubsection()
   * -------------------------------------------------------------
   */
  private void populateSomeOrdinarySubsection(final IQuestionnaire oQN, final ISection oSubsection,
      final IGRCObject oVRMQuest) throws Exception {

    for (String sF : m_cs.getConfigProperties().getProperty(STRREG_QUESTION_FIELDS).split(";"))
      this.populateQuestion(oVRMQuest.getField(sF.trim()), oQN, oSubsection);
  }


  /**
   * ------------------------------------------------------------- weAreOkayWithThePreliminaries()
   * -------------------------------------------------------------
   */
  private boolean weAreOkayWithThePreliminaries() throws Exception {
    ASSERT(m_cs != null, "Uninitialized state!");

    boolean bRet = true;
    bRet &= (this.checkManyStringFieldsSetting(STRREG_VRM_FIELDS) & bRet);
    bRet &= (this.checkManyStringFieldsSetting(STRREG_QUESTION_FIELDS) & bRet);
    bRet &= (this.checkOneEnityPathSetting(STRREG_COLLECTOR_ENTITY) & bRet);
    bRet &= (this.checkOneDirectorySetting(STRREG_PAIRING_FILE_DIR) & bRet);
    bRet &= (this.checkSettingExists(STRREG_PAIRING_BASE_FILENAME) & bRet);
    bRet &= (this.checkSettingExists(STRREG_OVERALL_EMAIL_RECIPIENTS) & bRet);
    bRet &= (this.checkSettingExists(STRREG_MASTER_SECTION_SUFFIX) & bRet);
    bRet &= (this.checkSettingExists(STRREG_SELECTION_SUFFIX) & bRet);
    bRet &= (this.checkDateFormatSetting(STRREG_DATE_FORMAT) & bRet);
    return bRet;
  }


  private boolean checkDateFormatSetting(final String sSetting) throws Exception {
    if (this.isBare(sSetting)) {
      this.signalSettingError(sSetting);
      return false;
    }

    try {
      (new SimpleDateFormat(m_cs.getConfigProperties().getProperty(sSetting))).format(new Date());
    } catch (IllegalArgumentException e) {
      this.signalSettingError(sSetting);
      return false;// meh, kind of weak....
    }
    return true;
  }

  private boolean checkSettingExists(final String sSetting) throws Exception {
    if (null == m_cs.getConfigProperties().getProperty(sSetting)) {
      this.signalSettingError(sSetting);
      return false;
    }
    return true;
  }

  private boolean checkOneDirectorySetting(final String sSetting) throws Exception {
    if (this.isBare(m_cs.getConfigProperties().getProperty(sSetting))) {
      this.signalSettingError(sSetting);
      return false;
    }

    File fX = new File(m_cs.getConfigProperties().getProperty(sSetting));
    if (!fX.exists() || !fX.isDirectory()) {
      this.signalSettingError(sSetting);
      return false;
    }
    return true;
  }


  private boolean checkManyStringFieldsSetting(final String sSetting) throws Exception {
    if (m_cs.getConfigProperties().getProperty(sSetting) == null) {
      this.signalSettingError(sSetting);
      return false;
    }

    if ("".equals(m_cs.getConfigProperties().getProperty(sSetting)))
      return true;

    for (String s : m_cs.getConfigProperties().getProperty(sSetting).split(";")) {
      IFieldDefinition f = null;
      try {
        f = m_ms.getField(s.trim());
      } catch (ObjectNotFoundException e) {
        // eat it (done on purpose)
      }

      if (f == null) {
        this.signalSettingError(sSetting);
        return false;
      }

    }

    return true;
  }

  private boolean checkOneEnityPathSetting(final String sSetting) throws Exception {
    String sP = m_cs.getConfigProperties().getProperty(sSetting);
    if (this.isBare(sP))
      return true;// the setting might be missing or empty, that's okay

    sP = sP.trim();
    IGRCObject oE = this.fetchGRCObject(this.entityDisplayPathToFullPath(sP));
    if (oE == null) {
      this.signalSettingError(sSetting);
      return false;
    }

    if (!ksEntityType.equals(oE.getType().getName())) {
      this.signalSettingError(sSetting);
      return false;
    }

    return true;
  }



  /**
   * ------------------------------------------------------------- slurpUpVRMsToBeProcessed()
   * -------------------------------------------------------------
   */
  final static String ksStmtX =
      "SELECT [VendorRiskManagement].[Resource ID], [VendorRiskManagement].[Name] FROM [VendorRiskManagement]";

  protected Map<String, IGRCObject> slurpUpVRMsToBeProcessed() throws Exception {
    ASSERT(m_cs != null, "Bad logic!");
    ASSERT(m_qs != null, "Bad logic!");

    Map<String, IGRCObject> mRet = new HashMap<String, IGRCObject>();

    // prep (2)
    final String sSelectionSuffix = m_cs.getConfigProperties().getProperty(STRREG_SELECTION_SUFFIX);

    try {
      IQuery q = m_qs.buildQuery(ksStmtX);

      for (IPage page : q.fetchRows(0).getPages())
        for (IResultSetRow row : page) {
          // filter stuff out right away
          if (((IStringField) row.getField(1)).getValue().endsWith(sSelectionSuffix))
            mRet.put(((IStringField) row.getField(1)).getValue()
                .replace(m_cs.getConfigProperties().getProperty(STRREG_SELECTION_SUFFIX), "")
                .trim(), m_rs.getGRCObject(((IIdField) row.getField(0)).getValue()));// same naming
                                                                                     // logic as the
                                                                                     // one in
                                                                                     // VRMtpQTMigrationBatch
        }

      m_L.debug("[Slurped up VendorRiskManagements: ] " + mRet.values().toString());

    } catch (Exception e) {
      m_L.error(kPREFIX + " ERROR:" + e.getMessage());
      throw e;
    }

    return mRet;
  }

  /**
   * ------------------------------------------------------------- slurpUpNecessaryQTs() - uses the
   * strong assumption that the VRM names / QT names are unique!
   * -------------------------------------------------------------
   */
  final static String ksStmtY =
      "SELECT [QuestionnaireTemplate].[Resource ID], [QuestionnaireTemplate].[Name] FROM [QuestionnaireTemplate] WHERE [QuestionnaireTemplate].[Name] IN %IN_CLAUSE%";

  protected List<IGRCObject> slurpUpNecessaryQTs(final Set<String> lsQTNames) throws Exception {
    ASSERT(m_cs != null, "Bad logic!");
    ASSERT(m_qs != null, "Bad logic!");

    List<IGRCObject> lRet = new ArrayList<IGRCObject>();
    try {
      IQuery q = m_qs
          .buildQuery(ksStmtY.replace("%IN_CLAUSE%", this.putTogetherQTQueryInClause(lsQTNames)));

      for (IPage page : q.fetchRows(0).getPages())
        for (IResultSetRow row : page)
          lRet.add(m_rs.getGRCObject(((IIdField) row.getField(0)).getValue()));

      String sT = "[Slurped up QTs: ] ";
      for (IGRCObject o : lRet)
        sT += (o.getPath() + ", ");
      m_L.debug(sT);

    } catch (Exception e) {
      m_L.error(kPREFIX + " ERROR:" + e.getMessage());
      throw e;
    }

    return lRet;
  }

  /**
   * ------------------------------------------------------------- SOME NAVIGATIONAL METHODS They
   * use the strong assumptions that: - a section/sub-section title is unique among the set of all
   * the sections and sub-sections of a Questionnaire - the question title are unique within a
   * subsection -------------------------------------------------------------
   */

  protected ISection getSectionByTitle(final IQuestionnaire oQN, final String sN) {
    ASSERT(oQN != null, "Bad logic!");
    ASSERT(m_qns != null, "Bad logic!");
    ASSERT(!this.isBare(sN), "Bad usage!");
    for (ISection s : oQN.getSections())
      if (sN.equals(s.getTitle())) {
        ASSERT(s.getParentId() == null, "Bad, bad usage!");
        return s;
      }

    ASSERT(false, "Shouldn't be here...");
    return null;
  }

  protected ISection getSubsectionByTitleAndParent(final IQuestionnaire oQN, final Id idParent,
      final String sN) {
    ASSERT(oQN != null, "Bad logic!");
    ASSERT(m_qns != null, "Bad logic!");
    ASSERT(idParent != null, "Bad logic!");
    ASSERT(!this.isBare(sN), "Bad usage!");
    for (ISection s : oQN.getSections())
      if (sN.equals(s.getTitle()) && idParent.equals(
          s.getParentId())/* kind o=f superfluous since the section/subsection titles are unique */)
        return s;

    ASSERT(false, "Shouldn't be here...");
    return null;
  }

  protected IQuestion getQuestionByTitleAndParent(final IQuestionnaire oQN, final Id idParent,
      final String sN) {
    ASSERT(oQN != null, "Bad logic!");
    ASSERT(m_qns != null, "Bad logic!");
    ASSERT(idParent != null, "Bad logic!");
    ASSERT(!this.isBare(sN), "Bad usage!");
    for (IQuestion q : oQN.getQuestions())
      if (sN.equals(q.getTitle()) && idParent.equals(q.getParentId()))
        return q;

    ASSERT(false, "Shouldn't be here...");
    return null;
  }

  /**
   * ------------------------------------------------------------- getFieldByLabel() - strong
   * assumption (not unlike FastMap): the field labels are unique within an object (it actually
   * return the first with that label) -------------------------------------------------------------
   */
  protected IField getFieldByLabel(final IGRCObject o, final String sN) {
    ASSERT(o != null, "Bad logic!");
    ASSERT(!this.isBare(sN), "Bad usage!");
    for (IField f : o.getFields())
      if (sN.equals(f.getFieldDefinition().getLocalizedLabel()))
        return f;

    ASSERT(false, "Shouldn't be here...");
    return null;

  }

  /**
   * ------------------------------------------------------------- populateQuestion()
   * -------------------------------------------------------------
   */
  private static SimpleDateFormat ksdfAnswerDateFormat = null;

  private void populateQuestion(final IField fSource, IQuestionnaire oQN,
      ISection oParentSubsection) {
    ASSERT(fSource != null, "Bad logic!");
    ASSERT(oQN != null, "Bad logic!");
    ASSERT(oParentSubsection != null, "Bad logic!");
    ASSERT(oParentSubsection.getParentId() != null, "Bad logic!");// it should really be a
                                                                  // subsection, not a section

    IQuestion q = this.getQuestionByTitleAndParent(oQN, oParentSubsection.getId(),
        fSource.getFieldDefinition().getLocalizedLabel());

    if (fSource.isNull()) {
      q.setAnswer(null);
      return;
    }

    if (fSource instanceof IStringField)
      q.setAnswer(((IStringField) fSource).getValue());
    else if (fSource instanceof IBooleanField)
      q.setAnswer(((IBooleanField) fSource).getValue());
    else if (fSource instanceof IFloatField)
      q.setAnswer(((IFloatField) fSource).getValue());
    else if (fSource instanceof IIntegerField)
      q.setAnswer(((IIntegerField) fSource).getValue());
    else if (fSource instanceof IDateField)
      q.setAnswer(ksdfAnswerDateFormat.format(((IDateField) fSource).getValue()));
    else if (fSource instanceof ICurrencyField)
      q.setAnswer(((ICurrencyField) fSource).getLocalCurrency().getCurrencyCode().name()
          /* getSymbol() */ + " " + ((ICurrencyField) fSource).getLocalAmount());
    else if (fSource instanceof IEnumField)
      q.setAnswer(((IEnumField) fSource).getEnumValue().getIndex());
    else if (fSource instanceof IMultiEnumField) {
      List<Integer> lSelection = new ArrayList<Integer>();
      for (IEnumValue v : ((IMultiEnumField) fSource).getEnumValues())
        lSelection.add(v.getIndex());
      q.setAnswer(lSelection);
    } else
      ASSERT(false, "Field type not yet supported!");



  }

  /**
   * ------------------------------------------------------- sendOverallStatusEmail()
   * -------------------------------------------------------
   */
  private final static String ksStatsSection =
      "<div>Batch Job Completion Status:<BR/><BR/>Start Timestamp: %START_TIME%<BR/>End Timestamp: %END_TIME%<BR/><BR/>VRM to Questionnaire Template Migration Batch Processing Results:<BR/></div>"
          + "<div style=\"padding: 24px;\">Total: %TOTAL%<BR/>Success: %SUCCESS%<BR/></div><BR/>"
          + "<div>Detailed information:</div><BR/>";// LAI:TODO Fa-l string!!!

  private String putTogetherFeedback() {
    return ksStatsSection.replace("%START_TIME%", ksdfValidDateFormat.format(m_dStartTimestamp))
        .replace("%END_TIME%", this.getCurrentTimeStamp())
        .replace("%TOTAL%", new Integer(m_nSuccessCounter + m_nErrorCounter).toString())
        .replace("%SUCCESS%", new Integer(m_nSuccessCounter).toString())
        .replace("%FAILED%", new Integer(m_nErrorCounter).toString()) + m_sStatusInfo;
  }

  /**
   * ------------------------------------------------------- sendOverallStatusEmail()
   * -------------------------------------------------------
   */
  private void sendOverallStatusEmail() throws Exception {

    String sT = AuroraEnv.getProperty(SoxPropertyConstants.APPLICATION_URL_PATH)
        .replace("http://", "").replace("https://", "");
    sT = sT.substring(0, sT.lastIndexOf("/openpages"));
    final String sSubject = this.loadAppStr(IDS_OVERALL_EMAIL_SUBJECT, new Object[] {sT});

    this.sendEmail(sSubject, this.putTogetherFeedback(), m_cs.getConfigProperties()
        .getProperty(STRREG_OVERALL_EMAIL_RECIPIENTS).split(",")/* safe */);
  }



  /**
   * ------------------------------------------------------- sendEmail()
   * -------------------------------------------------------
   */
  private void sendEmail(final String sSubject, final String sBody, final String[] asAdds) {

    try {
      final String sMS = m_cs.getConfigProperties()
          .getProperty("/OpenPages/Applications/Common/Email/Mail Server");
      if (this.isBare(sMS)) {
        _L(this.loadAppStr(IDS_ERROR_MISSING_MAIL_SERVER));
        return;
      }

      final String sPWD = m_cs.getConfigProperties()
          .getProperty("/OpenPages/Applications/Common/Email/SMTP Password");
      final String sUID = m_cs.getConfigProperties()
          .getProperty("/OpenPages/Applications/Common/Email/SMTP User Name");

      // plumbing
      Properties props = new Properties();
      props.put("mail.smtp.host", sMS);

      final String sPN =
          m_cs.getConfigProperties().getProperty("/OpenPages/Applications/Common/Email/SMTP Port");
      if (!this.isBare(sPN))
        props.put("mail.smtp.port", sPN);

      if (!this.isBare(sUID)) {// Not very clean, need to find out why AuroraEnv.isCloud() doesn't
                               // work
        props.put("mail.smtp.auth", "true");

        final String sProxyIP = m_cs.getConfigProperties()
            .getProperty("/OpenPages/Applications/Common/Email/SOCKS Proxy Private IP Address");
        if (!this.isBare(sProxyIP))
          props.put("mail.smtp.socks.host", sProxyIP);

        props.put("mail.smtp.ssl.enable", "true");
      }

//
//      Session session = Session.getInstance(props, this.isBare(sUID) ? null : new Authenticator() {
//        protected PasswordAuthentication getPasswordAuthentication() {
//          return new PasswordAuthentication(sUID, sPWD);
//        }
//      });
//      // plumbing
//      MimeMessage msg = new MimeMessage(session);
//
//      // from
//      final String sFrom = m_cs.getConfigProperties().getProperty(STRREG_EMAIL_FROM);
//      msg.setFrom(new InternetAddress((this.isBare(sFrom) ? ksDefaultFrom : sFrom)));
//
//      // to
//      InternetAddress[] m_aRecipients = new InternetAddress[asAdds.length];
//      for (int ii = 0; ii < asAdds.length; ii++)
//        m_aRecipients[ii] = new InternetAddress(asAdds[ii] != null ? asAdds[ii].trim() : null);
//
//      msg.setRecipients(Message.RecipientType.TO, m_aRecipients);
//
//      // subject
//      msg.setSubject(sSubject, "utf-8");
//
//      // content
//      msg.setContent(sBody, "text/html;charset=utf-8");
//
//      // ...now send
//      Transport.send(msg);

      // clean up
  //    msg = null;
  //    session = null;
    } catch (Exception e) {
      _L("Email not sent.  Cause: " + e.getMessage());
      // throw e;
    }
  }


  /**
   * ------------------------------------------------------------- getDisplayableCurrentUserName() -
   * convenience -------------------------------------------------------------
   */
  protected String getDisplayableCurrentUserName() throws Exception {
    String sFN = m_ss.getCurrentUser().getFirstName();
    String sLN = m_ss.getCurrentUser().getLastName();

    if (sFN == null && sLN == null)
      return m_ss.getCurrentUser().getName();

    return sFN + " " + sLN + "(" + m_ss.getCurrentUser().getName() + ")";
  }


  /**
   * ------------------------------------------------------------- getCurrentTimeStamp() --
   * shorthand -------------------------------------------------------------
   */
  protected String getCurrentTimeStamp() {
    return ksdfValidDateFormat.format(Calendar.getInstance().getTime());
  }



  /**
   *
   * Constants, constants....
   *
   */
  final private static String IDS_ERROR_ALREADY_RUNNING =
      "com.frg.vrmtoqaresponserolloverbatch.error.already.running";// "[{0}]: VRM to Questionnaire
                                                                   // Assessment Response Roll-over
                                                                   // Batch already running."
  final private static String IDS_ERROR_MISSING_MAIL_SERVER =
      "com.frg.vrmtoqaresponserolloverbatch.error.missing.mail.server";// "[{0}]: Mail server not
                                                                       // set in the Registry. No
                                                                       // email sent!"
  final private static String IDS_ERROR_INVALID_SETTING =
      "com.frg.vrmtoqaresponserolloverbatch.error.invalid.setting";// "Invalid registry setting: "
  final private static String IDS_ERROR_PROLOGUE_FAILED =
      "com.frg.vrmtoqaresponserolloverbatch.error.prologue.failed";// "Prologue execution failed.
                                                                   // Processing stopped."
  final private static String IDS_NOTHING_TO_PROCESS =
      "com.frg.vrmtoqaresponserolloverbatch.nothing.to.process";// "Nothing to process!"
  final private static String IDS_OVERALL_EMAIL_SUBJECT =
      "com.frg.vrmtoqaresponserolloverbatch.overall.email.subject";// "[SR&amp;RM OpenPages] - VRM
                                                                   // to Questionnaire Assessment
                                                                   // Response Roll-over Batch Job
                                                                   // Status"



  final private static String STRREG_EMAIL_FROM =
      "/OpenPages/Custom Deliverables/FirstRand/VRMMigrationBatch/Email From";// VRMMigrationBatch@openpages.com
  final private static String STRREG_OVERALL_EMAIL_RECIPIENTS =
      "/OpenPages/Custom Deliverables/FirstRand/VRMMigrationBatch/Overall Status Email Recipients";
  final private static String STRREG_SELECTION_SUFFIX =
      "/OpenPages/Custom Deliverables/FirstRand/VRMMigrationBatch/Selection Suffix";// "_2020"
  final private static String STRREG_VRM_FIELDS =
      "/OpenPages/Custom Deliverables/FirstRand/VRMMigrationBatch/VRM Fields";// "FR-Annexure
                                                                              // :Annexure Type;
                                                                              // FR-Annexure
                                                                              // :Arrangement Type;
                                                                              // FR-Annexure :Legal
                                                                              // Name of Service
                                                                              // Provider;
                                                                              // FR-Annexure
                                                                              // :Holding Company
                                                                              // Name; FR-Annexure
                                                                              // :Approved By;
                                                                              // FR-Annexure :Date
                                                                              // of Approval;
                                                                              // FR-Annexure
                                                                              // :Business Owner;
                                                                              // FR-Annexure :Risk
                                                                              // Manager;
                                                                              // FR-Annexure
                                                                              // :Assigned To;
                                                                              // FR-Annexure :Date
                                                                              // of Next Review;
                                                                              // FR-Annexure
                                                                              // :Description of
                                                                              // Arrangement;
                                                                              // FR-Annexure
                                                                              // :Service Material;
                                                                              // FR-Annexure
                                                                              // :Primary Reason for
                                                                              // Material
                                                                              // Classification;
                                                                              // FR-Annexure :Type
                                                                              // of Approval;
                                                                              // FR-Annexure :Cloud
                                                                              // Question Part 1;
                                                                              // FR-Annexure
                                                                              // :Industry;
                                                                              // FR-Annexure :Other
                                                                              // Industry;
                                                                              // FR-Annexure
                                                                              // :Cluster;
                                                                              // FR-Annexure
                                                                              // :Contract Expiry
                                                                              // Date; FR-Annexure
                                                                              // :Name of Alternate
                                                                              // Provider;
                                                                              // FR-Annexure
                                                                              // :Contract Ongoing;
                                                                              // FR-Annexure
                                                                              // :Contract Period
                                                                              // Date From;
                                                                              // FR-Annexure
                                                                              // :Contract Period
                                                                              // Date To;
                                                                              // FR-Annexure :Total
                                                                              // Contract Value;
                                                                              // FR-Annexure :Prior
                                                                              // Total Exposure
                                                                              // Value; FR-Annexure
                                                                              // :Current Total
                                                                              // Exposure Value"
  final private static String STRREG_QUESTION_FIELDS =
      "/OpenPages/Custom Deliverables/FirstRand/VRMMigrationBatch/Question Fields";// "FR-Outsourcing:Question;
                                                                                   // FR-Outsourcing:Answer;
                                                                                   // FR-Outsourcing:Answer
                                                                                   // NA;
                                                                                   // FR-Outsourcing:Risk
                                                                                   // Rating;
                                                                                   // FR-Outsourcing:Risk
                                                                                   // Rating NA;
                                                                                   // FR-Outsourcing:Overall
                                                                                   // Risk Rating;
                                                                                   // FR-Outsourcing:Attestation
                                                                                   // Answer;
                                                                                   // FR-Outsourcing:Free
                                                                                   // Text Answer;
                                                                                   // FR-Outsourcing:Rationale;
                                                                                   // FR-Outsourcing:Substantiation
                                                                                   // Plan;
                                                                                   // FR-Outsourcing:Action
                                                                                   // Plan;
                                                                                   // FR-Outsourcing:Date
                                                                                   // for Action
                                                                                   // Plan;
                                                                                   // FR-Outsourcing:Responsible
                                                                                   // Person;
                                                                                   // FR-Outsourcing:Evidence
                                                                                   // Available"
  final private static String STRREG_COLLECTOR_ENTITY =
      "/OpenPages/Custom Deliverables/FirstRand/VRMMigrationBatch/Collector Entity";// "/Questionnaire
                                                                                    // Template
                                                                                    // Library"
  final private static String STRREG_PAIRING_BASE_FILENAME =
      "/OpenPages/Custom Deliverables/FirstRand/VRMMigrationBatch/Pairing Base File Name";// "/VRMMigrationPairs"
  final private static String STRREG_MASTER_SECTION_SUFFIX =
      "/OpenPages/Custom Deliverables/FirstRand/VRMMigrationBatch/Master Section Suffix";// "-Master"
  final private static String STRREG_PAIRING_FILE_DIR =
      "/OpenPages/Custom Deliverables/FirstRand/VRMMigrationBatch/Pairings File Directory";// "."
  final private static String STRREG_DATE_FORMAT =
      "/OpenPages/Custom Deliverables/FirstRand/VRMMigrationBatch/Date Format";// "d MMM yyyy
                                                                               // HH:mm:ss"


  final private static String ksNewLine = String.format("%n");
  final private static SimpleDateFormat ksdfValidDateFormat =
      new SimpleDateFormat("MM-dd-yyyy HH:mm:ss,SSS");
  final private static String ksDefaultFrom = "VRMMigrationBatch@openpages.com";
  final private static String ksEntityType = "SOXBusEntity";
  final private static String ksVRMType = "VendorRiskManagement";
  final private static String ksQAType = "QuestionnaireAssessment";
  final private static String ksQTType = "QuestionnaireTemplate";
  final private static String ksVRMQsectionType = "Qsection";
  final private static String ksVRMQuestType = "Quest";



}
