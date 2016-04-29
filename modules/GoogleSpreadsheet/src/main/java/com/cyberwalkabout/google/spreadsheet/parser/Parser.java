package com.cyberwalkabout.google.spreadsheet.parser;

import java.io.InputStream;

/**
 * @author Andrii Kovalov
 */
public interface Parser {
    void parse(InputStream in);
}
