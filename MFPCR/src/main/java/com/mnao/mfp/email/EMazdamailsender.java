/*
 * Created on Jan 4, 2006
 * By: 
 */
package com.mnao.mfp.email;

/**
 * @author Naveen Lather 
 *  <B>Type name:</B>EMazdamailsender
 *  <B>Description:</B> 
 * <BR><BR>
 * <B>Creation Date: </B>Jan 4, 2006  
 * <BR><BR>
 *  @version:  
 *  <BR><BR>
 *  <B>Patterns Used: </B>  List of patterns used if applicable
 *  <BR><BR>
 *  Copyright 2006 by Mazda North America Operations, Inc.,
 *  7755 Irvine Center Drive
 *  Irvine, CA 92623, U.S.A.
 *  All rights reserved.
 *  <BR><BR>
 *  This software is the confidential and proprietary information of 
 *  Mazda North America Operations Inc. ("Confidential Information").
 *  You shall not disclose such Confidential Information and shall use 
 *  it only is accordance with the terms of the license agreement
 *  you entered into with Mazda North American Operations.
 *  <B>Change history:</B>
 * 
 */
/*
import com.mazdausa.common.configuration.EMDCSPropertiesManager;
import com.mazdausa.common.configuration.EMDCSProperties;
import com.mazdausa.common.exceptions.PropertyManagerNotFoundException;
import com.mazdausa.common.exceptions.PropertiesNotFoundException;
import com.mazdausa.common.constants.CommonConstants;
import com.mazdausa.common.log.CommonFrameWorkEMDCSLogger;
import org.apache.mazdalog4j.Logger;
*/

import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mnao.mfp.common.util.AppConstants;
import com.mnao.mfp.common.util.Utils;
import com.mnao.mfp.list.controller.ListController;

public class EMazdamailsender extends EMazdamail {
	private static final Logger log = LoggerFactory.getLogger(ListController.class);

	/**
	 * This is the only constructor, it loads the properties through property
	 * manager and sets the SMTP Host. Creation date: (01/06/06 8:46:51 AM)
	 */
	public EMazdamailsender() {

		this.smtpHost = Utils.getAppProperty(AppConstants.SMTP_HOST);

	}

	/**
	 * This method sends messages to the single/multiple receipients, without
	 * attachments. The to, cc and bcc parameters should be array of strings
	 * Creation date: (01/06/06 8:46:51 AM)
	 * 
	 * @param String   from: Sender eMail Address, No Space allowed in String.
	 * @param String[] to: Receiver eMail Address, No Space allowed in String.
	 * @param String[] cc: Receiver eMail Address, No Space allowed in String.
	 * @param String[] bcc: Receiver eMail Address, No Space allowed in String.
	 * @param String   subject: Subject Text, Space allowed in String.
	 * @param String   message: Message, Space allowed in String.
	 */
	public void SendMazdaMail(String from, String[] to, String[] cc, String[] bcc, String subject, String message)
			throws MessagingException {
		sendMail(from, to, cc, bcc, subject, message, null);
	}

	/**
	 * This method sends messages to the single/multiple receipients, with
	 * attachments. The to, cc and bcc parameters should be array of strings
	 * Creation date: (01/06/06 8:46:51 AM)
	 * 
	 * @param String   from: Sender eMail Address, No Space allowed in String.
	 * @param String[] to: Receiver eMail Address, No Space allowed in String.
	 * @param String[] cc: Receiver eMail Address, No Space allowed in String.
	 * @param String[] bcc: Receiver eMail Address, No Space allowed in String.
	 * @param String   subject: Subject Text, Space allowed in String.
	 * @param String   message: Message, Space allowed in String.
	 * @param String[] attachments: Attachments
	 */
	public void SendMazdaMail(String from, String[] to, String[] cc, String[] bcc, String subject, String message,
			String[] attachments) throws MessagingException {
		sendMail(from, to, cc, bcc, subject, message, attachments);
	}

