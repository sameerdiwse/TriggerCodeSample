/**
 * IBM Confidential Copyright IBM Corporation 2020 The source code for this program is not published
 * or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S.
 * Copyright Office.
 */

package com.ibm.openpages.ext.custom.util;

import java.io.File;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import org.apache.log4j.Logger;
import com.ibm.openpages.api.application.EmailOptions;
import com.ibm.openpages.api.configuration.IConfigProperties;
import com.ibm.openpages.api.security.IUser;
import com.ibm.openpages.api.service.IConfigurationService;
import com.ibm.openpages.api.service.local.application.EmailInternalUtil;
import com.openpages.apps.common.util.StringUtil;
import com.openpages.ext.solutions.common.LoggerUtilExtended;

public class EmailUtilities {

  // /Platform/Publishing/Mail/From Address
  private static final String GENERIC_OP_EMAIL_SENDER_ADDRESS_SETTING =
      "/OpenPages/Platform/Publishing/Mail/From Address";

  private static final String MAIL_SERVER_SETTING =
      "/OpenPages/Applications/Common/Email/Mail Server";

  static final String style =
      "<style type=\"text/css\">table, th, td {border: 1px solid #D4E0EE;border-collapse: collapse;color: #555;font-size: + fontSize + %;text-align: center;}caption {margin: 5px;font-size: + fontSize + %;}td, th {	padding: 4px;font-size: + fontSize + %;	}thead th {	text-align: center;	background: #416FA3;color: #FFFFFF;	font-size: + fontSize + %;}tbody th {font-size: + fontSize + %;	} tbody tr {background: #FCFDFE;font-size: + fontSize + %;}	tbody tr.odd {background: #F7F9FC;font-size: + fontSize + %;}table a:link {	color: #718ABE;	text-decoration: none;	font-size: + fontSize + %;	}table a:visited {	color: #718ABE;	text-decoration: none;	font-size: + fontSize + %;} table a:hover {	color: #718ABE;	font-size: + fontSize + %;} tfoot th, tfoot td {font-size: + fontSize + %;}br {	line-height: 25%;}button {font-size: + fontSize + %;}</style>";
  private Logger logger = null;
  private IConfigurationService configurationService;
  private Locale locale;
  private IConfigProperties settings;

  public EmailUtilities(IConfigurationService configurationService, Locale locale, Logger logger)
      throws Exception {
    this(configurationService, locale);
    this.logger = logger;
  }
  
  public EmailUtilities(IConfigurationService configurationService, Logger logger)
      throws Exception {
    this(configurationService, null, logger);
    this.logger = logger;
  }

  public EmailUtilities(IConfigurationService configurationService, Locale locale)
      throws Exception {
    this.configurationService = configurationService;
    this.locale = locale;
    this.logger = LoggerUtilExtended.getLogger(EmailUtilities.class.getSimpleName());
  }

  public void setLocale(Locale locale) {
    this.locale = locale;
  }

  /**
   * Sends the Notification
   * 
   * @param body
   * @param reportText
   */

  public void prepareAndSendEmail(EmailProperty emailProperty) throws Exception {
    logger.debug("Entering prepareAndSendEmail()");

    String subject = buildEmailSubject(emailProperty);
    String content = buildEmailMessage(emailProperty, style);

    if (isValidEmailAddress(emailProperty.getToAddress())
        || isValidEmailAddress(emailProperty.getCcAddresses())) {
      Exception exception = null;
      try {
        sendEmail(emailProperty.getMailServer(), emailProperty.getFromName(),
            emailProperty.getFromAddress(), emailProperty.getReplyToAddress(),
            emailProperty.getToAddress(), emailProperty.getCcAddresses(), subject, content,
            emailProperty.getAttachmentName(), emailProperty.getAttachmentFile(),
            emailProperty.getPdfAttachmentBytes());
      } catch (SendFailedException sfe) {
        exception = sfe;
      } catch (Exception e) {
        exception = e;
      } finally {
        if (exception != null) {
          logger.error("Exception occurred trying to send email", exception);
          if (exception instanceof SendFailedException) {
            Address[] validSent = ((SendFailedException) exception).getValidSentAddresses();
            Address[] validUnsent = ((SendFailedException) exception).getValidUnsentAddresses();
            Address[] invalid = ((SendFailedException) exception).getInvalidAddresses();
            logger.error("validSent: " + Arrays.asList(validSent) + ", validUnsent: "
                + Arrays.asList(validUnsent) + ", invalid: " + Arrays.asList(invalid));
          }

          throw exception;
        }
      }
    } else if (!isValidEmailAddress(emailProperty.getToAddress())) {
      throw new SendFailedException("Invalid recipientAddress: " + emailProperty.getToAddress());
    }

    logger.debug("Exiting prepareAndSendEmail()");
  }

