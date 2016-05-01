package com.cyberwalkabout.google.spreadsheet.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Andrii Kovalov
 */
public class AbstractParser implements Parser {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractParser.class);

    protected XMLReader reader;

    @Override
    public void parse(InputStream in) {
        try {
            reader.parse(new InputSource(in));
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        } catch (SAXException e) {
            LOG.error(e.getMessage(), e);
        }
    }
}
