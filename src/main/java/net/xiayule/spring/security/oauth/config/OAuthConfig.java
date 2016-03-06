package net.xiayule.spring.security.oauth.config;

import net.xiayule.spring.security.oauth.filter.CustomOauthConsumerContextFilter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Scope;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.oauth.common.signature.SharedConsumerSecretImpl;
import org.springframework.security.oauth.config.ProtectedResourceDetailsServiceFactoryBean;
import org.springframework.security.oauth.consumer.*;
import org.springframework.security.oauth.consumer.client.CoreOAuthConsumerSupport;
import org.springframework.security.oauth.consumer.client.OAuthRestTemplate;
import org.springframework.security.oauth.consumer.filter.OAuthConsumerContextFilter;
import org.springframework.security.oauth.consumer.filter.OAuthConsumerProcessingFilter;
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.AccessTokenRequest;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.common.AuthenticationScheme;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.web.access.intercept.DefaultFilterInvocationSecurityMetadataSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.annotation.Resource;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLStreamHandler;
import java.util.*;

import static org.springframework.context.annotation.ScopedProxyMode.INTERFACES;

/**
 * Created by tan on 16-2-18.
 */
@Configuration
@EnableOAuth2Client
public class OAuthConfig {

    @Resource(name = "accessTokenRequest")
    private AccessTokenRequest accessTokenRequest;

    @Bean
    @Scope(value = "session", proxyMode = INTERFACES)
    public OAuth2ClientContext githubOauth2ClientContext() {
        return new DefaultOAuth2ClientContext(accessTokenRequest);
    }

    @Bean
    @Scope(value = "session", proxyMode = INTERFACES)
    public OAuth2ClientContext bitbucketOauth2ClientContext() {
        return new DefaultOAuth2ClientContext(accessTokenRequest);
    }

    @Bean
    @Scope(value = "session", proxyMode = INTERFACES)
    public OAuth2ClientContext sparklrOauth2ClientContext() {
        return new DefaultOAuth2ClientContext(accessTokenRequest);
    }

    @Bean
    public OAuth2ProtectedResourceDetails bitbucket() {
        AuthorizationCodeResourceDetails details = new AuthorizationCodeResourceDetails();
        details.setId("bitbucket");
        details.setClientId("zHDJGt9HVVLcDqrTV8");
        details.setClientSecret("DJyaKtQHGvYhKqFKrfnCjUR6HVQzAjhL");
        details.setAccessTokenUri("https://bitbucket.org/site/oauth2/access_token");
        details.setUserAuthorizationUri("https://bitbucket.org/site/oauth2/authorize");
        details.setScope(Arrays.asList("repository", "email", "account"));
        details.setClientAuthenticationScheme(AuthenticationScheme.query);
        details.setAuthenticationScheme(AuthenticationScheme.query);
        return details;
    }


    @Bean
    public OAuth2ProtectedResourceDetails github() {
        AuthorizationCodeResourceDetails details = new AuthorizationCodeResourceDetails();
        details.setId("github");
        details.setClientId("5412450300e37df625fa");
        details.setClientSecret("21e40331f2dbf36d6d6ac98dedd823d3719b3e04");
        details.setAccessTokenUri("https://github.com/login/oauth/access_token");
        details.setUserAuthorizationUri("https://github.com/login/oauth/authorize");
        details.setScope(Arrays.asList("repo", "user:email", "write:public_key", "read:public_key"));
        details.setClientAuthenticationScheme(AuthenticationScheme.form);
        return details;
    }

    @Bean
    public OAuth2ProtectedResourceDetails sparklr() {
        AuthorizationCodeResourceDetails details = new AuthorizationCodeResourceDetails();
        details.setId("sparklr/tonr");
        details.setClientId("tonr");
        details.setClientSecret("secret");
        details.setAccessTokenUri("http://localhost:8080/sparklr2/oauth/token");
        details.setUserAuthorizationUri("http://localhost:8080/sparklr2/oauth/authorize");
        details.setScope(Arrays.asList("read", "write"));
        return details;
    }

    private List<HttpMessageConverter<?>> messageConverters() {
        List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
        messageConverters.add(new StringHttpMessageConverter());
        messageConverters.add(new GsonHttpMessageConverter());
        return messageConverters;
    }

    @Bean
    public OAuth2RestOperations githubRestTemplate(@Qualifier("githubOauth2ClientContext") OAuth2ClientContext clientContext) {
        return new OAuth2RestTemplate(github(), clientContext);
    }

    @Bean
    public OAuth2RestOperations bitbucketRestTemplate(@Qualifier("bitbucketOauth2ClientContext") OAuth2ClientContext clientContext) {
        OAuth2RestTemplate template = new OAuth2RestTemplate(bitbucket(), clientContext);
        template.setMessageConverters(messageConverters());
        return template;
    }

