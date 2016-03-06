package net.xiayule.spring.security.oauth.service;

import com.google.gson.JsonObject;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import static java.lang.String.format;

/**
 * Created by tan on 16-2-20.
 */
@Service
public class BitbucketServiceImpl implements OAuthService {

    private static final String BITBUCKET_BASE_URL = "https://api.bitbucket.org/2.0/";

    @Resource(name = "bitbucketRestTemplate")
    private OAuth2RestOperations bitbucketRestTemplate;

    @Override
    public String projects() {
        JsonObject result = bitbucketRestTemplate.getForObject(BITBUCKET_BASE_URL
                + "user", JsonObject.class);

        String username = result.get("username").getAsString();

        String projects = bitbucketRestTemplate.getForObject(BITBUCKET_BASE_URL
                + format("repositories/%s?pagelen=100", username), String.class);

        return projects;
    }

    @Override
    public void link() {
        String user = bitbucketRestTemplate.getForObject(BITBUCKET_BASE_URL + "user", String.class);
        System.out.println(user);
    }

    public void unlink() {
        bitbucketRestTemplate.getOAuth2ClientContext().setAccessToken(null);
    }
}
