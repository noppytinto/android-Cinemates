package mirror42.dev.cinemates.mailAPI;



import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MailSender {

    private Properties mailServerProperties;
    private Session getMailSession;
    private MimeMessage generateMailMessage;

    // email content param
    final private String REGARDS = "<br><br>Cordiali saluti, <br>Staff di Cinemates20<br>";
    final private String NO_REPLY_MEX_INFO = "<h4 style=\"color:Tomato;\">No reply message</h4>";
    final private String INTRODUCTION = "Ciao ";
    final private String SUBJECT = "CINEMATES20 (no reply)";


    public boolean sendAnEmail(String receiver, String body) throws EmailException {
        boolean isSent = false;

        if(receiver == null)
            throw new EmailException("Il campo mail non può essere vuoto");

        if(body == null || body.trim().isEmpty())
            throw new EmailException("Il campo body non può essere vuoto o null");

        try {
            setProperties();
            setSession(receiver, body);
            setAndGoTransport();
            isSent = true;
        }
        catch (Exception e) {
            e.getMessage();
            e.printStackTrace();
            throw new EmailException();
        }
        return isSent;
    }



    // private methods

    private void setProperties() {
        mailServerProperties = System.getProperties();
        mailServerProperties.put("mail.smtp.port", ConfigurationMailManager.getPort());
        mailServerProperties.put("mail.smtp.auth", "true");
        mailServerProperties.put("mail.smtp.starttls.enable", "true");
        mailServerProperties.put("mail.smtp.ssl.trust", ConfigurationMailManager.getHost());
    }

    private void setSession(String receiver, String body) throws AddressException, MessagingException {
        getMailSession = Session.getDefaultInstance(mailServerProperties, null);
        generateMailMessage = new MimeMessage(getMailSession);
        generateMailMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(receiver));
        generateMailMessage.setSubject(SUBJECT);
        String emailBody = INTRODUCTION + estractUsernameFromEmail(receiver)
                +"<br>" + body + REGARDS + NO_REPLY_MEX_INFO;
        generateMailMessage.setContent(emailBody,  "text/html");
    }

    private void setAndGoTransport() throws MessagingException, SendFailedException {

        Transport transport = getMailSession.getTransport("smtp");


        // Enter your correct gmail UserID and Password
        transport.connect(ConfigurationMailManager.getHost(), ConfigurationMailManager.getGmailId(), ConfigurationMailManager.getPassword());
        transport.sendMessage(generateMailMessage, generateMailMessage.getAllRecipients());
        transport.close();

    }

    private String estractUsernameFromEmail(String mail) {
        String username;
        try {
            username = mail.substring(0, mail.lastIndexOf("@"));
        } catch(Exception e ) {
            throw new StringIndexOutOfBoundsException("la mail non contiene @");
        }
        return username;
    }

}
