package net.xiayule.spring.security.oauth.controller;

import net.xiayule.spring.security.oauth.service.OAuthService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * Created by tan on 16-2-20.
 *
 */
@Controller
@RequestMapping("/bitbucket")
public class BitbucketController {

    @Resource(name = "bitbucketServiceImpl")
    private OAuthService bitbucketService;

    @RequestMapping("/projects")
    @ResponseBody
    public String projects() {
        return bitbucketService.projects();
    }

    @RequestMapping(value = "/link", method = GET)
    public String link(@RequestParam(required = false) String error) {
        if (error != null) {
            return "oauth-error";
        }

        bitbucketService.link();

        return "oauth-success";
    }

    @RequestMapping(value = "/unlink", method = GET)
    public String unlink() {
        bitbucketService.unlink();

        return "redirect:/";
    }
}
