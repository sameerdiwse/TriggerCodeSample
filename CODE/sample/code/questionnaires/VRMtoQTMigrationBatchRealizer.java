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
 * "date":"07/15/2020",
 * 
 * "summary":"VRMtoQTMigrationBatchRealizer",
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
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
import com.ibm.openpages.api.metadata.DataType;
import com.ibm.openpages.api.metadata.IEnumValue;
import com.ibm.openpages.api.metadata.IFieldDefinition;
import com.ibm.openpages.api.metadata.Id;
import com.ibm.openpages.api.query.IPage;
import com.ibm.openpages.api.query.IQuery;
import com.ibm.openpages.api.query.IResultSetRow;
import com.ibm.openpages.api.questionnaire.AnswerType;
import com.ibm.openpages.api.questionnaire.IQuestion;
import com.ibm.openpages.api.questionnaire.IQuestionOption;
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



public class VRMtoQTMigrationBatchRealizer {


  /*
   * ..etc...
   */
  private static String kPREFIX = "[[VRMtoQTMigrationBatchRealizer]]: ";


  /**
   * ------------------------------ fields ------------------------------
   */
//  private HttpServletRequest m_req = null;

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
  private Map<Id, Id> m_mapVRMtoQT = new HashMap<Id, Id>();

  // misc
  Date m_dStartTimestamp = null;
  int m_nSuccessCounter, m_nErrorCounter;

