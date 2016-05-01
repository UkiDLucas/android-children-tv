package com.cyberwalkabout.google.spreadsheet.parser;

import java.util.Map;

/**
 * @author Andrii Kovalov
 */
public interface WorksheetRowParser {

    Map<String, String> parse(String rawContent);
}
