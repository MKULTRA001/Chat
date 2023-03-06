package cse326.SoftwareEng.database;

import cse326.SoftwareEng.backEnd.HelloController;
import cse326.SoftwareEng.backEnd.HelloSocketConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;

@Configuration
@Import({HelloController.class})
@EnableWebSocketMessageBroker
public class WebSecurityConfig{
    @Bean
    public HelloSocketConfig helloSocketConfig() {
        return new HelloSocketConfig();
    }
    @Bean
    public UserDetailsService userDetailsService() {
        return new CustomUserDetailsService();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        //DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        CustomAuthenticationProvider authProvider = new CustomAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
    @Autowired
    private CustomWebAuthenticationDetailsSource authenticationDetailsSource;
    @Autowired
    private LoginFailureHandler loginFailureHandler;
    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeHttpRequests(requests -> {
                try {
                    requests
                    .requestMatchers("/users", "/myUsername", "/chat_index", "/chat", "/chat-websocket",
                            "/app", "/name", "/ChangePassword", "/updatePassword").authenticated()
                    .anyRequest().permitAll()
                    .and()
                    .formLogin()
                    .authenticationDetailsSource(authenticationDetailsSource)
                    .usernameParameter("username")
                    .loginPage("/login")
                    .defaultSuccessUrl("/users", true)
                    .failureHandler(loginFailureHandler)//.failureUrl("/login?error=true")
                    .permitAll()
                    .and()
                    .logout().logoutSuccessUrl("/")
                    .deleteCookies("JSESSIONID")
                    .and()
                    .exceptionHandling().accessDeniedPage("/403");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        return http.build();
    }
}