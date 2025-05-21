package com.ftn.uns.ac.rs.smarthome.services;

import com.ftn.uns.ac.rs.smarthome.config.MqttConfiguration;
import com.sendgrid.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.Properties;

@Service
//public class MailService {
//    private final JavaMailSender emailSender;
//    private final Properties env;
//
//    public MailService(JavaMailSender emailSender) throws IOException {
//        this.emailSender = emailSender;
//        this.env = new Properties();
//        env.load(MqttConfiguration.class.getClassLoader().getResourceAsStream("application.properties"));
//    }
//    public void sendSimpleMessage(String to, String subject, String text) {
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setFrom("smarthome@noreply.com");
//        message.setTo(to);
//        message.setSubject(subject);
//        message.setText(text);
//        emailSender.send(message);
//    }
//
////    public boolean sendTextEmail(String receiver, String subject, String text) throws IOException {
////        Email from = new Email("varga.sv54.2020@uns.ac.rs");
////        Email to = new Email(receiver);
////        Content content = new Content("text/html", text);
////        Mail mail = new Mail(from, subject, to, content);
////
////        SendGrid sg = new SendGrid(this.env.getProperty("sendgrid.api-key"));
////        Request request = new Request();
////        request.setMethod(Method.POST);
////        request.setEndpoint("mail/send");
////        request.setBody(mail.build());
////        try {
////            sg.api(request);
////            return true;
////        } catch (Exception ex) {
////            System.out.println(ex.getMessage());
////            return false;
////        }
////    }
//
//
//    public boolean sendTextEmail(String receiver, String subject, String htmlContent) {
//        try {
//            MimeMessage message = emailSender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
//
//            helper.setFrom("your_email@gmail.com");
//            helper.setTo(receiver);
//            helper.setSubject(subject);
//            helper.setText(htmlContent, true); // HTML enabled
//
//            emailSender.send(message);
//            return true;
//        } catch (MessagingException e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//}
public class MailService {
    private final JavaMailSender emailSender;

    // Inject email gửi từ application.properties
    @Value("${spring.mail.username}")
    private String fromEmail;

    public MailService(JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }

    // Gửi email đơn giản (text thuần)
    public void sendSimpleMessage(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        emailSender.send(message);
    }

    // Gửi email HTML
    public boolean sendTextEmail(String receiver, String subject, String htmlContent) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail); // dùng email cấu hình
            helper.setTo(receiver);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true = cho phép HTML

            emailSender.send(message);
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }
}