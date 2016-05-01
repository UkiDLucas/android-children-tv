package com.cyberwalkabout.google.spreadsheet;

import com.cyberwalkabout.google.spreadsheet.parser.SpreadsheetInfoParser;
import com.cyberwalkabout.google.spreadsheet.parser.WorksheetParser;
import com.cyberwalkabout.google.spreadsheet.parser.WorksheetRowParser;
import com.cyberwalkabout.google.spreadsheet.parser.listener.SpreadsheetParserCallbacks;
import com.cyberwalkabout.google.spreadsheet.parser.listener.WorksheetParserCallbacks;
import com.cyberwalkabout.google.spreadsheet.url.UrlBuilder;
import com.cyberwalkabout.google.spreadsheet.url.UrlType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author Andrii Kovalov
 */
public class GoogleSpreadSheetClient {
    private static final Logger LOG = LoggerFactory.getLogger(GoogleSpreadSheetClient.class);

    private WorksheetRowParser worksheetRowParser;

    public GoogleSpreadSheetClient() {
    }

    public void setWorksheetRowParser(WorksheetRowParser worksheetRowParser) {
        this.worksheetRowParser = worksheetRowParser;
    }

    public void loadSpreadsheet(String spreadsheetKey, SpreadsheetParserCallbacks callbacks) {
        String url = UrlBuilder.newBuilder().urlType(UrlType.SPREADSHEET).spreadsheetKey(spreadsheetKey).build();

        LOG.info("Load spreadsheet form: " + url);

        try {
            URLConnection connection = new URL(url).openConnection();
            InputStream in = connection.getInputStream();

            try {
                SpreadsheetInfoParser parser = new SpreadsheetInfoParser(callbacks);
                parser.parse(in);
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException ignore) {
                    }
                }
            }
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public void loadWorksheet(String spreadsheetKey, String worksheetKey, WorksheetParserCallbacks callbacks) {
        String url = UrlBuilder.newBuilder().urlType(UrlType.WORKSHEET).spreadsheetKey(spreadsheetKey).worksheetKey(worksheetKey).build();

        LOG.info("Load worksheet form: " + url);

        try {
            URLConnection connection = new URL(url).openConnection();
            InputStream in = connection.getInputStream();

            try {
                WorksheetParser parser = new WorksheetParser(callbacks, worksheetRowParser);
                parser.parse(in);
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException ignore) {
                    }
                }
            }
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }
}
