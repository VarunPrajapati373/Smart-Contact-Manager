package com.smart.service;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.stereotype.Service;


@Service
public class EmailService {
	
	
public boolean sendEmail(String subject, String message, String to) {
		
		boolean status= false;
		
		String from="varunprajapati373@gmail.com";
		
		//variable for Gmail
				String host="smtp.gmail.com";
				
				//get the system properties
				Properties properties = System.getProperties();
				System.out.println("PROPERTIES "+ properties);
				
				//setting important information to properties object
				
				properties.put("mail.smtp.host", host);
				properties.put("mail.smtp.port", "465");
				properties.put("mail.smtp.ssl.enable", "true");
				properties.put("mail.smtp.auth", "true");
				
				
				//Step :1 get the session object
				Session session= Session.getInstance(properties, new Authenticator() {

					@Override
					protected PasswordAuthentication getPasswordAuthentication() {
						// TODO Auto-generated method stub
						return new PasswordAuthentication("varunprajapati373@gmail.com", "hmlsoxvthshohfou");
					}
				
				});
				
				session.setDebug(true);
				
				//Step :2 compose the message [text, multimedia]
				MimeMessage m = new MimeMessage(session);
				
				
				try {
					//from email
					m.setFrom(from);
					
					// adding recipent
					m.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
					
					//adding subject to message
					
					m.setSubject(subject);
					
					//adding body data
					//m.setText(message);
					m.setContent(message,"text/html");
			
					//sending message
					Transport.send(m);
					
					System.out.println("Send Success.........");
					
					status = true;
					
				} catch (MessagingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				return status;
			}

}