  public void notifyByEmail(IUser recipientUser, String subjectAppTextKey, String bodyAppTextKey,
      List<String> bodyPlaceholderValues) throws Exception {
    logger.debug("Entered notifyByEmail(recipientUser: name " + recipientUser.getName()
        + ", subjectAppTextKey: " + subjectAppTextKey + "; bodyAppTextKey: " + bodyAppTextKey
        + ", bodyPlaceholderValues: " + bodyPlaceholderValues + ")");

    String fromAddress = getSetting(GENERIC_OP_EMAIL_SENDER_ADDRESS_SETTING);

    String toAddress = recipientUser.getEmailAddress();
    Locale userLocale = recipientUser.getLocale();
    logger.debug("Recipient user is " + recipientUser.getName() + "; email address: " + toAddress
        + "; locale: " + userLocale.getDisplayName() + "; language: " + userLocale.getLanguage()
        + "; fromAddress: " + fromAddress);

    setLocale(userLocale);

    EmailUtilities emailUtilities = new EmailUtilities(configurationService, userLocale, logger);
    EmailProperty emailProperty = new EmailProperty();
    emailProperty.setFromAddress(fromAddress);// OP Registry setting ; generic email address
    emailProperty.setToAddress(toAddress);
    emailProperty.setSubjectKey(subjectAppTextKey);
    emailProperty.setContentKey(bodyAppTextKey);
    emailProperty.setContentPlaceHolderValues(bodyPlaceholderValues.toArray(new String[0]));
    emailUtilities.prepareAndSendEmail(emailProperty);

    logger.debug("Exiting notifyByEmail()");
  }

  private boolean isValidEmailAddress(String email) {
    logger.debug("Entering isValidEmailAddress(" + email + ")");

    boolean result = true;
    if (email != null) {
      try {
        InternetAddress emailAddr = new InternetAddress(email);
        emailAddr.validate();
      } catch (AddressException ex) {
        logger.error("Exception validating email address " + email);
        result = false;
      }
    } else {
      result = false;
    }
    logger.debug("Exiting isValidEmailAddress() - returning " + result);
    return result;
  }

  private String buildEmailSubject(EmailProperty emailProperty) throws Exception {
    logger.debug("Entered buildEmailSubject()");
    String subject = "";
    if (emailProperty.getSubjectKey() != null) {
      subject =
          configurationService.getLocalizedApplicationText(emailProperty.getSubjectKey(), locale);
      if (subject != null && emailProperty.getSubjectPlaceHolderValues() != null) {

        Object[] placeholderValues = (Object[]) emailProperty.getSubjectPlaceHolderValues();
        for (int i = 0; i < placeholderValues.length; i++) {
          subject = subject.replace("{" + i + "}", (String) placeholderValues[i]);
        }
        logger.debug("subject with filled in placeholders is: " + subject);
      }
    }
    logger.debug("Exiting buildEmailSubject() - returning " + subject);
    return subject;
  }

  private String buildEmailMessage(EmailProperty emailProperty, String styleStr) throws Exception {
    logger.debug("Entered buildEmailMessage()");
    String emailMessage = "";

    String contentAppText = null;
    contentAppText =
        configurationService.getLocalizedApplicationText(emailProperty.getContentKey(), locale);
    if (contentAppText == null) {
      throw new Exception("No Application Text was defined on Application Text Key "
          + emailProperty.getContentKey());
    }

    if (emailProperty.getContentPlaceHolderValues() != null) {

      Object[] placeholderValues = (Object[]) emailProperty.getContentPlaceHolderValues();
      for (int i = 0; i < placeholderValues.length; i++) {
        contentAppText = contentAppText.replace("{" + i + "}", (String) placeholderValues[i]);
      }

      // logger.debug("emailAppTextContent with filled in placeholders is: " + contentAppText);
    }

    String appTextLowerCase = contentAppText.toLowerCase();
    if (appTextLowerCase.contains("<html") && appTextLowerCase.contains("</html>")) {
      emailMessage = contentAppText;
    } else if (appTextLowerCase.contains("<body") && appTextLowerCase.contains("</body>")) {
      emailMessage =
          "<!DOCTYPE HTML><html><head>" + styleStr + "</head>" + contentAppText + "</html>";
    } else {
      emailMessage = "<!DOCTYPE HTML><html><head>" + styleStr + "</head><body>";
      // Append Email header
      if (emailProperty.getHeaderKey() != null) {
        String emailHeader =
            configurationService.getLocalizedApplicationText(emailProperty.getHeaderKey(), locale);
        if (emailHeader != null) {
          emailMessage += emailHeader;
          // Append new line character after header
          emailMessage += "<br>";
        }
      }

      // Append Email Content
      emailMessage += contentAppText;

      if (emailProperty.getFooterKey() != null) {
        // Append new line character before footer
        emailMessage += "<br>";

        // Append Email footer
        String emailFooter =
            configurationService.getLocalizedApplicationText(emailProperty.getFooterKey(), locale);
        if (emailFooter != null)
          emailMessage += emailFooter;
      }
      emailMessage += "</body></html>";
    }
    logger.debug("Exiting buildEmailContent() - returning " + emailMessage);
    return emailMessage;
  }

