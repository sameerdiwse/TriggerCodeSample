/**
 * IBM Confidential Copyright IBM Corporation 2020 The source code for this program is not published
 * or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S.
 * Copyright Office.
 */

package com.ibm.openpages.ext.custom.trigger.handler;

import java.util.List;
import org.apache.log4j.Logger;
import com.ibm.openpages.api.Context;
import com.ibm.openpages.api.metadata.Id;
import com.ibm.openpages.api.resource.IGRCObject;
import com.ibm.openpages.api.service.ServiceFactory;
import com.ibm.openpages.api.trigger.events.AbstractResourceEvent;
import com.ibm.openpages.api.trigger.events.CreateResourceEvent;
import com.ibm.openpages.api.trigger.events.UpdateResourceEvent;
import com.ibm.openpages.api.trigger.ext.DefaultEventHandler;
import com.ibm.openpages.ext.custom.util.CommonUtils;
import com.openpages.ext.solutions.common.LoggerUtilExtended;

public class VendorBreachesProbabilityHandler extends DefaultEventHandler {

  private static final String IS_ENABLED_TRIGGER_SETTING = "Is Enabled";
  Context context = null;
  private Logger logger =
      LoggerUtilExtended.getLogger(VendorBreachesProbabilityHandler.class.getSimpleName());

  private CommonUtils utils;

  // /OpenPages/Solutions/Custom/Vendor-Breach Probability Trigger
  private static final String SETTINGS_PATH = "trigger.settingsPath";

  // An error occurred while calculating Breaches Probability on the Vendor. Please contact the
  // system administrator.
  private static final String GENERIC_ERROR_APP_TEXT_KEY =
      "com.posten.vendor.breaches.calculation.trigger.generic.error.message";

  // POS-VRM-Shared:Country
  private static final String VENDOR_COUNTRY_FIELD_SETTING = "Vendor Country Field";

  // POS-VRM-Shared:Industry
  private static final String VENDOR_INDUSTRY_FIELD_SETTING = "Vendor Industry Field";

  // POS-VRM-Pref:Risk Labour Rights
  private static final String PREF_COUNTRY_RISK_FIELD_SETTING = "Preference Country Risk Field";

  // POS-VRM-Pref:Likelihood Breaches
  private static final String PREF_INDUSTRY_RISK_FIELD_SETTING = "Preference Industry Risk Field";

  // POS-VRM-Vendor:Probability Breaches
  private static final String VENDOR_BREACHES_PROBABILITY_FIELD_SETTING =
      "Vendor Breaches Probability Field";

  // High
  private static final String HIGH_VALUE_SETTING = "High Value";

  // Medium
  private static final String MEDIUM_VALUE_SETTING = "Medium Value";

  // Low
  private static final String LOW_VALUE_SETTING = "Low Value";

  // SELECT [Preference].[Resource ID], [Preference].[Name],
  // [Preference].[POS-VRM-Pref:Risk Labour Rights], [Preference].[POS-VRM-Pref:Likelihood
  // Breaches],[Preference].[POS-VRM-Shared:Country],[Preference].[POS-VRM-Shared:Industry]
  // FROM [Preference]
  // WHERE [Preference].[POS-VRM-Shared:Country]= {0} AND [Preference].[POS-VRM-Shared:Industry] =
  // {1}
  private static final String GET_PREF_BY_COUNTRY_AND_INDUSTRY =
      "Query-Get Preference with Matching Country and Industry";


  // Description: Set Breaches Probability To Empty On Empty Country or Industry
  // true
  private static final String SET_EMPTY_PROBABILITY_ON_EMPTY_FIELD =
      "Set Empty Probability on Empty Field";

  private IGRCObject vendor = null;
  AbstractResourceEvent event = null;

  public boolean handleEvent(CreateResourceEvent event) {
    try {
      String settingsPathAttr = getAttributes().get(SETTINGS_PATH);
      utils = new CommonUtils(logger, ServiceFactory.getServiceFactory(event.getContext()),
          settingsPathAttr);

      if (!utils.getSettingWithDefaultValueIfNull(IS_ENABLED_TRIGGER_SETTING, "true")
          .equalsIgnoreCase("true")) {
        logger.debug(
            "Trigger is disabled in setting - exiting handleEvent(CreateResourceEvent event)");
      } else {
        vendor = (IGRCObject) event.getCreatedResource();
        calculateBreachesProbability();
        utils.saveResorce(vendor);
      }
    } catch (Exception e) {
      logger.error("Error occurred.", e);
      // throwException(
      // utils.getAppText(GENERIC_ERROR_APP_TEXT_KEY)
      // + (e.getMessage() == null ? "" : "\nMessage: " + e.getMessage())
      // + (e.getCause() == null ? "" : "\nCause: " + e.getCause()),
      // new ArrayList<Object>(), e, context);
    } finally {
      logger.debug("Exiting handleEvent(CreateResourceEvent event)");
    }
    return super.handleEvent(event);
  }

