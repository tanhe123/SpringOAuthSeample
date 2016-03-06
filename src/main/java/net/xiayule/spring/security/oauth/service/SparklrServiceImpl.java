package net.xiayule.spring.security.oauth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestOperations;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tan on 16-2-18.
 */
@Service
public class SparklrServiceImpl implements SparklrService {

    @Value("${sparklrPhotoListURL}")
    private String sparklrPhotoListURL;

    @Value("${sparklrPhotoURLPattern}")
    private String sparklrPhotoURLPattern;

    @Autowired
    private RestOperations sparklrRestTemplate;

    public List<String> getSparklrPhotoIds() throws Exception {
        try {
            InputStream photosXML = new ByteArrayInputStream(sparklrRestTemplate.getForObject(
                    URI.create(sparklrPhotoListURL), byte[].class));

            final List<String> photoIds = new ArrayList<String>();
            SAXParserFactory parserFactory = SAXParserFactory.newInstance();
            parserFactory.setValidating(false);
            parserFactory.setXIncludeAware(false);
            parserFactory.setNamespaceAware(false);
            SAXParser parser = parserFactory.newSAXParser();
            parser.parse(photosXML, new DefaultHandler() {
                @Override
                public void startElement(String uri, String localName, String qName, Attributes attributes)
                        throws SAXException {
                    if ("photo".equals(qName)) {
                        photoIds.add(attributes.getValue("id"));
                    }
                }
            });
            return photoIds;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } catch (SAXException e) {
            throw new IllegalStateException(e);
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException(e);
        }
    }

    public InputStream loadSparklrPhoto(String id) throws Exception {
        return new ByteArrayInputStream(sparklrRestTemplate.getForObject(
                URI.create(String.format(sparklrPhotoURLPattern, id)), byte[].class));
    }
}
