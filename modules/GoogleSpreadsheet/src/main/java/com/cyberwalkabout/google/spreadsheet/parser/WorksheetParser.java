package com.cyberwalkabout.google.spreadsheet.parser;

import com.cyberwalkabout.google.spreadsheet.parser.listener.WorksheetParserCallbacks;
import com.cyberwalkabout.google.spreadsheet.parser.sax.WorksheetHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * @author Andrii Kovalov
 */
public class WorksheetParser extends AbstractParser {
    public static final Logger LOG = LoggerFactory.getLogger(WorksheetParser.class);

    public WorksheetParser(WorksheetParserCallbacks callbacks, WorksheetRowParser worksheetRowParser) {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            reader = parser.getXMLReader();

            WorksheetHandler handler = new WorksheetHandler();
            handler.setCallbacks(callbacks);
            handler.setWorksheetRowParser(worksheetRowParser);

            reader.setContentHandler(handler);
        } catch (ParserConfigurationException e) {
            LOG.error(e.getMessage(), e);
        } catch (SAXException e) {
            LOG.error(e.getMessage(), e);
        }
    }
}