	/**
	 * This method sends messages to the single/multiple receipients, with or
	 * without attachments. The to, cc and bcc parameters should be srings, with
	 * multiple email addresses separated by a delimitor Creation date: (01/06/06
	 * 8:46:51 AM)
	 * 
	 * @param String   from: Sender eMail Address, No Space allowed in String.
	 * @param String   to: Receiver eMail Address, No Space allowed in String.
	 * @param String   cc: Receiver eMail Address, No Space allowed in String.
	 * @param String   bcc: Receiver eMail Address, No Space allowed in String.
	 * @param String   subject: Subject Text, Space allowed in String.
	 * @param String   message: Message, Space allowed in String.
	 * @param String[] attachments: Attachments, pass null if there are no
	 *                 attachments
	 * @param String   delimitor: Delimiter, if null, ";" will be used
	 */
	public void SendMazdaMail(String from, String to, String cc, String bcc, String subject, String message,
			String[] attachments, String delimiter) throws MessagingException {
		if (delimiter == null)
			delimiter = Utils.getAppProperty(AppConstants.DEFAULT_DELIM);
		if (delimiter == null || delimiter.trim().length() == 0)
			delimiter = ";";
		// The above check should throw illegalArgument exception
		sendMail(from, (to != null) ? to.split(delimiter) : null, (cc != null) ? cc.split(delimiter) : null,
				(bcc != null) ? bcc.split(delimiter) : null, subject, message, attachments);
	}

	/**
	 * This method sends messages to the single/multiple receipients, with or
	 * without attachments. The to, cc and bcc parameters should be srings, with
	 * multiple email addresses separated by default delimitor(semi-colon). Creation
	 * date: (01/06/06 8:46:51 AM)
	 * 
	 * @param String   from: Sender eMail Address, No Space allowed in String,
	 *                 default delimiter is ";"
	 * @param String   to: Receiver eMail Address, No Space allowed in String,
	 *                 default delimiter is ";"
	 * @param String   cc: Receiver eMail Address, No Space allowed in String,
	 *                 default delimiter is ";"
	 * @param String   bcc: Receiver eMail Address, No Space allowed in String,
	 *                 default delimiter is ";"
	 * @param String   subject: Subject Text, Space allowed in String.
	 * @param String   message: Message, Space allowed in String.
	 * @param String[] attachments: Attachments, pass null if there are no
	 *                 attachments *
	 */
	public void SendMazdaMail(String from, String to, String cc, String bcc, String subject, String message,
			String[] attachments) throws MessagingException {
		String delimiter = Utils.getAppProperty(AppConstants.DEFAULT_DELIM);
		if (delimiter == null || delimiter.trim().length() == 0)
			delimiter = ";";
		sendMail(from, (to != null) ? to.split(delimiter) : null, (cc != null) ? cc.split(delimiter) : null,
				(bcc != null) ? bcc.split(delimiter) : null, subject, message, attachments);
	}

	private String BuildFinalMessage(Throwable exception, String message) {
		String stackTrace = BuildStack(exception);
		message = message + "\n" + stackTrace;
		return message;
	}

	private String BuildStack(Throwable exception) {
		String stackTrace = exception.toString();
		StackTraceElement[] stackTraceElement = exception.getStackTrace();

		for (int i = 0; i < stackTraceElement.length; i++) {
			stackTrace = stackTrace + stackTraceElement[i].toString();
		}

		return stackTrace;
	}

	public void SendMazdaMail(String from, String to, String subject, String message) throws MessagingException {
		try {
			prepareSession();// Prepare the session
			log.debug(
					"Attempting to send message from " + from + " with subject " + subject + " and message " + message);
			this.mailto(from, to, subject, message);
		} catch (MessagingException Mexp) {
			log.error("Exception occured while sending mail " + Mexp.getMessage() + "SMTP Server is "
					+ this.getSmtpHost());
			// let the implementor handle the exception, I will log it here
			throw Mexp;
		}

	}

	private void sendMail(String from, String[] to, String[] cc, String[] bcc, String subject, String message,
			String[] attachments) throws MessagingException {
		try {
			prepareSession();// Prepare the session
			log.debug(
					"Attempting to send message from " + from + " with subject " + subject + " and message " + message);
			this.mailto(from, to, cc, bcc, subject, message, attachments);
		} catch (MessagingException Mexp) {
			log.error("Exception occured while sending mail " + Mexp.getMessage() + "SMTP Server is "
					+ this.getSmtpHost());
			// let the implementor handle the exception, I will log it here
			throw Mexp;
		}

	}

}
