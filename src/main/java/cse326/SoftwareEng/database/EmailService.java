package cse326.SoftwareEng.database;

import cse326.SoftwareEng.database.userDB.User;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class EmailService {
    private JavaMailSender javaMailSender;

    @Autowired
    public EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void sendMail(String to, String subject, String text) {
        try{
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom("chat@cs.nmt.edu");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, true);
            javaMailSender.send(message);
        }
        catch (Exception e) {
            System.out.println("[EmailService] Error while Sending Mail to "+to+" "+e);
        }
    }

    public void sendCode(User user, int code){
        String text = "<p>Hello " + user.getUsername() + ",</p>"
                + "<p>Your verification code is"
                + "<p><b>" + code + "</b></p>"
                + "<br>"
                + "<p>Note: this verification code is set to expire in 5 minutes.</p>";
            sendMail(user.getEmail(), "Chat Verification Code", text);
    }
}
