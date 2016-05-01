package com.cyberwalkabout.google.spreadsheet.parser.sax;

import com.cyberwalkabout.google.spreadsheet.model.SpreadsheetInfo;
import com.cyberwalkabout.google.spreadsheet.model.Worksheet;
import com.cyberwalkabout.google.spreadsheet.parser.listener.SpreadsheetParserCallbacks;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrii Kovalov
 *         <p/>
 *         TODO: handle pagination
 *         <openSearch:totalResults>1</openSearch:totalResults>
 *         <openSearch:startIndex>1</openSearch:startIndex>
 */
public class SpreadsheetInfoHandler extends DefaultHandler {
    private static final String TAG_FEED = "feed";
    private static final String TAG_AUTHOR = "author";
    private static final String TAG_ID = "id";
    private static final String TAG_TITLE = "title";
    private static final String TAG_ENTRY = "entry";
    private static final String TAG_UPDATED = "updated";
    private static final String TAG_NAME = "name";
    private static final String TAG_EMAIL = "email";
    private static final String TAG_COLUMN_COUNT = "gs:colCount";
    private static final String TAG_ROW_COUNT = "gs:rowCount";

    private boolean inAuthorTag;
    private boolean inEntryTag;
    private String currentTag = "";

    private SpreadsheetInfo spreadsheetInfo;
    private List<Worksheet> worksheetList;
    private Worksheet worksheet;

    private SpreadsheetParserCallbacks callbacks;

    public void setCallbacks(SpreadsheetParserCallbacks callbacks) {
        this.callbacks = callbacks;
    }

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
        spreadsheetInfo = new SpreadsheetInfo();
        worksheetList = new ArrayList<Worksheet>();
        spreadsheetInfo.setWorksheetList(worksheetList);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        currentTag = qName;

        if (TAG_AUTHOR.equals(qName)) {
            inAuthorTag = true;
        } else if (TAG_ENTRY.equals(qName)) {
            inEntryTag = true;
            worksheet = new Worksheet();
            worksheetList.add(worksheet);
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (inAuthorTag) {
            if (TAG_NAME.equals(currentTag)) {
                spreadsheetInfo.setAuthorName(String.valueOf(ch, start, length));
            } else if (TAG_EMAIL.equals(currentTag)) {
                spreadsheetInfo.setAuthorEmail(String.valueOf(ch, start, length));
            }
        } else if (inEntryTag) {
            if (TAG_ID.equals(currentTag)) {
                worksheet.setId(String.valueOf(ch, start, length));
            } else if (TAG_UPDATED.equals(currentTag)) {
                worksheet.setUpdated(String.valueOf(ch, start, length));
            } else if (TAG_TITLE.equals(currentTag)) {
                worksheet.setTitle(String.valueOf(ch, start, length));
            } else if (TAG_COLUMN_COUNT.equals(currentTag)) {
                worksheet.setColumnCount(Integer.parseInt(String.valueOf(ch, start, length)));
            } else if (TAG_ROW_COUNT.equals(currentTag)) {
                worksheet.setRowCount(Integer.parseInt(String.valueOf(ch, start, length)));
            }
        } else {
            if (TAG_ID.equals(currentTag)) {
                spreadsheetInfo.setId(String.valueOf(ch, start, length));
            } else if (TAG_UPDATED.equals(currentTag)) {
                spreadsheetInfo.setUpdated(String.valueOf(ch, start, length));
            } else if (TAG_TITLE.equals(currentTag)) {
                spreadsheetInfo.setTitle(String.valueOf(ch, start, length));
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (TAG_AUTHOR.equals(qName)) {
            inAuthorTag = false;
        } else if (TAG_ENTRY.equals(qName)) {
            if (callbacks != null) {
                callbacks.onWorksheet(worksheet);
            }

            inEntryTag = false;
        }
        currentTag = "";
    }

    @Override
    public void endDocument() throws SAXException {
        if (callbacks != null) {
            callbacks.onSpreadsheet(spreadsheetInfo);
        }
    }
}
