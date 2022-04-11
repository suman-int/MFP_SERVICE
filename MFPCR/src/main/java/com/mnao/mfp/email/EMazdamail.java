/*
 * Created on Jan 4, 2006
 * By: 
 */
package com.mnao.mfp.email;

/**
 * @author Naveen Lather
 *  <B>Type name:</B>EMazdamail
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
import javax.mail.*;
import javax.mail.internet.*;
import com.mnao.mfp.list.controller.ListController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EMazdamail
{
	private static final Logger log = LoggerFactory.getLogger(ListController.class);
    
    private java.lang.String _mimeType = "text/plain";
    private String DEFAULT_SMTPHOST = "smtp";

    //Mail session
    private Session session;
    protected String smtpHost;

    protected void mailto(String from, String to, String subject, String message)
            throws MessagingException {
            MimeMessage mimeMessage = new MimeMessage(session);
            mimeMessage.setFrom(new InternetAddress(from));
            mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            mimeMessage.setSubject(subject);
            mimeMessage.setContent(message, this.get_mimeType());
            log.debug("Sending message with from = "+from+" to= "+to+" subject= "+subject+" message= "+message);
            Transport.send(mimeMessage);
    }

    /**
     * Insert the method's description here.
     * Creation date: (3/19/2001 10:17:12 AM)
     */
    protected EMazdamail()
    {
    }

    protected void mailto(
            String from,
            String[] to,
            String[] cc,
            String[] bcc,
            String subject,
            String message)
            throws MessagingException {
            mailto(from, to, cc, bcc, subject, message, null);
    }


    /**
     * Insert the method's description here.
     * Creation date: (4/30/2002 10:41:03 AM)
     * @return java.lang.String
     */
    public java.lang.String get_mimeType() {
            return _mimeType;
    }

    /**
     * Insert the method's description here.
     * Creation date: (4/30/2002 11:12:01 AM)
     * @return java.lang.String
     */
    protected String getSmtpHost() {
          if(this.smtpHost == null)
          {
            log.debug("SMTP HOST is null, setting to default "+DEFAULT_SMTPHOST);  
            return DEFAULT_SMTPHOST;
          }
          else
          {  
            log.debug("SMTP HOST is "+this.smtpHost);  
            return this.smtpHost;
          }
    }

    protected void mailto(
            String from,
            String[] to,
            String[] cc,
            String[] bcc,
            String subject,
            String message,
            String[] attachments)
            throws MessagingException {
            MimeMessage mimeMessage = new MimeMessage(session);
            if (from != null)
                    mimeMessage.setFrom(new InternetAddress(from));
            if (to != null)
            {
                    for (int i = 0; i < to.length; i++)
                    {
                            mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(to[i]));
                            log.debug("Recepeint: To includes "+to[i]);
                    }
            }
            if (cc != null)
            {
                    for (int i = 0; i < cc.length; i++)
                    {
                            mimeMessage.addRecipient(Message.RecipientType.CC, new InternetAddress(cc[i]));
                            log.debug("Recepeint: cc includes "+cc[i]);
                    }
            }
            if (bcc != null)
            {
                    for (int i = 0; i < bcc.length; i++)
                    {
                            mimeMessage.addRecipient(
                                    Message.RecipientType.BCC,
                                    new InternetAddress(bcc[i]));
                            log.debug("Recepeint: bcc includes "+bcc[i]);
                    }
            }
            mimeMessage.setSubject(subject);
            //If there is any attachment, setup the multipart
            if (attachments != null)
            {
                    //Create the text part
                    MimeBodyPart mpart = new MimeBodyPart();
                    mpart.setContent(message, this.get_mimeType());

                    //Create the attachment
                    MimeBodyPart mattachPart = null;
                    Multipart mp = new MimeMultipart();
                    mp.addBodyPart(mpart);

                    for (int i = 0; i<attachments.length; i++)
                    {
                            javax.activation.FileDataSource dataSource =
                                    new javax.activation.FileDataSource(attachments[i]);
                            log.debug("Attachment: includes "+attachments[i]);
                            mattachPart = new MimeBodyPart();
                            mattachPart.setDataHandler(new javax.activation.DataHandler(dataSource));
                            mattachPart.setFileName(dataSource.getName());
                            //Default the description to the file name only
                            mattachPart.setDescription(dataSource.getFile().getName());

                            //add the attachment to the body part
                            mp.addBodyPart(mattachPart);
                    }
                    mimeMessage.setContent(mp);
                    mimeMessage.setSentDate(new java.util.Date());
            }
            else
            {
                    mimeMessage.setContent(message, this.get_mimeType());
            }
            Transport.send(mimeMessage);
    }

    /**
     * Insert the method's description here.
     * Creation date: (4/30/2002 11:08:38 AM)
     */
    protected void prepareSession()
    {
            java.util.Properties properties = System.getProperties();
            properties.put("mail.smtp.host", this.getSmtpHost());
            session = Session.getInstance(properties, null);
    }

    /**
     * Insert the method's description here.
     * Creation date: (4/30/2002 10:41:03 AM)
     * @param new_mimeType java.lang.String
     */
    public void set_mimeType(java.lang.String new_mimeType) {
            _mimeType = new_mimeType;
    }   

}
