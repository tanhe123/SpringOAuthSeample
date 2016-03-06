package net.xiayule.spring.security.oauth.config;

import net.xiayule.spring.security.oauth.filter.CustomOauthConsumerContextFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth.consumer.filter.OAuthConsumerContextFilter;
import org.springframework.security.oauth.consumer.filter.OAuthConsumerProcessingFilter;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

/**
 * Created by tan on 16-2-18.
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private OAuthConsumerContextFilter oauthConsumerContextFilter;

    @Autowired
    private OAuthConsumerProcessingFilter oauthConsumerFilter;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        http.authorizeRequests()
                .antMatchers("/sparklr/**").hasRole("USER")
                .antMatchers("/bitbucket1/**").hasRole("USER")
                .anyRequest().permitAll()
                .and()
            .logout()
                .logoutSuccessUrl("/login")
                .permitAll()
                .and()
            .csrf().disable()
                .formLogin()
                .loginProcessingUrl("/login")
                .loginPage("/login")
                .failureUrl("/login?authentication_error=true")
                .permitAll();

        // for oauth 1.0
        http.addFilterAfter(oauthConsumerContextFilter, AnonymousAuthenticationFilter.class);
        http.addFilterAfter(oauthConsumerFilter, CustomOauthConsumerContextFilter.class);

        // @formatter:on
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication().withUser("username")
                .password("password")
                .roles("USER");
    }
}
