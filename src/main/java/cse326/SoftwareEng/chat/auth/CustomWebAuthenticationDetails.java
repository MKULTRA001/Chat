package cse326.SoftwareEng.chat.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
/* Adds verification code as a login parameter.
* Method to retrieve verification code.
* */
public class CustomWebAuthenticationDetails extends WebAuthenticationDetails {

    private String verificationCode;

    public CustomWebAuthenticationDetails(HttpServletRequest request) {
        super(request);
        verificationCode = request.getParameter("code");
    }
    public String getVerificationCode(){
        return verificationCode;
    }
}