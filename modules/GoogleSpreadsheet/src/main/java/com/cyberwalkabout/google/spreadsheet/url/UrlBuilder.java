package com.cyberwalkabout.google.spreadsheet.url;

/**
 * @author Andrii Kovalov
 */
public class UrlBuilder {
    // sample -
    // http://spreadsheets.google.com/feeds/worksheets/0ArW-5VtRGDl7dEw0bExiTUV4YUNBTlV1LXl4WWpPMXc/public/basic
    private static final String SPREADSHEET_URL_TEMPLATE = "http://spreadsheets.google.com/feeds/worksheets/%s/public/basic";

    // sample -
    // https://spreadsheets.google.com/feeds/list/0AtyKUBfX5e55dEJkbW5uLWtNVVl0QXBDcDItd1ctTXc/1/public/basic
    private static final String WORKSHEET_URL_TEMPLATE = "http://spreadsheets.google.com/feeds/list/%s/%s/public/full";

    // private static final String WORKSHEET_URL_TEMPLATE = "http://spreadsheets.google.com/feeds/list/%s/%s/public/basic";
    // https://spreadsheets.google.com/feeds/list/0Av4hFaTJuRZ6dFJWSmU2YjhOQjV1Vmg5bERUY2tRenc/1/public/full

    private UrlType urlType;
    private String spreadsheetKey;
    private String worksheetKey;

    public static UrlBuilder newBuilder() {
        return new UrlBuilder();
    }

    private UrlBuilder() {
    }

    public UrlBuilder spreadsheetKey(String spreadsheetKey) {
        this.spreadsheetKey = spreadsheetKey;
        return this;
    }

    public UrlBuilder worksheetKey(String worksheetKey) {
        this.worksheetKey = worksheetKey;
        return this;
    }

    public UrlBuilder urlType(UrlType urlType) {
        this.urlType = urlType;
        return this;
    }

    public String build() {
        if (urlType == null) {
            throw new IllegalArgumentException("url type isn't provided");
        }

        if (spreadsheetKey == null || spreadsheetKey.length() == 0) {
            throw new IllegalArgumentException("spreadsheet key isn't provided");
        }

        switch (urlType) {
            case SPREADSHEET:
                return String.format(SPREADSHEET_URL_TEMPLATE, spreadsheetKey);
            case WORKSHEET:

                if (worksheetKey == null || worksheetKey.length() == 0) {
                    throw new IllegalArgumentException("worksheet key isn't provided");
                }
                return String.format(WORKSHEET_URL_TEMPLATE, spreadsheetKey, worksheetKey);

            default:
                throw new IllegalArgumentException("Unsupported url type");
        }
    }
}
