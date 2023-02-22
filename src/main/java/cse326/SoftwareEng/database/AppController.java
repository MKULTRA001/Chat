package cse326.SoftwareEng.database;

import com.sun.net.httpserver.HttpContext;
import cse326.SoftwareEng.backEnd.HelloController;
import cse326.SoftwareEng.backEnd.TextMessage;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.HttpCookie;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Controller
@Import(HelloController.class)
public class AppController {

    private final UserRepository userRepo;

    public AppController(UserRepository userRepo) {
        this.userRepo = userRepo;
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
        userRepo.save(user);
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
}