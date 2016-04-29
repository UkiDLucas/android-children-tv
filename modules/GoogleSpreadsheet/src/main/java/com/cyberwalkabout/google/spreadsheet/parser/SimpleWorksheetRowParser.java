package com.cyberwalkabout.google.spreadsheet.parser;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrii Kovalov
 *         <p/>
 *         TODO: seems to be not needed anymore
 */
public class SimpleWorksheetRowParser implements WorksheetRowParser {

    public static final int UNKNOWN = -1;

    @Override
    public Map<String, String> parse(String rawContent) {
        if (rawContent == null || rawContent.length() == 0) {
            return Collections.emptyMap();
        } else {
            Map<String, String> data = new HashMap<String, String>();

            int startIndex = 0;

            while (startIndex < rawContent.length()) {
                int index = rawContent.indexOf(':', startIndex);

                if (index != UNKNOWN) {
                    String key = rawContent.substring(startIndex, index).trim();

                    int endIndex = rawContent.indexOf(',', index);

                    if (endIndex == UNKNOWN) {
                        endIndex = rawContent.length() - 1;
                    }

                    String value = rawContent.substring(index + 1, endIndex).trim();

                    startIndex = endIndex + 1;

                    data.put(key, value);
                } else {
                    break;
                }
            }

            return data;
        }
    }

    public static void main(String[] args) {
        String rawContent1 = "seriesid: 142, language: en, id: 2308, title: A Special Valentine with The Family Circus, agegroup: 6+, duration: 7:43:00, description: A Special Valentine with The Family Circus - Part 1 , rating: 5, youtubeid: iPmrYDUrp3c, youtubevideolink: https://www.youtube.com/watch?v=iPmrYDUrp3c&amp;list=PLw9NMDPZ4YHr7dVdSOqLJIo_7kar2J9Wg&amp;index=8";
        String rawContent2 = "language: en, id: 2307, title: Peep and the Big Wide World: Bringing Spring , agegroup: 6+, duration: 8:50:00, description: Peep and the Big Wide World: Bringing Spring , rating: 5, youtubeid: evclcDtRQ-Y, youtubevideolink: https://www.youtube.com/watch?v=evclcDtRQ-Y&list=PLw9NMDPZ4YHr7dVdSOqLJIo_7kar2J9Wg&index=4";

        System.out.println("Parse content: " + rawContent2);

        WorksheetRowParser rowParser = new SimpleWorksheetRowParser();
        Map<String, String> data = rowParser.parse(rawContent2);

        System.out.println("Data");
        for (Map.Entry<String, String> entry : data.entrySet()) {
            System.out.println(entry);
        }
    }
}
