package net.xiayule.spring.security.oauth.service;

import org.springframework.security.oauth.consumer.OAuthSecurityContextHolder;
import org.springframework.security.oauth.consumer.client.OAuthRestTemplate;
import org.springframework.security.oauth.consumer.rememberme.HttpSessionOAuthRememberMeServices;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by tan on 16-2-23.
 */
@Service
public class BitbucketOauth1ServiceImpl implements OAuthService {
    private static final String BITBUCKET_BASE_URL = "https://api.bitbucket.org/1.0/";

    @Resource(name = "bitbucketOAuth1RestTemplate")
    private OAuthRestTemplate bitbucketRestTemplate;

    @Override
    public String projects() {
        System.out.println(bitbucketRestTemplate.getForObject(BITBUCKET_BASE_URL + "/users/xiayule/ssh-keys", String.class));

        String projects = bitbucketRestTemplate.getForObject(BITBUCKET_BASE_URL + "user/repositories", String.class);
        return projects;
    }

    @Override
    public void link() {
        String user = bitbucketRestTemplate.getForObject(BITBUCKET_BASE_URL + "user", String.class);
        System.out.println(user);
    }

    public void unlink() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        // bitbucketRestTemplate 是从该方法获得的 access token
        OAuthSecurityContextHolder.setContext(null);

        request.setAttribute(HttpSessionOAuthRememberMeServices.REMEMBERED_TOKENS_KEY, null);
    }
}
