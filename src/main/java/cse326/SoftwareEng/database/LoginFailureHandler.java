package cse326.SoftwareEng.database;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    /* If the login error is related to 2FA. It will redirect with verify set to true */
    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response, AuthenticationException exception)
            throws IOException, ServletException {

        String redirectURL = "/login?error";
        if (exception.getMessage().contains("Incorrect Verification Code") )
            redirectURL = "/login?error=true&verify=true";
        if (exception.getMessage().contains("Verification is required"))
            redirectURL = "/login?verify=true";

        super.setDefaultFailureUrl(redirectURL);
        super.onAuthenticationFailure(request, response, exception);
    }

}