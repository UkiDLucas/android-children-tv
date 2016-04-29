package com.cyberwalkabout.google.spreadsheet.parser;

import com.cyberwalkabout.google.spreadsheet.parser.listener.SpreadsheetParserCallbacks;
import com.cyberwalkabout.google.spreadsheet.parser.sax.SpreadsheetInfoHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * @author Andrii Kovalov
 */
public class SpreadsheetInfoParser extends AbstractParser {
    public static final Logger LOG = LoggerFactory.getLogger(SpreadsheetInfoParser.class);

    public SpreadsheetInfoParser(SpreadsheetParserCallbacks callbacks) {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            reader = parser.getXMLReader();

            SpreadsheetInfoHandler handler = new SpreadsheetInfoHandler();
            handler.setCallbacks(callbacks);

            reader.setContentHandler(handler);
        } catch (ParserConfigurationException e) {
            LOG.error(e.getMessage(), e);
        } catch (SAXException e) {
            LOG.error(e.getMessage(), e);
        }
    }
}
