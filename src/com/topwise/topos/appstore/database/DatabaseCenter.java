
package com.topwise.topos.appstore.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.net.Uri;
import android.provider.BaseColumns;

public class DatabaseCenter {

    private static final String DATABASE_NAME = "local.db";
    private static final int DATABASE_VERSION = 1;
    
    private static final String SCHEME = "content://";
    private static final String AUTHORITY = "com.topwise.topos.appstore";

    public static final String _ID = "_id";

    public static final class InstallRecordTable implements BaseColumns {
        public static final String TABLE_NAME = "install_record";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_ICON_PATH = "icon_path";
        public static final String COLUMN_VERSION_NAME = "version_name";
        public static final String COLUMN_PATH = "path";
        public static final Uri CONTENT_URI =  Uri.parse(SCHEME + AUTHORITY + "/" + TABLE_NAME);
    }
    
    public static final class UpgradeRecordTable implements BaseColumns {
        public static final String TABLE_NAME = "upgrade_record";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_ICON_PATH = "icon_path";
        public static final String COLUMN_VERSION_NAME = "version_name";
        public static final String COLUMN_PATH = "path";
        public static final Uri CONTENT_URI =  Uri.parse(SCHEME + AUTHORITY + "/" + TABLE_NAME);
    }
    
    public static final class IsNewTable implements BaseColumns {
        public static final String TABLE_NAME = "isNew";
        public static final String COLUMN_UID = "uid";
        public static final String COLUMN_ISNEW = "isnew";
        public static final String COLUMN_TIME = "time";
        public static final Uri CONTENT_URI =  Uri.parse(SCHEME + AUTHORITY + "/" + TABLE_NAME);
    }

    private DatabaseHelper mOpenHelper = null;

    private String mTableName = null;
    
    public DatabaseCenter(Context context) {
        mOpenHelper = new DatabaseHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public DatabaseCenter(Context context, String tableName) {
        if (tableName == InstallRecordTable.TABLE_NAME 
                || tableName == UpgradeRecordTable.TABLE_NAME
                || tableName == IsNewTable.TABLE_NAME) {
            mTableName = tableName;
            mOpenHelper = new DatabaseHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
    }
    
    public void close() {
        mOpenHelper.close();
    }
    
    public Cursor query(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return this.query(mTableName, projection, selection, selectionArgs, sortOrder);
    }

    public Cursor query(String tableName, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (tableName == null) {
            return null;
        }
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(tableName);
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        return c;
    }
    
    public long insert(ContentValues initialValues) {
        return this.insert(mTableName, initialValues);
    }

    public long insert(String tableName, ContentValues initialValues) {
        if (tableName == null) {
            return -1;
        }
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        return db.insert(tableName, null, initialValues);
    }
    
    public int delete(String where, String[] whereArgs) {
        return this.delete(mTableName, where, whereArgs);
    }

    public int delete(String tableName, String where, String[] whereArgs) {
        if (tableName == null) {
            return -1;
        }
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        return db.delete(tableName, where, whereArgs);
    }
    
    public int update(ContentValues values, String where, String[] whereArgs) {
        return this.update(mTableName, values, where, whereArgs);
    }

    public int update(String tableName, ContentValues values, String where, String[] whereArgs) {
        if (tableName == null) {
            return -1;
        }
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        return db.update(tableName, values, where, whereArgs);
    }
    
    public DatabaseHelper getDatabaseHelper() {
        return mOpenHelper;
    }

    public class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context, String name, CursorFactory factory, int version) {
            super(context, name, factory, version);
            // TODO Auto-generated constructor stub
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + InstallRecordTable.TABLE_NAME + " (" + _ID  + " INTEGER PRIMARY KEY,"
                    + InstallRecordTable.COLUMN_ID + " TEXT,"
                    + InstallRecordTable.COLUMN_TITLE + " TEXT,"
                    + InstallRecordTable.COLUMN_ICON_PATH + " TEXT,"
                    + InstallRecordTable.COLUMN_VERSION_NAME + " TEXT,"
                    + InstallRecordTable.COLUMN_PATH + " TEXT);");
            db.execSQL("CREATE TABLE " + UpgradeRecordTable.TABLE_NAME + " (" + _ID  + " INTEGER PRIMARY KEY,"
                    + UpgradeRecordTable.COLUMN_ID + " TEXT,"
                    + UpgradeRecordTable.COLUMN_TITLE + " TEXT,"
                    + UpgradeRecordTable.COLUMN_ICON_PATH + " TEXT,"
                    + UpgradeRecordTable.COLUMN_VERSION_NAME + " TEXT,"
                    + UpgradeRecordTable.COLUMN_PATH + " TEXT);");
            db.execSQL("CREATE TABLE " + IsNewTable.TABLE_NAME + " (" + _ID + " INTEGER PRIMARY KEY,"
                    + IsNewTable.COLUMN_UID + " TEXT,"
                    + IsNewTable.COLUMN_ISNEW + " TEXT,"
                    + IsNewTable.COLUMN_TIME + " TEXT);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + InstallRecordTable.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + UpgradeRecordTable.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + IsNewTable.TABLE_NAME);
            onCreate(db);
        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + InstallRecordTable.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + UpgradeRecordTable.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + IsNewTable.TABLE_NAME);
            onCreate(db);
        }

    }

}
