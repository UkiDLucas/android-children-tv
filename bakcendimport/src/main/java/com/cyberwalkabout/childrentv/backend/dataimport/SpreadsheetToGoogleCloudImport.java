package com.cyberwalkabout.childrentv.backend.dataimport;

import com.cyberwalkabout.google.spreadsheet.GoogleSpreadSheetClient;
import com.cyberwalkabout.google.spreadsheet.model.Worksheet;
import com.cyberwalkabout.google.spreadsheet.model.WorksheetRow;
import com.cyberwalkabout.google.spreadsheet.parser.listener.WorksheetParserCallbacks;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.tools.remoteapi.RemoteApiInstaller;
import com.google.appengine.tools.remoteapi.RemoteApiOptions;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

public class SpreadsheetToGoogleCloudImport {

    private static final String CONFIG_PROPERTIES = "config.properties";
    private static final Map<String, String> FIELD_ALIASES = new HashMap<>();

    // TODO: duplicated from
    public static final String PARENT_TYPE = "YoutubeVideo";
    public static final String TYPE = "VideoEntity";

    public final static String TITLE = "title";
    public final static String DESCRIPTION = "description";
    public final static String AGE_GROUP = "age_group";
    public final static String SERIES_ID = "series_id";
    public final static String YOUTUBE_ID = "youtube_id";
    public final static String YOUTUBE_URL = "youtube_url";
    public final static String DURATION = "duration";
    public final static String RATING = "rating";
    public final static String LANGUAGE = "language";

    static {
        FIELD_ALIASES.put("title", TITLE);
        FIELD_ALIASES.put("description", DESCRIPTION);
        FIELD_ALIASES.put("agegroup", AGE_GROUP);
        FIELD_ALIASES.put("seriesid", SERIES_ID);
        FIELD_ALIASES.put("youtubeid", YOUTUBE_ID);
        FIELD_ALIASES.put("youtubevideolink", YOUTUBE_URL);
        FIELD_ALIASES.put("duration", DURATION);
        FIELD_ALIASES.put("rating", RATING);
        FIELD_ALIASES.put("language", LANGUAGE);
    }

    private String appId;
    private String spreadsheetKey;
    private String worksheetKey;

    private String email;
    private String password;

    private boolean initialised;


    public void importData() {
        if (!initialised) {
            init();
            initialised = true;
        }

        RemoteApiOptions options = new RemoteApiOptions().server(appId + ".appspot.com", 443).credentials(email, password);
        RemoteApiInstaller installer = new RemoteApiInstaller();
        try {
            installer.install(options);

            final DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

            //Transaction txn = ds.beginTransaction();
            try {
                final AtomicLong counter = new AtomicLong();

                GoogleSpreadSheetClient googleSpreadSheetClient = new GoogleSpreadSheetClient();
                googleSpreadSheetClient.loadWorksheet(spreadsheetKey, worksheetKey, new WorksheetParserCallbacks() {
                    @Override
                    public void onWorksheet(Worksheet worksheet) {

                    }

                    @Override
                    public void onWorksheetRow(WorksheetRow worksheetRow) {
                        Entity entity = toEntity(worksheetRow);

                        System.out.println("Insert: " + entity);
                        counter.incrementAndGet();

                        ds.put(entity);
                    }
                });

                System.out.println("Inserted " + counter.get() + " entities");

                //txn.commit();
            } finally {
                /*if (txn.isActive()) {
                    txn.rollback();
                }*/
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            installer.uninstall();
        }
    }

    private Entity toEntity(WorksheetRow row) {
        Key parentKey = KeyFactory.createKey(PARENT_TYPE, "all");
        Entity entity = new Entity(TYPE, parentKey);

        for (Map.Entry<String, String> entry : row.getData().entrySet()) {
            String key = entry.getKey();

            if (FIELD_ALIASES.containsKey(key)) {
                String alias = FIELD_ALIASES.get(key);
                if (isLongDataType(alias)) {
                    entity.setProperty(alias, Long.valueOf(entry.getValue()));
                } else {
                    entity.setProperty(alias, entry.getValue());
                }
//            } else {
//                System.err.println("Unknown data field '" + key + "'");
            }
        }

        return entity;
    }

    private boolean isLongDataType(String fieldName) {
        return SERIES_ID.equals(fieldName) || RATING.equals(fieldName);
    }

    private void init() {
        try {
            loadProperties();
            promptCredentials();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void loadProperties() throws FileNotFoundException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(CONFIG_PROPERTIES);

        Properties properties = new Properties();

        if (inputStream != null) {
            try {
                properties.load(inputStream);
                appId = properties.getProperty("appId");
                spreadsheetKey = properties.getProperty("spreadsheetKey");
                worksheetKey = properties.getProperty("worksheetKey");

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            throw new FileNotFoundException("property file '" + CONFIG_PROPERTIES + "' not found in the classpath");
        }
    }

    private void promptCredentials() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("email: ");
            email = br.readLine();
            System.out.print("password: ");
            password = br.readLine();
        } catch (IOException e) {
            System.err.println("Failed to read username/password!");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SpreadsheetToGoogleCloudImport importer = new SpreadsheetToGoogleCloudImport();
        importer.importData();
    }
}