  /**
   * ------------------------------ Constructor ------------------------------
   */
  public VRMtoQTMigrationBatchRealizer(/*HttpServletRequest */String req) throws Exception {
    /*
     * m_req = req;
     */
    try {
      IServiceFactory sf = null;// ServiceFactory.getServiceFactory(m_req);
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
    VRMtoQTMigrationBatchRealizer.m_kRunningAlready = true;
    m_sStatusInfo = "";
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
  private void registerOneItem(final IGRCObject oVRM, final IGRCObject oQT, final String sStatus,
      final String sErrorDetail) throws Exception {

    if (this.isBare(m_sStatusInfo))
      m_sStatusInfo = ksTABLE_STYLE + ksSTATUS_TABLE_PROLOGUE;

    m_sStatusInfo += ksSTATUS_TABLE_ROW.replace("%VRM%", this.makeATag(oVRM))
        .replace("%QT%", (oQT == null ? "-" : this.makeATag(oQT))).replace("%STATUS%", sStatus)
        .replace("%ERROR_DETAILS%", sErrorDetail);
  }

  private void wrapUpStatus() {
    if (m_sStatusInfo.contains(ksSTATUS_TABLE_PROLOGUE))
      m_sStatusInfo += ksSTATUS_TABLE_EPILOGUE;
  }

  private void registerOneSuccess(final IGRCObject oVRM, final IGRCObject oQT) throws Exception {
    this.registerOneItem(oVRM, oQT, "Success", "");
    m_L.debug("VendorRiskManagement object succesfully processed: " + oVRM.getName());
    m_nSuccessCounter++;
  }

  private void registerOneError(final IGRCObject oVRM, final Exception e) throws Exception {
    this.registerOneItem(oVRM, null, "Error", e.getMessage());
    m_L.error("Error processing VendorRiskManagement object : " + oVRM.getName() + ksNewLine
        + "CAUSE: " + e.getMessage() + ksNewLine + VRMtoQTMigrationBatchRealizer.printStack(e));
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
  protected String loadAppStr______(final String sKey) throws Exception {
    return sKey;
  }

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

  protected String loadAppStr____(final String sKey, Object[] aSubstVals) throws Exception {
    return sKey + "---->" + aSubstVals.toString();
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

  /* The assumption here is that the section names/titles are unique (!) */
  protected ISection getSectionByTitle(final IQuestionnaire oQNR, final String sN) {
    ASSERT(oQNR != null && !this.isBare(sN), "Invalid usage!");
    for (ISection s : oQNR.getSections())
      if (sN.equals(s.getTitle()))
        return s;
    ASSERT(false, "Wow, check logic!");
    return null;// keep the compiler happy
  }


  final private static String ksTABLE_STYLE =
      "<style> table{ border-width: 1px; border-spacing: 0px; border-style: solid; border-color: black; border-collapse: collapse; width:100%; } th { border-width: 1px; border-style: solid; border-color: black; background-color:lightblue; } td { border-width: 1px; border-style: solid; border-color: black; } </style>";

  final private static String ksSTATUS_TABLE_PROLOGUE =
      "<table><tbody><tr> <th>Vendor Risk Management</th> <th>Questionnaire Template</th> <th>STATUS</th> <th>ERROR DETAILS</th> </tr>";
  final private static String ksSTATUS_TABLE_ROW =
      "<tr>  <td>%VRM%</td> <td>%QT%</td> <td>%STATUS%</td> <td>%ERROR_DETAILS%</td> </tr>";
  final private static String ksSTATUS_TABLE_EPILOGUE = "</tbody></table>";


  /**
   * ------------------------------------------------------- PUBLIC: proceed() - entry point
   * -------------------------------------------------------
   */

  @SuppressWarnings("unused")
  public String proceed______() throws Exception {
    try {
      IGRCObject oP = this.fetchGRCObject(this.entityDisplayPathToFullPath(
          m_cs.getConfigProperties().getProperty(STRREG_COLLECTOR_ENTITY)));


      IGRCObject oQT = m_rs.getResourceFactory().createGRCObject("QT_" + this.getCurrentTimeStamp(),
          m_ms.getType(ksQTType));
      oQT.setPrimaryParent(oP);
      oQT.setDescription("Manufacturat de conu'  Mielu'....");
      oQT = m_rs.saveResource(oQT);

      IQuestionnaire oQNR = m_qns.getQuestionnaire(oQT.getId());
      ISection oS = m_qns.getQuestionnaireFactory().createSection();
      oS.setTitle("Titlu ISection");
      oQNR.addSection(oS);
      m_qns.saveQuestionnaire(oQNR);

      ISection oSS = m_qns.getQuestionnaireFactory()
          .createSubSection(oQNR.getSections().get(0/* !!! */).getId());
      oSS.setTitle("Titlu ISection (ca subsection)");
      oQNR.addSection(oSS);
      m_qns.saveQuestionnaire(oQNR);

      Id id1 = oS.getParentId();
      Id id2 = oSS.getParentId();


      ISection oSSS = oQNR.getSections().get(1/* !!! */);// ;

      IQuestion oQ = this.assembleQuestion("FR-Annexure:Annexure Type", oSSS);
      oQNR.addQuestion(oQ);

      // for(String sF : m_cs.getConfigProperties().getProperty(STRREG_VRM_FIELDS).split(";")){
      // oQNR.addQuestion(this.assembleQuestion(sF, oSSS));
      // }
      m_qns.saveQuestionnaire(oQNR);
      Id id3 = oSS.getParentId();



      String sss = SoxPropertyConstants.VERSIONS_TABLE;

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
      if (VRMtoQTMigrationBatchRealizer.m_kRunningAlready) {
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

      // process VRMs
      for (IGRCObject x : this.slurpUpVRMsToBeProcessed()) {
        bNothingToProcess = false;
        this.processOneItem(x);
      }


      if (bNothingToProcess) {
        String sM = this.loadAppStr(IDS_NOTHING_TO_PROCESS);
        _L(sM);
        m_sStatusInfo += ("<h2>" + sM + "</h2><BR/>");
      }

      return this.putTogetherFeedback();
    } catch (Exception e) {
      final String sMess = "UNRECOVERABLE ERROR: " + e.getMessage() + ksNewLine + "<BR/>"
          + VRMtoQTMigrationBatchRealizer.printStack(e);
      _L(sMess);
      m_sStatusInfo += sMess;
      return this.putTogetherFeedback() + "<BR/>" + sMess;
    } catch (AssertionError e) {
      final String sStack = VRMtoQTMigrationBatchRealizer.printStack(e);
      _L(e.getMessage() + ksNewLine + sStack);
      m_sStatusInfo += (e.getMessage() + "<BR/>" + sStack);
      return e.getMessage() + "<BR/>" + sStack;
    } finally {
      VRMtoQTMigrationBatchRealizer.m_kRunningAlready = false;
      this.wrapUpStatus();// meh...
      this.dumpPairs();
      this.sendOverallStatusEmail();
    }


  }

  /**
   * ------------------------------------------------------------- generateRemarkableSection()
   * -------------------------------------------------------------
   */
  private void generateRemarkableSection(IQuestionnaire oQNR, final String sTitle) {
    ASSERT(oQNR != null, "Bad logic!");
    ASSERT(!this.isBare(sTitle), "Bad logic!");

    ISection oS = m_qns.getQuestionnaireFactory().createSection();
    oS.setTitle(sTitle + m_cs.getConfigProperties().getProperty(STRREG_MASTER_SECTION_SUFFIX));
    oQNR.addSection(oS);
    m_qns.saveQuestionnaire(oQNR);

    ISection oSSM = m_qns.getQuestionnaireFactory()
        .createSubSection(this.getSectionByTitle(oQNR, oS.getTitle()).getId());
    oSSM.setTitle(m_cs.getConfigProperties().getProperty(STRREG_MASTER_SECTION_SUFFIX));
    oQNR.addSection(oSSM);
    m_qns.saveQuestionnaire(oQNR);

    oSSM = this.getSectionByTitle(oQNR, oSSM.getTitle());
    for (String sF : m_cs.getConfigProperties().getProperty(STRREG_VRM_FIELDS).split(";")) {
      oQNR.addQuestion(this.assembleQuestion(sF, oSSM));
    }
    m_qns.saveQuestionnaire(oQNR);

  }

  /**
   * ------------------------------------------------------------- generateSecondarySubsection()
   * -------------------------------------------------------------
   */
  private void generateSomeOrdinarySection(final IGRCObject oQsection, IQuestionnaire oQNR) {
    ASSERT(oQsection != null, "Bad logic!");
    ASSERT(ksQsectionType.equals(oQsection.getType().getName()), "Bad usage!");
    ASSERT(oQNR != null, "Bad logic!");

    ISection oS = m_qns.getQuestionnaireFactory().createSection();
    oS.setTitle(oQsection.getName());
    oS.setDescription(oQsection.getDescription());
    if (!oQsection.getField(ksOrderFieldName).isNull())
      oS.setOrder(((IIntegerField) oQsection.getField(ksOrderFieldName)).getValue());
    oQNR.addSection(oS);
    m_qns.saveQuestionnaire(oQNR);

    oS = this.getSectionByTitle(oQNR, oS.getTitle());// dumb, eh?....
    for (IGRCObject oQuest : this.getAllChildrenOfType(oQsection.getId(), ksQuestionType)) {
      // subsection
      ISection oSS = m_qns.getQuestionnaireFactory().createSubSection(oS.getId());
      oSS.setTitle(oQuest.getName());
      oSS.setDescription(oQuest.getDescription());
      if (!oQuest.getField(ksOrderFieldName).isNull())
        oS.setOrder(((IIntegerField) oQuest.getField(ksOrderFieldName)).getValue());
      oQNR.addSection(oSS);
      m_qns.saveQuestionnaire(oQNR);
      oSS = this.getSectionByTitle(oQNR, oSS.getTitle());// dumb....
      // questions
      for (String sF : m_cs.getConfigProperties().getProperty(STRREG_QUESTION_FIELDS).split(";")) {
        oQNR.addQuestion(this.assembleQuestion(sF, oSS));
      }
      m_qns.saveQuestionnaire(oQNR);
    }
  }

  /**
   * ------------------------------------------------------------- dumpPairs() NOTE: it eats any
   * exception - no point in stopping everything just because the pairing file cannot be
   * created/written to -------------------------------------------------------------
   */
  private void dumpPairs() {
    if (m_mapVRMtoQT.isEmpty())
      return;

    try {
      File fX = new File(m_cs.getConfigProperties().getProperty(STRREG_PAIRING_FILE_DIR) + ksSLASH
          + m_cs.getConfigProperties().getProperty(STRREG_PAIRING_BASE_FILENAME)
          + ksdfFileTimestamp.format(new Date()) + ".pairs");
      if (!fX.createNewFile()) {
        m_L.error("Cannot create pairings file");
        return;
      }
      String sX = "";
      for (Map.Entry<Id, Id> e : m_mapVRMtoQT.entrySet())
        sX += (e.getKey() + "=" + e.getValue() + ",");
      FileUtils.writeStringToFile(fX, sX);
    } catch (Exception e) {
      m_L.error("Cannot create or write to the pairings file.  Cause: " + e.getMessage());
    }

  }

  /**
   * ------------------------------------------------------------- processOneItem()
   * -------------------------------------------------------------
   */
  private void processOneItem(final IGRCObject oVRM) throws Exception {

    try {
      final String sT = oVRM.getName()
          .replace(m_cs.getConfigProperties().getProperty(STRREG_SELECTION_SUFFIX), "");
      // create QT
      IGRCObject oNewQT = m_rs.getResourceFactory().createGRCObject(sT, m_ms.getType(ksQTType));
      oNewQT.setDescription(ksBRANDING);
      oNewQT.setPrimaryParent(this.figureOutParent(oVRM));
      oNewQT = m_rs.saveResource(oNewQT);

      // register the mapping - (QT, VRM) pairs
      m_mapVRMtoQT.put(oVRM.getId(), oNewQT.getId());

      // build the template
      IQuestionnaire oQNR = m_qns.getQuestionnaire(oNewQT.getId());

      // - main section
      this.generateRemarkableSection(oQNR, sT);

      // - the other sections
      for (IGRCObject oQsection : this.getAllChildrenOfType(oVRM.getId(), ksQsectionType))
        this.generateSomeOrdinarySection(oQsection, oQNR);



      // final touch...
      this.registerOneSuccess(oVRM, oNewQT);
    } catch (OpenPagesException e) {
      this.registerOneError(oVRM, e);
    }
  }



  /**
   * ------------------------------------------------------------- processOneItem()
   * -------------------------------------------------------------
   */
  private IGRCObject figureOutParent(final IGRCObject oSource) {
    ASSERT(m_rs != null, "Uninitialized state!");
    ASSERT(oSource != null, "Bad logic!");
    ASSERT(ksVRMType.equals(oSource.getType().getName()), "Bad usage!");

    if (this.isBare(m_cs.getConfigProperties().getProperty(STRREG_COLLECTOR_ENTITY)))
      return (oSource.getPrimaryParent() == null ? null
          : m_rs.getGRCObject(oSource.getPrimaryParent()));
    // else....
    return this.fetchGRCObject(this.entityDisplayPathToFullPath(
        m_cs.getConfigProperties().getProperty(STRREG_COLLECTOR_ENTITY)));// safe
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
   * ------------------------------------------------------------- assembleQuestion()
   * -------------------------------------------------------------
   */
  @SuppressWarnings("unused")
  private IQuestion assembleQuestion(final IField fSource, ISection oS) {
    ASSERT(m_qns != null, "Uninitialized state!");
    ASSERT(m_qns.getQuestionnaireFactory() != null, "Uninitialized state!");

    IQuestion oQ = null;
    if (fSource instanceof IStringField || fSource instanceof IBooleanField
        || fSource instanceof IDateField || fSource instanceof IFloatField
        || fSource instanceof IIntegerField || fSource instanceof ICurrencyField) {
      oQ = m_qns.getQuestionnaireFactory().createQuestion(oS.getId());
      oQ.setAnswerType(AnswerType.TEXT);
    }

    if (fSource instanceof IEnumField) {
      oQ = m_qns.getQuestionnaireFactory().createQuestion(oS.getId());
      oQ.setAnswerType(AnswerType.RADIO);
    }
    if (fSource instanceof IMultiEnumField) {
      oQ = m_qns.getQuestionnaireFactory().createQuestion(oS.getId());
      oQ.setAnswerType(AnswerType.MULTI_CHECK);
    }

    if (fSource instanceof IEnumField || fSource instanceof IMultiEnumField) {
      ASSERT(oQ != null, "Something really bad happened!");
      List<IQuestionOption> lO = new ArrayList<IQuestionOption>();
      for (IEnumValue v : fSource.getFieldDefinition().getEnumValues())
        lO.add(m_qns.getQuestionnaireFactory().createQuestionOption(v.getLocalizedLabel(),
            v.getIndex(), v.getName(), 0, null));
      oQ.setOptions(lO);
    }

    ASSERT(oQ != null, "Field type not yet supported!");
    oQ.setTitle(fSource.getFieldDefinition().getLocalizedLabel());
    oQ.setDescription(fSource.getFieldDefinition().getName());
    return oQ;
  }

  private IQuestion assembleQuestion(final IFieldDefinition fSource, ISection oS) {
    ASSERT(m_qns != null, "Uninitialized state!");
    ASSERT(m_qns.getQuestionnaireFactory() != null, "Uninitialized state!");

    IQuestion oQ = null;
    switch (fSource.getDataType()) {
      case INTEGER_TYPE:
      case FLOAT_TYPE:
      case STRING_TYPE:
      case DATE_TYPE:
      case CURRENCY_TYPE:
      case BOOLEAN_TYPE:
      case MEDIUM_STRING_TYPE:
      case LARGE_STRING_TYPE:
      case UNLIMITED_STRING_TYPE:
        oQ = m_qns.getQuestionnaireFactory().createQuestion(oS.getId());
        oQ.setAnswerType(AnswerType.TEXT);
        break;
      case ENUM_TYPE:
        oQ = m_qns.getQuestionnaireFactory().createQuestion(oS.getId());
        oQ.setAnswerType(AnswerType.RADIO);
        break;
      case MULTI_VALUE_ENUM:
        oQ = m_qns.getQuestionnaireFactory().createQuestion(oS.getId());
        oQ.setAnswerType(AnswerType.MULTI_CHECK);
        break;
      default:
        ASSERT(false, "Field type not yet supported!");
    }

    if (fSource.getDataType() == DataType.ENUM_TYPE
        || fSource.getDataType() == DataType.MULTI_VALUE_ENUM) {
      ASSERT(oQ != null, "Something really bad happened!");
      List<IQuestionOption> lO = new ArrayList<IQuestionOption>();
      for (IEnumValue v : fSource.getEnumValues())
        lO.add(m_qns.getQuestionnaireFactory().createQuestionOption(v.getLocalizedLabel(),
            v.getIndex(), v.getName(), 0, null));
      oQ.setOptions(lO);
    }

    oQ.setTitle(fSource.getLocalizedLabel());
    oQ.setDescription(fSource.getName());

    return oQ;
  }

  private IQuestion assembleQuestion(final String sSourceF, ISection oS) {
    return this.assembleQuestion(m_ms.getField(sSourceF.trim()), oS);// safe....
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
    return bRet;
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

  protected List<IGRCObject> slurpUpVRMsToBeProcessed() throws Exception {
    ASSERT(m_cs != null, "Bad logic!");
    ASSERT(m_qs != null, "Bad logic!");

    List<IGRCObject> lRet = new ArrayList<IGRCObject>();

    // prep (2)
    final String sSelectionSuffix = m_cs.getConfigProperties().getProperty(STRREG_SELECTION_SUFFIX);

    try {
      IQuery q = m_qs.buildQuery(ksStmtX);

      for (IPage page : q.fetchRows(0).getPages())
        for (IResultSetRow row : page) {
          // filter stuff out right away
          if (((IStringField) row.getField(1)).getValue().endsWith(sSelectionSuffix))
            lRet.add(m_rs.getGRCObject(((IIdField) row.getField(0)).getValue()));
        }

      m_L.debug("[Slurped up VendorRiskManagements: ] " + lRet.toString());

    } catch (Exception e) {
      m_L.error(kPREFIX + " ERROR:" + e.getMessage());
      throw e;
    }

    return lRet;
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
//
//      // clean up
//      msg = null;
//      session = null;
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
      "com.frg.vrmtoqtmigrationbatch.error.already.running";// "[{0}]: VRM to Questionnaire Template
                                                            // Migration Batch already running."
  final private static String IDS_ERROR_MISSING_MAIL_SERVER =
      "com.frg.vrmtoqtmigrationbatch.error.missing.mail.server";// "[{0}]: Mail server not set in
                                                                // the Registry. No email sent!"
  final private static String IDS_ERROR_INVALID_SETTING =
      "com.frg.vrmtoqtmigrationbatch.error.invalid.setting";// "Invalid registry setting: "
  final private static String IDS_ERROR_PROLOGUE_FAILED =
      "com.frg.vrmtoqtmigrationbatch.error.prologue.failed";// "Prologue execution failed.
                                                            // Processing stopped."
  final private static String IDS_NOTHING_TO_PROCESS =
      "com.frg.vrmtoqtmigrationbatch.nothing.to.process";// "Nothing to process!"
  final private static String IDS_OVERALL_EMAIL_SUBJECT =
      "com.frg.vrmtoqtmigrationbatch.overall.email.subject";// "[SR&amp;RM OpenPages] - VRM to
                                                            // Questionnaire Template Migration
                                                            // Batch Job Status"



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


  final private static String ksNewLine = String.format("%n");
  final private static String ksSLASH = File.separator;
  final private static SimpleDateFormat ksdfValidDateFormat =
      new SimpleDateFormat("MM-dd-yyyy HH:mm:ss,SSS");
  final private static SimpleDateFormat ksdfFileTimestamp =
      new SimpleDateFormat("-yyyy.MM.HH.dd-mm.ss");
  final private static String ksDefaultFrom = "VRMMigrationBatch@openpages.com";
  final private static String ksEntityType = "SOXBusEntity";
  final private static String ksVRMType = "VendorRiskManagement";
  final private static String ksQsectionType = "Qsection";
  final private static String ksQuestionType = "Quest";
  final private static String ksQTType = "QuestionnaireTemplate";
  final private static String ksOrderFieldName = "FR-Outsourcing:Order";

  final private static String ksBRANDING = "Created by the VRM to Questionnaire Migration Batch";



}
