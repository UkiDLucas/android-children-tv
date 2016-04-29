package com.cyberwalkabout.google.spreadsheet.parser.sax;

import com.cyberwalkabout.google.spreadsheet.model.Worksheet;
import com.cyberwalkabout.google.spreadsheet.model.WorksheetRow;
import com.cyberwalkabout.google.spreadsheet.parser.SimpleWorksheetRowParser;
import com.cyberwalkabout.google.spreadsheet.parser.WorksheetRowParser;
import com.cyberwalkabout.google.spreadsheet.parser.listener.WorksheetParserCallbacks;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Map;

/**
 * @author Andrii Kovalov
 */
public class WorksheetHandler extends DefaultHandler {
    private static final String TAG_ID = "id";
    private static final String TAG_TITLE = "title";
    private static final String TAG_ENTRY = "entry";
    private static final String TAG_CONTENT = "content";
    private static final String TAG_UPDATED = "updated";

    private static final String NAMESPACE_GSX = "gsx";

    private boolean inEntryTag;
    private String currentTag = "";

    private Worksheet worksheet = new Worksheet();

    private WorksheetRow currentWorksheetRow;
    private int currentEntryIndex = 0;

    private WorksheetRowParser worksheetRowParser;
    private WorksheetParserCallbacks callbacks;

    public void setCallbacks(WorksheetParserCallbacks callbacks) {
        this.callbacks = callbacks;
    }

    public void setWorksheetRowParser(WorksheetRowParser worksheetRowParser) {
        this.worksheetRowParser = worksheetRowParser;
    }

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();

        if (this.worksheetRowParser == null) {
            this.worksheetRowParser = new SimpleWorksheetRowParser();
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        currentTag = qName;
        if (TAG_ENTRY.equals(qName)) {
            inEntryTag = true;
            currentWorksheetRow = new WorksheetRow(currentEntryIndex++);
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (inEntryTag) {
            if (currentTag.startsWith(NAMESPACE_GSX)) {
                String fieldName = currentTag.substring(currentTag.indexOf(':') + 1, currentTag.length());

                Map<String, String> data = currentWorksheetRow.getData();
                if (data != null && data.containsKey(fieldName)) {
                    data.put(fieldName, String.valueOf(data.get(fieldName) + String.valueOf(ch, start, length)).trim());
                } else {
                    currentWorksheetRow.putInData(fieldName, String.valueOf(ch, start, length).trim());
                }

            } else {
                if (TAG_ID.equals(currentTag)) {
                    currentWorksheetRow.setId(String.valueOf(ch, start, length));
                } else if (TAG_UPDATED.equals(currentTag)) {
                    currentWorksheetRow.setUpdated(String.valueOf(ch, start, length));
                } else if (TAG_TITLE.equals(currentTag)) {
                    currentWorksheetRow.setTitle(String.valueOf(ch, start, length));
                } else if (TAG_CONTENT.equals(currentTag)) {
                    currentWorksheetRow.appendRawContent(String.valueOf(ch, start, length));
                }
            }
        } else {
            if (TAG_ID.equals(currentTag)) {
                worksheet.setId(String.valueOf(ch, start, length));
            } else if (TAG_UPDATED.equals(currentTag)) {
                worksheet.setUpdated(String.valueOf(ch, start, length));
            } else if (TAG_TITLE.equals(currentTag)) {
                worksheet.setTitle(String.valueOf(ch, start, length));
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (TAG_ENTRY.equals(qName)) {
            //currentWorksheetRow.setData(worksheetRowParser.parse(currentWorksheetRow.getContent()));

            if (callbacks != null) {
                callbacks.onWorksheetRow(currentWorksheetRow);
            }

            inEntryTag = false;
        }
        currentTag = "";
    }

    @Override
    public void endDocument() throws SAXException {
        if (callbacks != null) {
            callbacks.onWorksheet(worksheet);
        }
    }
}