package com.cyberwalkabout.google.spreadsheet.parser.listener;

import com.cyberwalkabout.google.spreadsheet.model.SpreadsheetInfo;
import com.cyberwalkabout.google.spreadsheet.model.Worksheet;

/**
 * @author Andrii Kovalov
 */
public interface SpreadsheetParserCallbacks {

    void onSpreadsheet(SpreadsheetInfo spreadsheetInfo);

    void onWorksheet(Worksheet worksheet);
}
