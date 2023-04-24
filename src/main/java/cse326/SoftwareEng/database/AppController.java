package cse326.SoftwareEng.database;

import cse326.SoftwareEng.backEnd.HelloController;
import cse326.SoftwareEng.database.messageDB.*;
import cse326.SoftwareEng.database.userDB.User;
import cse326.SoftwareEng.database.userDB.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.repository.query.Param;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    private final UserChannelRepository userChannelRepository;
    public AppController(UserRepository userRepo, UserRepositoryMessageDB userRepositoryMessageDB, MessageRepository messageRepository, UserChannelRepository userChannelRepository) {
        this.userRepo = userRepo;
        this.userRepositoryMessageDB = userRepositoryMessageDB;
        this.messageRepository = messageRepository;
        this.userChannelRepository = userChannelRepository;
    }

    @Autowired
    private EmailService emailService;

    private Authentication getAuth(){
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public class Utility {
        public static String getSiteURL(HttpServletRequest request) {
            String siteURL = request.getRequestURL().toString();
            return siteURL.replace(request.getServletPath(), "");
        }
    }

    @RequestMapping("/")
    public String viewHomePage() {
        return "index";
    }

    @GetMapping("/signup")
    public String showRegistration(Model model) {
        //model.addAttribute("user", new User());
        if (!model.containsAttribute("user"))
            model.addAttribute("user", new User());
        return "signup";
    }

    @PostMapping("/signup")
    public String processRegister(User user, @RequestParam("confirm_password") String confirm_password,
                                  BindingResult result, RedirectAttributes redirectAttributes) {
        if(userRepo.findByUsername(user.getUsername()) != null)
            redirectAttributes.addFlashAttribute("error", "Username already taken.");
        else if(userRepo.findByEmail(user.getEmail()) != null)
            redirectAttributes.addFlashAttribute("error", "Email already in use.");
        else if(!user.getPassword().equals(confirm_password))
            redirectAttributes.addFlashAttribute("error", "Password does not match.");
        else{
            System.out.println("[Signup] Pass");
            redirectAttributes.addFlashAttribute("message", "Account has successfully been created.");
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
            UserChannel userChannel = new UserChannel(userMessageDB.getId(), "c17d1a10-c9db-443b-81aa-40d5156b9357");
            userChannelRepository.save(userChannel);
            return "redirect:/login";
        }
        redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.createAccountModel", result);
        redirectAttributes.addFlashAttribute("user", user);
        return "redirect:/signup";
    }
    @RequestMapping("/deleteAccount")
    public String deleteAccount(HttpServletRequest request, RedirectAttributes redirectAttributes){
        String userName = getAuth().getName();
        userRepo.deleteByUsername(userName);
       if(messageRepository.existsByUserId(userName))
           messageRepository.deleteUser(userName);
        userRepositoryMessageDB.deleteByUsername(userName);
        SecurityContextHolder.clearContext();
        new SecurityContextLogoutHandler().logout(request, null, null);
        redirectAttributes.addFlashAttribute("message", "Account has been deleted.");
        return "redirect:/signup";
    }

    @RequestMapping("/login")
    public String login() {
        if (getAuth() == null || AnonymousAuthenticationToken.class.
                isAssignableFrom(getAuth().getClass()))
            return "login";
        return "redirect:/chat_index";
    }

    @RequestMapping("/contacts")
    public String listUsers(Model model) {
        List<User> listUsers = userRepo.findAll();
        model.addAttribute("listUsers", listUsers);
        return "contacts";
    }

    @GetMapping("/profile")
    public String profile() {
        return "profile";
    }

    @PostMapping( "/changeUsername")
    public String changeUsername(@RequestParam("username") String username, Authentication authentication, Model model, RedirectAttributes redirectAttributes) {
        if(userRepo.findByUsername(username) == null){
            User user = userRepo.findByUsername(getAuth().getName());
            //user.setUsername(username);
            //userRepo.save(user);
            redirectAttributes.addFlashAttribute("message", "Username has been change.");
            System.out.println("[Username]"+username);
        }
        else
            redirectAttributes.addFlashAttribute("error", "Username is already taken.");
        return "redirect:/profile";
    }

    @PostMapping( "/changeEmail")
    public String  changeEmail(@RequestParam("email") String email,Authentication authentication,  Model model, RedirectAttributes redirectAttributes) {
        if(userRepo.findByEmail(email) == null){
            User user = userRepo.findByUsername(getAuth().getName());
            //user.setEmail(email);
            //userRepo.save(user);
            redirectAttributes.addFlashAttribute("message", "Email has been change.");
            System.out.println("Email has been change.");
        }
        else
            redirectAttributes.addFlashAttribute("error", "Email is already in use.");
        return "redirect:/profile";
    }

    @GetMapping("/security")
    public String security() {
        return "security";
    }

    @PostMapping( "/changePassword")
    public String  changePassword(@RequestParam("currentPassword") String currentPassword,
                                        @RequestParam("newPassword") String newPassword,
                                        @RequestParam("confirmPassword") String confirmPassword,
                                        Model model, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        Authentication auth = getAuth();
        User user = userRepo.findByUsername(auth.getName());
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        System.out.print("[Password]: " + confirmPassword);
        if(!passwordEncoder.matches(currentPassword, user.getPassword())) {
            redirectAttributes.addFlashAttribute("error", "The current password is incorrect.");
        }
        else if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "The new password and confirm password do not match.");
        }
        else if(passwordEncoder.matches(newPassword, user.getPassword())) {
            redirectAttributes.addFlashAttribute("error", "The new password cannot be the same as the current password.");
        }
        else {
            redirectAttributes.addFlashAttribute("success", "Your password has been updated successfully.");
            /*user.setPassword(passwordEncoder.encode(newPassword));
            Date javaDate = new Date();
            user.setUpdated_at(new Timestamp(javaDate.getTime()));
            userRepo.save(user);
            SecurityContextHolder.clearContext();*/
        }
        return "redirect:/security";
    }
    @PostMapping( "/changeVerification")
    public String change2FA(@RequestParam("2FA") boolean verification, Model model, RedirectAttributes redirectAttributes) {
        User user = userRepo.findByUsername(getAuth().getName());
        //user.setVerification(verification);
        System.out.println("[2FA]" + verification);
        if(verification)
            redirectAttributes.addFlashAttribute("message", "2FA has been enable.");
        else
            redirectAttributes.addFlashAttribute("message", "2FA has been disable.");
        return "redirect:/security";
    }
    /*
    @RequestMapping("/updatePassword")
    public String updatePassword(@RequestParam("currentPassword") String currentPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 Model model, HttpServletRequest request) {

        Authentication auth = getAuth();
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
    }*/

    @GetMapping("/preference")
    public String preference() {
        return "preference";
    }

    @GetMapping("/forgot_password")
    public String showForgotPasswordForm() {
        return "forgot_password";
    }

    @PostMapping("/forgot_password")
    public String processForgotPassword(HttpServletRequest request, Model model) {
        String email = request.getParameter("email");
        System.out.println("[Forgot] email:"+email);
        User user = userRepo.findByEmail(email);
        if (user == null)
            model.addAttribute("error", "Email not found");
        else{
            Random rand = new Random();
            int code = rand.nextInt(1000000);
            Timestamp timestamp = new Timestamp(new Date().getTime());
            user.setCode_timestamp(timestamp);
            user.setVerificationCode(code);
            userRepo.save(user);
            emailService.sendRest(user, Utility.getSiteURL(request));
            model.addAttribute("message", "Check your email for a reset link.");
        }
        return "forgot_password";
    }

     @GetMapping("/reset_password")
    public String showResetPasswordForm(@Param(value = "email") String email, @Param(value = "code") String code,
                                        HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        User user = userRepo.findByEmail(email);
        model.addAttribute("email", email);
        model.addAttribute("code", code);
        Date date = new Date();
        Timestamp timestamp = new Timestamp(date.getTime());
        System.out.println("Get "+email);
            if (user == null
                    || Integer.parseInt(code) != user.getVerificationCode()
                    || timestamp.after(new Timestamp(user.getCode_timestamp().getTime()+ (300 * 1000L)))) {
             redirectAttributes.addFlashAttribute("error", "Invalid link. Request password reset again.");
             return "redirect:/forgot_password";
            }
        return "reset_password";
     }

    @PostMapping("/reset_password")
    public String processResetPassword(@Param(value = "email") String email, @Param(value = "code") String code,
                                       @RequestParam("password") String password, @RequestParam("confirm_password") String confirm_password,
                                       HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        User user = userRepo.findByEmail(email);
        Date date = new Date();
        Timestamp timestamp = new Timestamp(date.getTime());
        if (user == null
                || Integer.parseInt(code) != user.getVerificationCode()
                || timestamp.after(new Timestamp(user.getCode_timestamp().getTime()+ (300 * 1000L)))) {
            model.addAttribute("error", "Invalid request. Request password reset again.");
            return "redirect:/forgot_password";
        }
        if(!password.equals(confirm_password)){
            redirectAttributes.addFlashAttribute("error", "Password does not match");
            redirectAttributes.addAttribute("email", email);
            redirectAttributes.addAttribute("code", code);
            return "redirect:/reset_password";
        }
        Date javaDate = new Date();
        user.setUpdated_at(new Timestamp(javaDate.getTime()));
        user.setPassword(new BCryptPasswordEncoder().encode(password));
        userRepo.save(user);
        redirectAttributes.addFlashAttribute("message", "Password was successfully reset.");
        return "redirect:/login";
    }
}