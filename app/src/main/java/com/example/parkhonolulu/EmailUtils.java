package com.example.parkhonolulu;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class EmailUtils {
    public static void sendOtpEmail(String recipientEmail, String otp) {
        // SMTP Server Configuration
        String smtpHost = "smtp.gmail.com"; // Χρησιμοποιούμε τον SMTP server του Gmail
        String smtpPort = "587"; // SMTP port για TLS
        String senderEmail = "parkhonolulu@gmail.com"; // Το email από το οποίο θα στείλουμε το OTP
        String senderPassword = "ukbj bpvo uvwi guwh"; // ο κωδικός εφαρμογής

        // Ρύθμιση των ιδιοτήτων SMTP
        Properties properties = new Properties();
        properties.put("mail.smtp.host", smtpHost);
        properties.put("mail.smtp.port", smtpPort);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");

        // Δημιουργία της συνεδρίας (session) με τα SMTP properties
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });

        try {
            // Δημιουργία του email message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Your code for Login");
            message.setText("Your code for login is: " + otp + "\nThis code is valid for 5 minutes.");

            // Αποστολή του μηνύματος
            Transport.send(message);
            System.out.println("code sent to " + recipientEmail);
        } catch (MessagingException e) {
            // Αναλυτική εκτύπωση του σφάλματος
            System.err.println("Error sending code email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void sendOtpEmailforpassword(String recipientEmail, String otp) {
        // SMTP Server Configuration
        String smtpHost = "smtp.gmail.com"; // Χρησιμοποιούμε τον SMTP server του Gmail
        String smtpPort = "587"; // SMTP port για TLS
        String senderEmail = "parkhonolulu@gmail.com"; // Το email από το οποίο θα στείλουμε το OTP
        String senderPassword = "ukbj bpvo uvwi guwh"; // ο κωδικός εφαρμογής

        // Ρύθμιση των ιδιοτήτων SMTP
        Properties properties = new Properties();
        properties.put("mail.smtp.host", smtpHost);
        properties.put("mail.smtp.port", smtpPort);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");

        // Δημιουργία της συνεδρίας (session) με τα SMTP properties
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });

        try {
            // Δημιουργία του email message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Your code to change password");
            message.setText("Your code to change password is: " + otp + "\nThis code is valid for 5 minutes.");

            // Αποστολή του μηνύματος
            Transport.send(message);
            System.out.println("Code sent to " + recipientEmail);
        } catch (MessagingException e) {
            // Αναλυτική εκτύπωση του σφάλματος
            System.err.println("Error sending code email: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