  /** <b>Important:</b>Currently this method supports only PDF Attachment type */
  private void sendEmail(String mailServer, String fromName, String fromAddress,
      String replyToAddress, String toAddress, String ccAddresses, String subject,
      String emailContent, String attachmentName, File attachmentFile, byte[] pdfAttachmentBytes)
      throws Exception {
    logger.debug("Entered sendNotification(\n" + mailServer + "\n, " + fromName + "\n, "
        + fromAddress + "\n, " + replyToAddress + "\n, " + toAddress + "\n, " + ccAddresses + "\n, "
        + subject + "\n, " + emailContent + "\n, " + attachmentName + ", tempAttachmentFile name: "
        + (attachmentFile != null ? attachmentFile.getName() : "null")
        + ", pdfAttachmentBytes size: "
        + (pdfAttachmentBytes != null ? pdfAttachmentBytes.length : "null"));

    try {
      if (mailServer == null) {
        mailServer = getSetting(MAIL_SERVER_SETTING);
      }

      EmailOptions options = new EmailOptions();
      options.setSmtpServer(mailServer);

      Session session = getEmailSession(options);

      MimeMessage msg = new MimeMessage(session);
      InternetAddress addressFrom = null;
      if (StringUtil.isGood(fromName))
        addressFrom = new InternetAddress(fromAddress, fromName);
      else
        addressFrom = new InternetAddress(fromAddress);
      msg.setFrom(addressFrom);

      if (toAddress != null && !toAddress.equalsIgnoreCase("")) {
        String[] toAddressesArray = toAddress.split(";");
        InternetAddress[] addressTO = new InternetAddress[toAddressesArray.length];
        for (int i = 0; i < toAddressesArray.length; i++) {
          addressTO[i] = new InternetAddress(toAddressesArray[i]);
        }
        msg.setRecipients(Message.RecipientType.TO, addressTO);
      }

      if (replyToAddress != null && !replyToAddress.equals("")) {
        InternetAddress addressReplyTo = new InternetAddress(replyToAddress);
        msg.setReplyTo(new InternetAddress[] {addressReplyTo});
      }

      if (ccAddresses != null && !ccAddresses.equalsIgnoreCase("")) {
        String[] ccAddressesArray = ccAddresses.split(";");
        InternetAddress[] addressCC = new InternetAddress[ccAddressesArray.length];
        for (int i = 0; i < ccAddressesArray.length; i++) {
          addressCC[i] = new InternetAddress(ccAddressesArray[i]);
        }
        msg.setRecipients(Message.RecipientType.CC, addressCC);
      }

      msg.setSubject(subject, "utf-8");

      if (attachmentFile != null) {
        // create the message part
        MimeBodyPart messageBodyPart = new MimeBodyPart();

        // fill message
        messageBodyPart.setText(emailContent, "utf-8");
        messageBodyPart.setHeader("Content-Type", "text/html");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);

        // Part two is attachment
        MimeBodyPart attachmentPart = new MimeBodyPart();
        DataSource source = new FileDataSource(attachmentFile);
        attachmentPart.setDataHandler(new DataHandler(source));
        attachmentPart
            .setFileName(attachmentName != null ? attachmentName : attachmentFile.getName());
        attachmentPart.setHeader("Content-Type", "application/pdf");
        multipart.addBodyPart(attachmentPart);

        // Put parts in message
        msg.setContent(multipart, "text/html; charset=utf-8");
      } else if (pdfAttachmentBytes != null) {
        // create the message part
        MimeBodyPart messageBodyPart = new MimeBodyPart();

        // fill message
        messageBodyPart.setText(emailContent, "utf-8");
        messageBodyPart.setHeader("Content-Type", "text/html");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);

        // Part two is attachment
        MimeBodyPart attachmentPart = new MimeBodyPart();
        DataSource source = new ByteArrayDataSource(pdfAttachmentBytes, "application/pdf");
        attachmentPart.setDataHandler(new DataHandler(source));
        attachmentPart.setFileName(attachmentName);
        attachmentPart.setHeader("Content-Type", "application/pdf");
        multipart.addBodyPart(attachmentPart);

        // Put parts in message
        msg.setContent(multipart, "text/html; charset=utf-8");
      } else {
        msg.setContent(emailContent, "text/html;charset=utf-8");
      }

      msg.saveChanges();

      Transport.send(msg);

      msg = null;
      session = null;
    } catch (

    Exception e) {
      logger.error("Error occured while sending Email :- " + e.getMessage());
      e.printStackTrace();
      throw e;
    }

    logger.debug("Exiting sendNotification()");
  }

  private static Session getEmailSession(EmailOptions options) throws Exception {
    Session session = null;
    Authenticator auth = EmailInternalUtil.getServerAuthenticator(options);
    if (auth != null) {
      session = Session.getInstance(EmailInternalUtil.getServerProperties(options), auth);
    } else {
      session = Session.getInstance(EmailInternalUtil.getServerProperties(options));
    }

    return session;

  }

  private String getSetting(String settingPath) {
    if (settings == null) {
      settings = configurationService.getConfigProperties();
    }
    String setting = settings.getProperty(settingPath);
    if (setting != null) {
      setting = setting.trim();
    }
    logger.debug("getSetting(" + settingPath + ") -> returning \"" + setting + "\".");
    return setting;
  }
}
