package cse326.SoftwareEng.database;

import cse326.SoftwareEng.backEnd.HelloController;
import cse326.SoftwareEng.database.messageDB.MessageRepository;
import cse326.SoftwareEng.database.messageDB.UserMessageDB;
import cse326.SoftwareEng.database.messageDB.UserRepositoryMessageDB;
import cse326.SoftwareEng.database.userDB.User;
import cse326.SoftwareEng.database.userDB.UserRepository;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.repository.query.Param;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Random;

@Controller
@Import(HelloController.class)
public class AppController {

    private final UserRepository userRepo;
    private final UserRepositoryMessageDB userRepositoryMessageDB;
    private final MessageRepository messageRepository;
    public AppController(UserRepository userRepo, UserRepositoryMessageDB userRepositoryMessageDB, MessageRepository messageRepository) {
        this.userRepo = userRepo;
        this.userRepositoryMessageDB = userRepositoryMessageDB;
        this.messageRepository = messageRepository;
    }

    @RequestMapping("/")
    public String viewHomePage() {
        return "index";
    }

    @RequestMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "signup_form";
    }

    @RequestMapping("/process_register")
    public String processRegister(User user) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        Date javaDate = new Date();
        user.setCreatedAt(new Timestamp(javaDate.getTime()));
        user.setUpdated_at(new Timestamp(javaDate.getTime()));
        user.setVerificationCode(-1);
        user.setCode_timestamp(new Timestamp(javaDate.getTime()));
        userRepo.save(user);
        UserMessageDB userMessageDB = new UserMessageDB(user.getUsername());
        userRepositoryMessageDB.save(userMessageDB);
        return "register_success";
    }

    @RequestMapping("/users")
    public String listUsers(Model model) {
        List<User> listUsers = userRepo.findAll();
        model.addAttribute("listUsers", listUsers);
        return "users";
    }
    @RequestMapping("/deleteAccount")
    public String deleteAccount(HttpServletRequest request){
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        userRepo.deleteByUsername(userName);
       if(messageRepository.existsByUserId(userName)) {
           messageRepository.deleteUser(userName);
       }
        userRepositoryMessageDB.deleteByUsername(userName);
        SecurityContextHolder.clearContext();
        new SecurityContextLogoutHandler().logout(request, null, null);
        return "deleted_success";
    }

    @RequestMapping("/login")
    public String userLogin() {
        return "login";
    }
    @RequestMapping("/ChangePassword")
    public String ChangePassword() {
        return "ChangePassword";
    }
    @RequestMapping("/updatePassword")
    public String updatePassword(@RequestParam("currentPassword") String currentPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 Model model, HttpServletRequest request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepo.findByUsername(auth.getName());
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();


        if(!passwordEncoder.matches(currentPassword, user.getPassword())) {
            model.addAttribute("error", "The current password is incorrect.");
            return "ChangePassword";
        }
        else if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "The new password and confirm password do not match.");
            return "ChangePassword";
        }
        else if(passwordEncoder.matches(newPassword, user.getPassword())) {
            model.addAttribute("error", "The new password cannot be the same as the current password.");
            return "ChangePassword";
        }
        else {
            user.setPassword(passwordEncoder.encode(newPassword));
            Date javaDate = new Date();
            user.setUpdated_at(new Timestamp(javaDate.getTime()));
            userRepo.save(user);
            model.addAttribute("success", "Your password has been updated successfully.");
            SecurityContextHolder.clearContext();
            new SecurityContextLogoutHandler().logout(request, null, null);
        return "update_success";
        }
    }

    @GetMapping("/forgot_password")
    public String showForgotPasswordForm() {
        return "forgot_password_form";
    }

    @PostMapping("/forgot_password")
    public String processForgotPassword(HttpServletRequest request, Model model) {
        String email = request.getParameter("email");
        System.out.println("[Forgot] email:"+email);
        User user = userRepo.findByEmail(email);
        if (user == null) {
            model.addAttribute("error", "Bad Email");
        }
        else{
            Random rand = new Random();
            int code = rand.nextInt(1000000);
            Date date = new Date();
            Timestamp timestamp = new Timestamp(date.getTime());
            user.setCode_timestamp(timestamp);
            user.setVerificationCode(code);
            String message = Utility.getSiteURL(request) + "/reset_password?email=" + email + "&code=" + code;
            sendMail(email, "subject", message);
            model.addAttribute("message", "A reset password link has been sent to your email. Please check.");
            System.out.println("Reset Email sent");
        }
        return "forgot_password_form";
    }

    @GetMapping("/reset_password")
    public String showResetPasswordForm(@Param(value = "email") String email, @Param(value = "code") String code, Model model) {
        User user = userRepo.findByEmail(email);
        model.addAttribute("email", email);
        model.addAttribute("code", code);

        if (user == null || user.getVerificationCode() != Integer.parseInt(code)) {
            model.addAttribute("message", "Invalid email or code");
            model.addAttribute("title", "Reset your password");
            System.out.println("[Reset Checking] 1st "+code);
            return "message";
        }

        return "reset_password_form";
    }

    @PostMapping("/reset_password")
    public String processResetPassword(HttpServletRequest request, Model model) {
        String email = request.getParameter("email");
        String code = request.getParameter("code");
        String password = request.getParameter("password");

        User user = userRepo.findByEmail(email);
        model.addAttribute("title", "Reset your password");

        Date date = new Date();
        Timestamp timestamp = new Timestamp(date.getTime());

        if (user == null
                || Integer.parseInt(code) != user.getVerificationCode()
                || timestamp.after(new Timestamp(user.getCode_timestamp().getTime()+ (300 * 1000L)))) {
            model.addAttribute("message", "Invalid Requets");
            System.out.println("[reset_password] Invalid");
        }
        else {
            user.setPassword(password);
            System.out.println("[reset_password] Password set");

            model.addAttribute("message", "You have successfully changed your password.");
        }
        return "message";
    }
    public class Utility {
        public static String getSiteURL(HttpServletRequest request) {
            String siteURL = request.getRequestURL().toString();
            return siteURL.replace(request.getServletPath(), "");
        }
    }
    @Autowired
    private JavaMailSender javaMailSender;

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


}