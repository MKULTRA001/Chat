package cse326.SoftwareEng.chat;

import cse326.SoftwareEng.chat.user.User;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

//Email service interface
@Component
public class EmailService {
    private JavaMailSender javaMailSender;

    @Autowired
    public EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    // Sends email
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

    // Email 2FA code
    public void sendCode(User user){
        String text = "<p>Hello " + user.getUsername() + ",</p>"
                + "<p>Your verification code is"
                + "<p><b>" + user.getVerificationCode() + "</b></p>"
                + "<p>Note: this verification code is set to expire in 5 minutes.</p>";
        sendMail(user.getEmail(), "Chat Verification Code", text);
    }

    // Email reset password link
    public void sendRest(User user, String URL){
        String text = "<p>Hello " + user.getUsername() + ",</p>"
                + "<p>Your password reset link is"
                + "<p><b>" + URL + "/reset_password?email=" + user.getEmail() + "&code=" + user.getVerificationCode() + "</b></p>"
                + "<p>Note: this verification code is set to expire in 5 minutes.</p>";
        sendMail(user.getEmail(), "Chat Verification Code", text);
    }
}
