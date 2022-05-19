import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class SSLGmailSender {

    private String username;
    private String password;
    private Properties props;

    public SSLGmailSender(String username, String password) {

        this.username = username;
        this.password = password;

        props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");

    }

    public String send(String subject, String text, String path, String toEmail) throws MessagingException, IOException {
        Session session = Session.getDefaultInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

            Message message = new MimeMessage(session);

//          от кого
            message.setFrom(new InternetAddress(username));

//          кому
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));

//          тема сообщения
            message.setSubject(subject);

//          содержимое сообщения
            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText(text);

//          если есть вложение, добавляем его к письму
            if (!path.equals("")) {
                MimeBodyPart attachmentPart = new MimeBodyPart();
                attachmentPart.attachFile(new File(path));
                Multipart multipart = new MimeMultipart();
                multipart.addBodyPart(messageBodyPart);
                multipart.addBodyPart(attachmentPart);
                message.setContent(multipart);
            } else {
                message.setText(text);
            }

            //отправляем сообщение
            try {
                Transport.send(message);
            } catch (SendFailedException e){
                System.out.println("SenderSSL ERROR: " + e.getMessage());
                return "ERROR";
            }
            return "SUCCESS";
    }
}