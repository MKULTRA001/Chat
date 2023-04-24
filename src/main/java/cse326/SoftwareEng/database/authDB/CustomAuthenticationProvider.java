package cse326.SoftwareEng.database.authDB;

import cse326.SoftwareEng.database.EmailService;
import cse326.SoftwareEng.database.userDB.User;
import cse326.SoftwareEng.database.userDB.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Random;
public class CustomAuthenticationProvider extends DaoAuthenticationProvider {
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private EmailService emailService;

    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication)
            throws AuthenticationException {
        super.additionalAuthenticationChecks(userDetails, authentication);

        User user = userRepo.findByUsername(userDetails.getUsername());
        if(user.getVerification()){
            //System.out.println("[2FA] "+ user.getUsername()+ " | "+ user.getCode_timestamp()+ " | "+user.getVerificationCode());
            Date date = new Date();
            Timestamp timestamp = new Timestamp(date.getTime());

            if(user.getCode_timestamp() == null
                    || timestamp.after(new Timestamp(user.getCode_timestamp().getTime()+ (300 * 1000L)))
                    || ((CustomWebAuthenticationDetails) authentication.getDetails()).getVerificationCode() == null
                    ){
                //System.out.println("[2FA] Generating Code");
                Random rand = new Random();
                int code = rand.nextInt(1000000);
                user.setCode_timestamp(timestamp);
                user.setVerificationCode(code);
                userRepo.save(user);
                emailService.sendCode(user);
                throw new InsufficientAuthenticationException("Verification is required");
            }
            else{
                //System.out.println("[2FA] Checking Code");
                int code = Integer.parseInt(((CustomWebAuthenticationDetails) authentication.getDetails()).getVerificationCode());
                if(code != user.getVerificationCode()){
                    //System.out.println("[2FA] Invalid Code");
                    throw new BadCredentialsException(messages.getMessage(
                            "AbstractUserDetailsAuthenticationProvider.badCredentials",
                            "Incorrect Verification Code"));
                }
            }
        }
    }
}


