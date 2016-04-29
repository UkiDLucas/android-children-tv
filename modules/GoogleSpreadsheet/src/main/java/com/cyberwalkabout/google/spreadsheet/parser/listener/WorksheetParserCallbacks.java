package com.cyberwalkabout.google.spreadsheet.parser.listener;

import com.cyberwalkabout.google.spreadsheet.model.Worksheet;
import com.cyberwalkabout.google.spreadsheet.model.WorksheetRow;

/**
 * @author Andrii Kovalov
 */
public interface WorksheetParserCallbacks {

    void onWorksheet(Worksheet worksheet);

    void onWorksheetRow(WorksheetRow worksheetRow);

}