    @Bean
    public OAuth2RestTemplate sparklrRestTemplate(@Qualifier("sparklrOauth2ClientContext") OAuth2ClientContext clientContext) {
        return new OAuth2RestTemplate(sparklr(), clientContext);
    }


    // oauth 1.0 for bitbucket begin

    @Bean
    @DependsOn(value = "bitbucket1")
    public ProtectedResourceDetailsServiceFactoryBean protectedResourceDetailsService() {
        return new ProtectedResourceDetailsServiceFactoryBean();
    }

    @Bean
    public OAuthConsumerSupport oAuthConsumerSupport(ProtectedResourceDetailsService protectedResourceDetailsService){
        CoreOAuthConsumerSupport support = new CoreOAuthConsumerSupport() {
            // 为了兼容 bitbucket: post 时参数也放在 path 上
            protected URL configureURLForProtectedAccess(URL url, OAuthConsumerToken requestToken, ProtectedResourceDetails details, String httpMethod, Map<String, String> additionalParameters) {
                StringBuilder fileb = new StringBuilder(url.getPath());
                String queryString = getOAuthQueryString(details, requestToken, url, httpMethod, additionalParameters);
                queryString += "&oauth_callback=http://localhost:8087/bitbucket1/link";
                fileb.append('?').append(queryString);
                String file = fileb.toString();

                try {
                    if ("http".equalsIgnoreCase(url.getProtocol())) {
                        URLStreamHandler streamHandler = getStreamHandlerFactory().getHttpStreamHandler(details, requestToken, this, httpMethod, additionalParameters);
                        return new URL(url.getProtocol(), url.getHost(), url.getPort(), file, streamHandler);
                    }
                    else if ("https".equalsIgnoreCase(url.getProtocol())) {
                        URLStreamHandler streamHandler = getStreamHandlerFactory().getHttpsStreamHandler(details, requestToken, this, httpMethod, additionalParameters);
                        return new URL(url.getProtocol(), url.getHost(), url.getPort(), file, streamHandler);
                    }
                    else {
                        throw new OAuthRequestFailedException("Unsupported OAuth protocol: " + url.getProtocol());
                    }
                }
                catch (MalformedURLException e) {
                    throw new IllegalStateException(e);
                }
            }
        };
        support.setProtectedResourceDetailsService(protectedResourceDetailsService);

        return support;
    }

    @Bean
    public ProtectedResourceDetails bitbucket1() {
        BaseProtectedResourceDetails details = new BaseProtectedResourceDetails();
        details.setId("bitbucket1");
        details.setConsumerKey("TPLYSg7SKeNszrfKYP");
        details.setSharedSecret(new SharedConsumerSecretImpl("Xqhb56YW65fde77XWMutF2PPvg43aGp5"));
        details.setRequestTokenURL("https://bitbucket.org/api/1.0/oauth/request_token");
        details.setUserAuthorizationURL("https://bitbucket.org/api/1.0/oauth/authenticate");
        details.setAccessTokenURL("https://bitbucket.org/api/1.0/oauth/access_token");
        return details;
    }

    @Bean
    public OAuthRestTemplate bitbucketOAuth1RestTemplate(OAuthConsumerSupport oAuthConsumerSupport){
        OAuthRestTemplate oAuthRestTemplate = new OAuthRestTemplate(new HttpComponentsClientHttpRequestFactory(),
                bitbucket1());
        oAuthRestTemplate.setMessageConverters(messageConverters());
        oAuthRestTemplate.setSupport(oAuthConsumerSupport);

        return oAuthRestTemplate;
    }

    @Bean
    public OAuthConsumerContextFilter oauthConsumerContextFilter(OAuthConsumerSupport oAuthConsumerSupport) {

        OAuthConsumerContextFilter filter = new CustomOauthConsumerContextFilter();

        filter.setConsumerSupport(oAuthConsumerSupport);

        return filter;
    }

    @Bean
    @DependsOn(value = "oauthConsumerContextFilter")
    public OAuthConsumerProcessingFilter oauthConsumerFilter(ProtectedResourceDetailsService protectedResourceDetailsService) {
        OAuthConsumerProcessingFilter filter = new OAuthConsumerProcessingFilter();

        filter.setProtectedResourceDetailsService(protectedResourceDetailsService);

        LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>> requestMap =
                new LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>>();

        List<ConfigAttribute> attributes = SecurityConfig.createListFromCommaDelimitedString("bitbucket1");

        RequestMatcher matcher = new AntPathRequestMatcher("/bitbucket1/**");

        requestMap.put(matcher, attributes);

        filter.setObjectDefinitionSource(new DefaultFilterInvocationSecurityMetadataSource(requestMap));

        return filter;
    }

    // oauth 1.0 for bitbucket end
}
