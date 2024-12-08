//package com.nexgen.bankingsystem.util;
//
//import jakarta.mail.Folder;
//import jakarta.mail.Message;
//import jakarta.mail.Session;
//import jakarta.mail.Store;
//import org.springframework.stereotype.Component;
//
//import java.util.Properties;
//
//@Component
//public class EmailReceiver {
//
//    public void checkInbox() throws Exception {
//        Properties properties = new Properties();
//        properties.put("mail.store.protocol", "imaps");
//
//        // Create a mail session
//        Session session = Session.getDefaultInstance(properties, null);
//
//        // Connect to the mail store
//        Store store = session.getStore("imaps");
//        store.connect("imap.gmail.com", "nexgin.bank@gmail.com", "ntwz qlpj kdmq enun");
//
//        // Open the inbox folder
//        Folder inbox = store.getFolder("INBOX");
//        inbox.open(Folder.READ_ONLY);
//
//        // Iterate through messages
//        for (Message message : inbox.getMessages()) {
//            // Print email subject
//            System.out.println("Subject: " + message.getSubject());
//
//            // Print sender's email
//            System.out.println("From: " + message.getFrom()[0].toString());
//
//            // Print recipient(s)
//            if (message.getAllRecipients() != null) {
//                System.out.println("To: ");
//                for (jakarta.mail.Address address : message.getAllRecipients()) {
//                    System.out.println("\t" + address.toString());
//                }
//            }
//
//            // Print sent date
//            System.out.println("Sent Date: " + message.getSentDate());
//
//            // Print received date (if available)
//            System.out.println("Received Date: " + message.getReceivedDate());
//
//            // Retrieve and print the message content
//            try {
//                Object content = message.getContent();
//
//                if (content instanceof String) {
//                    // If the email content is plain text
//                    System.out.println("Content: " + content);
//                } else if (content instanceof jakarta.mail.Multipart) {
//                    // If the email content is multipart (e.g., has attachments)
//                    jakarta.mail.Multipart multipart = (jakarta.mail.Multipart) content;
//                    for (int i = 0; i < multipart.getCount(); i++) {
//                        jakarta.mail.BodyPart bodyPart = multipart.getBodyPart(i);
//                        if (bodyPart.isMimeType("text/plain")) {
//                            // Plain text content
//                            System.out.println("Plain Text: " + bodyPart.getContent());
//                        } else if (bodyPart.isMimeType("text/html")) {
//                            // HTML content
//                            System.out.println("HTML Content: " + bodyPart.getContent());
//                        } else {
//                            // Handle attachments or other content types
//                            System.out.println("Attachment/File: " + bodyPart.getFileName());
//                        }
//                    }
//                } else {
//                    // Handle other content types
//                    System.out.println("Unsupported Content Type: " + content.getClass().getName());
//                }
//            } catch (Exception e) {
//                System.out.println("Error reading email content: " + e.getMessage());
//            }
//
//            System.out.println("-------------------------------------------------------------");
//        }
//
//        // Close resources
//        inbox.close(false);
//        store.close();
//    }
//}
