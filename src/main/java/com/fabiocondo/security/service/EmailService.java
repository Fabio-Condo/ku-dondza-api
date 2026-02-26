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

    /* =========================================================
       =============== ENVIO DE CÓDIGO OTP =====================
       ========================================================= */

    public void sendOtpCodeEmail(String email, String otpCode) throws MessagingException {
        Message message = createOtpEmail(email, otpCode);
        send(message);
    }

    private Message createOtpEmail(String email, String otpCode) throws MessagingException {
        Message message = new MimeMessage(getEmailSession());
        message.setFrom(new InternetAddress(FROM_EMAIL));
        message.setRecipients(TO, InternetAddress.parse(email, false));
        message.setRecipients(CC, InternetAddress.parse(CC_EMAIL, false));
        message.setSubject("Código de Verificação – Dikahub");
        message.setContent(buildOtpHtmlContent(otpCode), "text/html; charset=utf-8");
        message.setSentDate(new Date());
        message.saveChanges();
        return message;
    }

    private String buildOtpHtmlContent(String otpCode) {

        return String.format(
                "<!DOCTYPE html>" +
                        "<html lang='pt'>" +
                        "<head>" +
                        "<meta charset='UTF-8'>" +
                        "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                        "<style>" +

                        "body { margin:0; padding:0; background-color:#f4f6fb; font-family: Arial, Helvetica, sans-serif; }" +
                        ".wrapper { width:100%%; padding:40px 15px; }" +
                        ".container { max-width:600px; margin:0 auto; background:#ffffff; border-radius:14px; overflow:hidden; box-shadow:0 10px 30px rgba(0,0,0,0.08); }" +

                        ".header { background:#4361ee; padding:25px; text-align:center; }" +
                        ".header h1 { color:#ffffff; margin:0; font-size:22px; letter-spacing:0.5px; }" +

                        ".content { padding:30px; text-align:center; }" +
                        ".badge { display:inline-block; padding:6px 14px; background:#e6ecff; color:#4361ee; font-weight:bold; border-radius:20px; font-size:13px; margin-bottom:20px; }" +
                        ".title { font-size:20px; color:#2b2d42; margin-bottom:10px; }" +
                        ".text { font-size:15px; color:#555; line-height:1.6; }" +

                        ".otp-box { margin:25px 0; background:#f1f4ff; border:2px dashed #4361ee; padding:18px; border-radius:10px; font-size:32px; font-weight:bold; letter-spacing:4px; color:#4361ee; }" +

                        ".warning { font-size:13px; color:#888; margin-top:15px; }" +

                        ".footer { text-align:center; padding:20px; font-size:12px; color:#999; background:#fafbff; }" +

                        "@media (max-width:600px) { .content { padding:20px; } .otp-box { font-size:26px; } }" +

                        "</style>" +
                        "</head>" +

                        "<body>" +
                        "<div class='wrapper'>" +
                        "<div class='container'>" +

                        "<div class='header'>" +
                        "<h1>Dikahub</h1>" +
                        "</div>" +

                        "<div class='content'>" +

                        "<span class='badge'>VERIFICAÇÃO DE SEGURANÇA</span>" +

                        "<div class='title'>Confirme sua identidade</div>" +

                        "<p class='text'>Utilize o código abaixo para concluir o processo de verificação.</p>" +

                        "<div class='otp-box'>%s</div>" +

                        "<p class='warning'>Este código expira em 10 minutos.<br>Se você não solicitou este código, ignore este e-mail.</p>" +

                        "</div>" +

                        "<div class='footer'>" +
                        "© 2026 Dikahub • Educação Digital<br>" +
                        "www.dikahub.com<br>" +
                        "Este é um e-mail automático. Não responda." +
                        "</div>" +

                        "</div>" +
                        "</div>" +
                        "</body>" +
                        "</html>",

                otpCode
        );
    }

    /* =========================================================
       ========== CONFIRMAÇÃO DE PAGAMENTO =====================
       ========================================================= */

    public void sendPaymentConfirmationEmail(String email,
                                             String fullName,
                                             String amount,
                                             String method,
                                             String transactionId,
                                             String phoneNumber,
                                             String paymentDate) throws MessagingException {

        Message message = createPaymentEmail(email, fullName, amount, method, transactionId, phoneNumber, paymentDate);
        send(message);
    }

    private Message createPaymentEmail(String email,
                                       String fullName,
                                       String amount,
                                       String method,
                                       String transactionId,
                                       String phoneNumber,
                                       String paymentDate) throws MessagingException {

        Message message = new MimeMessage(getEmailSession());
        message.setFrom(new InternetAddress(FROM_EMAIL));
        message.setRecipients(TO, InternetAddress.parse(email, false));
        message.setSubject("Confirmação de Pagamento – Dikahub");
        message.setContent(buildPaymentHtmlContent(fullName, amount, method, transactionId, phoneNumber, paymentDate),
                "text/html; charset=utf-8");
        message.setSentDate(new Date());
        message.saveChanges();
        return message;
    }

    private String buildPaymentHtmlContent(String fullName,
                                           String amount,
                                           String method,
                                           String transactionId,
                                           String phoneNumber,
                                           String paymentDate) {

        String platformLink = "https://www.dikahub.com";

        return String.format(
                "<!DOCTYPE html>" +
                        "<html lang='pt'>" +
                        "<head>" +
                        "<meta charset='UTF-8'>" +
                        "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                        "<style>" +

                        "body { margin:0; padding:0; background-color:#f4f6fb; font-family: Arial, Helvetica, sans-serif; }" +
                        ".wrapper { width:100%%; padding:40px 15px; }" +
                        ".container { max-width:600px; margin:0 auto; background:#ffffff; border-radius:14px; overflow:hidden; box-shadow:0 10px 30px rgba(0,0,0,0.08); }" +

                        ".header { background:#4361ee; padding:25px; text-align:center; }" +
                        ".header h1 { color:#ffffff; margin:0; font-size:22px; letter-spacing:0.5px; }" +

                        ".content { padding:30px; }" +
                        ".content h2 { margin-top:0; color:#2b2d42; font-size:20px; }" +
                        ".content p { color:#555; font-size:15px; line-height:1.6; }" +

                        ".status-badge { display:inline-block; padding:6px 14px; background:#e6ecff; color:#4361ee; font-weight:bold; border-radius:20px; font-size:13px; margin-bottom:15px; }" +

                        ".details { background:#f8f9ff; border:1px solid #e0e7ff; padding:18px; border-radius:10px; margin-top:20px; }" +
                        ".details-row { margin-bottom:12px; }" +
                        ".label { color:#888; display:block; font-size:12px; margin-bottom:3px; }" +
                        ".value { color:#2b2d42; font-weight:bold; font-size:14px; }" +

                        ".button-container { text-align:center; margin-top:30px; }" +
                        ".btn { background:#4361ee; color:#ffffff !important; text-decoration:none; padding:14px 28px; border-radius:8px; font-weight:bold; font-size:14px; display:inline-block; box-shadow:0 6px 18px rgba(67,97,238,0.35); }" +

                        ".footer { text-align:center; padding:20px; font-size:12px; color:#999; background:#fafbff; }" +

                        "@media (max-width:600px) { .content { padding:20px; } }" +

                        "</style>" +
                        "</head>" +

                        "<body>" +
                        "<div class='wrapper'>" +
                        "<div class='container'>" +

                        "<div class='header'>" +
                        "<h1>Dikahub</h1>" +
                        "</div>" +

                        "<div class='content'>" +

                        "<span class='status-badge'>PAGAMENTO CONFIRMADO</span>" +

                        "<h2>Olá %s,</h2>" +

                        "<p>O seu pagamento foi processado com sucesso. Veja os detalhes da transação:</p>" +

                        "<div class='details'>" +

                        "<div class='details-row'>" +
                        "<span class='label'>Método de Pagamento</span>" +
                        "<span class='value'>%s</span>" +
                        "</div>" +

                        "<div class='details-row'>" +
                        "<span class='label'>Número Utilizado</span>" +
                        "<span class='value'>%s</span>" +
                        "</div>" +

                        "<div class='details-row'>" +
                        "<span class='label'>Valor Pago</span>" +
                        "<span class='value'>%s MZN</span>" +
                        "</div>" +

                        "<div class='details-row'>" +
                        "<span class='label'>Data da Transação</span>" +
                        "<span class='value'>%s</span>" +
                        "</div>" +

                        "<div class='details-row'>" +
                        "<span class='label'>ID da Transação</span>" +
                        "<span class='value'>%s</span>" +
                        "</div>" +

                        "</div>" +

                        "<div class='button-container'>" +
                        "<a href='" + platformLink + "' class='btn'>Acessar Plataforma</a>" +
                        "</div>" +

                        "<p style='margin-top:25px; font-size:13px; color:#888;'>Se não reconhece esta transação, contacte o suporte imediatamente.</p>" +

                        "</div>" +

                        "<div class='footer'>" +
                        "© 2026 Dikahub • Educação Digital<br>" +
                        "www.dikahub.com<br>" +
                        "Este é um e-mail automático. Não responda." +
                        "</div>" +

                        "</div>" +
                        "</div>" +
                        "</body>" +
                        "</html>",

                fullName,
                method,
                phoneNumber,
                amount,
                paymentDate,
                transactionId
        );
    }

    /* =========================================================
       ================= CONFIGURAÇÃO SMTP =====================
       ========================================================= */

    private void send(Message message) throws MessagingException {
        SMTPTransport smtpTransport =
                (SMTPTransport) getEmailSession().getTransport(SIMPLE_MAIL_TRANSFER_PROTOCOL);
        smtpTransport.connect(GMAIL_SMTP_SERVER, USERNAME, PASSWORD);
        smtpTransport.sendMessage(message, message.getAllRecipients());
        smtpTransport.close();
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
}