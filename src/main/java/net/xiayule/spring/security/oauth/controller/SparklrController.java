package net.xiayule.spring.security.oauth.controller;

import net.xiayule.spring.security.oauth.service.SparklrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by tan on 16-2-18.
 */
@Controller
public class SparklrController {

    @Autowired
    private SparklrService sparklrService;

    @RequestMapping("/sparklr/photos")
    public String photos(Model model) throws Exception {
        model.addAttribute("photoIds", sparklrService.getSparklrPhotoIds());
        return "sparklr";
    }

    @RequestMapping(value = "/sparklr/photos/{id}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<byte[]> photo(@PathVariable String id) throws Exception {
        InputStream photo = sparklrService.loadSparklrPhoto(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);

        return new ResponseEntity<byte[]>(toByteArray(photo), headers, HttpStatus.OK);
    }

    public static byte[] toByteArray(InputStream is) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            byte[] b = new byte[4096];
            int n = 0;
            while ((n = is.read(b)) != -1) {
                output.write(b, 0, n);
            }
            return output.toByteArray();
        } finally {
            output.close();
        }
    }
}
