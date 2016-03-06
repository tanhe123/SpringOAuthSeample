package net.xiayule.spring.security.oauth.service;

import org.apache.commons.collections.map.HashedMap;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import javax.annotation.Resource;
import java.util.Map;

/**
 * Created by tan on 16-2-19.
 */
@Service
public class GithubServiceImpl implements OAuthService {

    private static final String GITHUB_BASE_URL = "https://api.github.com/";

    @Resource(name = "githubRestTemplate")
    private OAuth2RestOperations githubRestTemplate;

    private String gitUrl(String path) {
        return GITHUB_BASE_URL + path;
    }

    public String projects() {
        Map<String, String> uriVariables = new HashedMap();
        uriVariables.put("type", "all");

        String repos = githubRestTemplate.getForObject(gitUrl("user/repos?type={type}"),
                String.class,
                uriVariables);

        return repos;
    }


    public void link() {
        try {
            githubRestTemplate.getForObject(gitUrl("user/emails"), String.class);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                githubRestTemplate.getOAuth2ClientContext().setAccessToken(null);
                githubRestTemplate.getAccessToken();

                System.out.println("get access token success");
            } else {
                throw e;
            }
        }
    }

    @Override
    public void unlink() {
        githubRestTemplate.getOAuth2ClientContext().setAccessToken(null);
    }
}
