/**
 * IBM Confidential Copyright IBM Corporation 2020 The source code for this program is not published
 * or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S.
 * Copyright Office.
 */

package com.ibm.openpages.ext.custom.util;

import java.io.File;

public class EmailProperty {

  private String contentKey;
  private String subjectKey;
  private String[] subjectPlaceHolderValues;
  private String[] contentPlaceHolderValues;

  private String fromName;
  private String fromAddress;
  private String toAddress;
  private String ccAddresses;
  private String htmlRow;
  private boolean printReportHeader;
  private String headerKey;
  private String header;
  private String footerKey;
  private String reportTitle;
  private String label;
  private String attachmentPath;
  private String attachmentName;

  private String replyToAddress;

  private String mailServer;
  private File attachmentFile;
  private byte[] pdfAttachmentBytes;

  public String getContentKey() {
    return contentKey;
  }

  public void setContentKey(String emailContentKey) {
    this.contentKey = emailContentKey;
  }

  public String getSubjectKey() {
    return subjectKey;
  }

  public String[] getContentPlaceHolderValues() {
    return contentPlaceHolderValues;
  }

  public void setContentPlaceHolderValues(String[] contentPlaceHolderValues) {
    this.contentPlaceHolderValues = contentPlaceHolderValues;
  }

  public String[] getSubjectPlaceHolderValues() {
    return subjectPlaceHolderValues;
  }

  public void setSubjectPlaceHolderValues(String[] subjectPlaceHolderValues) {
    this.subjectPlaceHolderValues = subjectPlaceHolderValues;
  }

  public void setSubjectKey(String subjectKey) {
    this.subjectKey = subjectKey;
  }

  public String getFromName() {
    return fromName;
  }

  public void setFromName(String fromName) {
    this.fromName = fromName;
  }

  public String getFromAddress() {
    return fromAddress;
  }

  public void setFromAddress(String fromAddress) {
    this.fromAddress = fromAddress;
  }

  public String getToAddress() {
    return toAddress;
  }

  public void setToAddress(String toAddress) {
    this.toAddress = toAddress;
  }

  public String getCcAddresses() {
    return ccAddresses;
  }

  public void setCcAddresses(String ccAddresses) {
    this.ccAddresses = ccAddresses;
  }

  public String getHtmlRow() {
    return htmlRow;
  }

  public void setHtmlRow(String htmlRow) {
    this.htmlRow = htmlRow;
  }

  public boolean isPrintReportHeader() {
    return printReportHeader;
  }

  public void setPrintReportHeader(boolean printReportHeader) {
    this.printReportHeader = printReportHeader;
  }

  public String getHeaderKey() {
    return headerKey;
  }

  public void setHeaderKey(String headerKey) {
    this.headerKey = headerKey;
  }

  public String getHeader() {
    return header;
  }

  public void setHeader(String header) {
    this.header = header;
  }

  public String getReportTitle() {
    return reportTitle;
  }

  public void setReportTitle(String reportTitle) {
    this.reportTitle = reportTitle;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getFooterKey() {
    return footerKey;
  }

  public void setFooterKey(String footerKey) {
    this.footerKey = footerKey;
  }

  public String getMailServer() {
    return mailServer;
  }

  public void setMailServer(String mailServer) {
    this.mailServer = mailServer;
  }

  public File getAttachmentFile() {
    return attachmentFile;
  }

  public byte[] getPdfAttachmentBytes() {
    return pdfAttachmentBytes;
  }

  public void setPdfAttachmentBytes(byte[] pdfAttachmentBytes) {
    this.pdfAttachmentBytes = pdfAttachmentBytes;
  }

  public void setAttachmentFile(File attachmentFile) {
    this.attachmentFile = attachmentFile;
  }

  public String getAttachmentPath() {
    return attachmentPath;
  }

  public void setAttachmentPath(String attachmentPath) {
    this.attachmentPath = attachmentPath;
  }

  public String getAttachmentName() {
    return attachmentName;
  }

  public void setAttachmentName(String attachmentName) {
    this.attachmentName = attachmentName;
  }

  public String getReplyToAddress() {
    return replyToAddress;
  }

  public void setReplyToAddress(String replyToAddress) {
    this.replyToAddress = replyToAddress;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("\nEmail content: " + this.contentKey);
    sb.append("\nEmail subjectKey: " + this.subjectKey);
    sb.append("\nEmail fromName: " + this.fromName);
    sb.append("\nEmail toAddress: " + this.toAddress);
    sb.append("\nEmail ccAddresses: " + this.ccAddresses);
    sb.append("\nEmail htmlRow: " + this.htmlRow);
    sb.append("\nEmail printReportHeader: " + this.printReportHeader);
    sb.append("\nEmail headerKey: " + this.headerKey);
    sb.append("\nEmail footerKey: " + this.footerKey);
    sb.append("\nEmail reportTitle: " + this.reportTitle);
    sb.append("\nEmail label: " + this.label);
    sb.append("\nEmail mailServer: " + this.mailServer);

    return sb.toString();
  }
}