  public boolean handleEvent(UpdateResourceEvent event) {
    try {
      String settingsPathAttr = getAttributes().get(SETTINGS_PATH);
      utils = new CommonUtils(logger, ServiceFactory.getServiceFactory(event.getContext()),
          settingsPathAttr);

      if (!utils.getSettingWithDefaultValueIfNull(IS_ENABLED_TRIGGER_SETTING, "true")
          .equalsIgnoreCase("true")) {
        logger.debug(
            "Trigger is disabled in setting - exiting handleEvent(CreateResourceEvent event)");
      } else {
        vendor = (IGRCObject) event.getResource();
        calculateBreachesProbability();
      }
    } catch (Exception e) {
      logger.error("Error occurred.", e);
      // throwException(
      // utils.getAppText(GENERIC_ERROR_APP_TEXT_KEY)
      // + (e.getMessage() == null ? "" : "\nMessage: " + e.getMessage())
      // + (e.getCause() == null ? "" : "\nCause: " + e.getCause()),
      // new ArrayList<Object>(), e, context);
    } finally {
      logger.debug("Exiting handleEvent(UpdateResourceEvent event)");
    }
    return super.handleEvent(event);
  }

  private void calculateBreachesProbability() throws Exception {
    logger.debug("Entered calculateBreachesProbability()");

    String vendorCountry =
        utils.getFieldValueAsString(utils.getSetting(VENDOR_COUNTRY_FIELD_SETTING), vendor);
    String vendorIndustry =
        utils.getFieldValueAsString(utils.getSetting(VENDOR_INDUSTRY_FIELD_SETTING), vendor);
    String vendorBreachesProbability = utils
        .getFieldValueAsString(utils.getSetting(VENDOR_BREACHES_PROBABILITY_FIELD_SETTING), vendor);

    logger.debug("vendorCountry: " + vendorCountry + "; vendorIndustry: " + vendorIndustry
        + "\n current value of vendorBreachesProbability: " + vendorBreachesProbability);

    if (vendorCountry.isEmpty() || vendorIndustry.isEmpty() && utils
        .getSettingTrimmedEmptyStringIfNull(
            SET_EMPTY_PROBABILITY_ON_EMPTY_FIELD)
        .equalsIgnoreCase("true")) {
      // set field
      utils.setFieldValueOnResource(utils.getSetting(VENDOR_BREACHES_PROBABILITY_FIELD_SETTING),
          null, vendor);
    }

    String[] queryParams = new String[2];
    queryParams[0] = "'" + vendorCountry + "'";
    queryParams[1] = "'" + vendorIndustry + "'";
    String query = utils.getSetting(GET_PREF_BY_COUNTRY_AND_INDUSTRY);
    List<String> preferenceRecord =
        utils.getQueryResultsMultipleReturnFields(query, queryParams, 3);
    if (preferenceRecord.size() == 0) {
      logger.error("Didn't find any preference with country=" + vendorCountry + " and industry="
          + vendorIndustry);
      return;
    }
    if (preferenceRecord.size() > 1) {
      logger.error("Found more than one preference with country=" + vendorCountry + " and industry="
          + vendorIndustry
          + "; this should not happen; will use first Preference returned by the query service.");
    }

    IGRCObject preference = utils.getResourceService()
        .getGRCObject(new Id(utils.getIdOfRecord(preferenceRecord.get(0))));

    String prefCountryRisk_RiskLabourRights =
        utils.getFieldValueAsString(utils.getSetting(PREF_COUNTRY_RISK_FIELD_SETTING), preference);
    String predIndustryRisk_LikelihoodBreach =
        utils.getFieldValueAsString(utils.getSetting(PREF_INDUSTRY_RISK_FIELD_SETTING), preference);

    logger.debug("got preference " + preference.getName() + " having ID " + preference.getId()
        + "; prefCountryRisk_RiskLabourRights: " + prefCountryRisk_RiskLabourRights
        + ", predIndustryRisk_LikelihoodBreach: " + predIndustryRisk_LikelihoodBreach);


    String high = utils.getSettingTrimmedEmptyStringIfNull(HIGH_VALUE_SETTING);
    String medium = utils.getSettingTrimmedEmptyStringIfNull(MEDIUM_VALUE_SETTING);
    String low = utils.getSettingTrimmedEmptyStringIfNull(LOW_VALUE_SETTING);


    String valueToSet = low;
    if (prefCountryRisk_RiskLabourRights.equalsIgnoreCase(high)
        || predIndustryRisk_LikelihoodBreach.equalsIgnoreCase(high)) {
      valueToSet = high;
    } else if (prefCountryRisk_RiskLabourRights.equalsIgnoreCase(medium)
        || predIndustryRisk_LikelihoodBreach.equalsIgnoreCase(medium)) {
      valueToSet = medium;
    }

    // set field
    utils.setFieldValueOnResource(utils.getSetting(VENDOR_BREACHES_PROBABILITY_FIELD_SETTING),
        valueToSet, vendor);

    logger.debug("Exiting calculateBreachesProbability()");
  }
}
