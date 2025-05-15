package com.fabiocondo.security.service;

import com.sun.mail.smtp.SMTPTransport;
import org.springframework.stereotype.Service;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.Properties;

import static com.fabiocondo.constant.EmailConstant.*;
import static javax.mail.Message.RecipientType.CC;
import static javax.mail.Message.RecipientType.TO;

@Service
public class EmailService {

    public void sendOtpCodeEmail(String email, String otpCode) throws MessagingException {
        Message message = createEmail(email, otpCode);
        SMTPTransport smtpTransport = (SMTPTransport) getEmailSession().getTransport(SIMPLE_MAIL_TRANSFER_PROTOCOL);
        smtpTransport.connect(GMAIL_SMTP_SERVER, USERNAME, PASSWORD);
        smtpTransport.sendMessage(message, message.getAllRecipients());
        smtpTransport.close();
    }

    private Message createEmail(String email, String otpCode) throws MessagingException {
        Message message = new MimeMessage(getEmailSession());
        message.setFrom(new InternetAddress(FROM_EMAIL));
        message.setRecipients(TO, InternetAddress.parse(email, false));
        message.setRecipients(CC, InternetAddress.parse(CC_EMAIL, false));
        message.setSubject(EMAIL_SUBJECT);
        //message.setText("Olá " + firstName + ", \n \n Username: "  + username + "\n \n E o seu novo password é: " + password + "\n \n My Admin App Support Team");
        //message.setContent("<p>Olá <strong>" + firstName + "</strong>,</p><p>Seu novo password é: <strong>" + password + "</strong></p>", "text/html; charset=utf-8");
        message.setContent(buildHtmlContent(otpCode), "text/html; charset=utf-8");
        message.setSentDate(new Date());
        message.saveChanges();
        return message;
    }

    private Session getEmailSession() {
        Properties properties = System.getProperties();
        properties.put(SMTP_HOST, GMAIL_SMTP_SERVER);
        properties.put(SMTP_AUTH, true);
        properties.put(SMTP_PORT, DEFAULT_PORT);
        properties.put(SMTP_STARTTLS_ENABLE, true);
        properties.put(SMTP_STARTTLS_REQUIRED, true);
        return Session.getInstance(properties, null);
    }

    private String buildHtmlContent(String otpCode) {
        return String.format(
                "<!DOCTYPE html>" +
                        "<html lang=\"pt\">" +
                        "<head>" +
                        "    <meta charset=\"UTF-8\">" +
                        "    <style>" +
                        "        body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 0; }" +
                        "        .container { background-color: #ffffff; max-width: 600px; margin: 30px auto; padding: 30px; border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.1); }" +
                        "        h2 { color: #4c6ef5; text-align: center; }" +
                        "        p { font-size: 16px; line-height: 1.5; color: #555; }" +
                        "        .otp { background-color: #e8edff; padding: 15px; border-radius: 5px; margin-top: 20px; font-size: 24px; font-weight: bold; color: #4c6ef5; text-align: center; }" +
                        "        .footer { margin-top: 30px; font-size: 13px; color: #999; text-align: center; }" +
                        "    </style>" +
                        "</head>" +
                        "<body>" +
                        "    <div class=\"container\">" +
                        "        <h2>Verificação de Segurança – Eduka+</h2>" +
                        "        <p>Recebemos uma solicitação para verificar sua identidade.</p>" +
                        "        <p>Utilize o código abaixo para continuar com o processo:</p>" +
                        "        <div class=\"otp\">%s</div>" +
                        "        <p>Este código expira em 10 minutos. Se você não solicitou este código, ignore este e-mail.</p>" +
                        "        <div class=\"footer\">" +
                        "            Eduka+ • Suporte Técnico<br>" +
                        "            Não responda a este e-mail." +
                        "        </div>" +
                        "    </div>" +
                        "</body>" +
                        "</html>",
                otpCode
        );
    }
}
