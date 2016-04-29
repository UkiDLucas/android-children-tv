package com.cyberwalkabout.common.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.cyberwalkabout.common.db.provider.DbProvider;
import com.cyberwalkabout.common.util.IoUtils;

import java.io.*;
import java.lang.ref.WeakReference;
import java.util.zip.GZIPOutputStream;

public class OpenHelperDbManager extends SimpleSQLiteOpenHelper implements DbManager {
    private static final String TAG = OpenHelperDbManager.class.getSimpleName();

    private String dbPath;
    private String dbName;
    private DbProvider dbProvider;
    private WeakReference<Context> ctxRef;

    public OpenHelperDbManager(Context context, String packageName, String dbName, int dbVersion) {
        this(context, packageName, dbName, dbVersion, null);
    }

    public OpenHelperDbManager(Context context, String packageName, String dbName, int dbVersion, DbProvider dbProvider) {
        super(context, dbName, null, dbVersion);
        this.ctxRef = new WeakReference<Context>(context);
        this.dbName = dbName;
        this.dbPath = "/data/data/" + packageName + "/databases/";
        this.dbProvider = dbProvider;
        Log.d(TAG, "Create " + getClass().getSimpleName() + " [dbPath=" + dbPath + "/" + dbName + "]");
    }

    @Override
    public void setDbProvider(DbProvider dbProvider) {
        this.dbProvider = dbProvider;
    }

    @Override
    public void deploy() {
        getReadableDatabase();
        try {
            copyDatabase();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        close();
    }

    @Override
    public void deleteDb() {
        File dbFile = getDbFile();
        File dbJournalFile = getDbJournalFile();
        dbFile.delete();
        dbJournalFile.delete();
    }

    @Override
    public boolean exists() {
        SQLiteDatabase checkDB = null;
        try {
            String fullPath = dbPath + dbName;
            checkDB = SQLiteDatabase.openDatabase(fullPath, null, SQLiteDatabase.OPEN_READONLY | SQLiteDatabase.NO_LOCALIZED_COLLATORS);
        } catch (Exception e) {
        }

        if (checkDB != null) {
            try {
                checkDB.close();
            } catch (Exception e) {
            }
        }
        return checkDB != null;
    }

    @Override
    public File getBackupFile() {
        return getBackupDbFile();
    }

    @Override
    public void backup() {
        Context ctx = ctxRef.get();
        if (ctx != null) {
            try {
                InputStream in = new BufferedInputStream(new FileInputStream(getDbFile()));
                OutputStream out = new GZIPOutputStream(new FileOutputStream(new File(ctxRef.get().getFilesDir(), "backup-" + dbName)));
                IoUtils.copy(in, out);
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        } else {
            Log.e(TAG, "Couldn't backup! Missing reference on Context!");
        }
    }

    private void copyDatabase() throws IOException {
        IoUtils.copy(dbProvider.getDb(), new FileOutputStream(dbPath + dbName));
    }

    private File getDbFile() {
        return new File(dbPath, dbName);
    }

    private File getDbJournalFile() {
        return new File(dbPath, dbName + "-journal");
    }

    private File getBackupDbFile() {
        return new File(dbPath, "backup-" + dbName);
    }
}