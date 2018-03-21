package com.topwise.topos.appstore.download;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.provider.BaseColumns;

public class DownloadDatabase {
    private static final String DATABASE_NAME = "download.db";
    private static final int DATABASE_VERSION = 2;

    public static final class Downloads implements BaseColumns {
        public static final String TABLE_NAME = "downloads";

        public static final String COLUMN_UID = "uid"; // 唯一id
        public static final String COLUMN_NAME = "name"; // 名字
        public static final String COLUMN_TYPE = "type"; // 类型
        public static final String COLUMN_TOTAL_SIZE = "total_size"; // 总大小
        public static final String COLUMN_DOWNLOAD_SIZE = "download_size"; // 已下载大小
        public static final String COLUMN_DOWNLOAD_STATUS = "download_status"; // 下载状态
        public static final String COLUMN_DOWNLOAD_URL = "download_url"; // 下载url
        public static final String COLUMN_FILE_PATH = "file_path"; // 本地存储路径
    }

    private DatabaseHelper mOpenHelper = null;
    private String mTableName = null;

    public DownloadDatabase(Context context) {
        mOpenHelper = new DatabaseHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public DownloadDatabase(Context context, String tableName) {
        mTableName = tableName;
        mOpenHelper = new DatabaseHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
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

        return qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
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

    public class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + Downloads.TABLE_NAME + " (" +
                    Downloads._ID + " INTEGER PRIMARY KEY," +
                    Downloads.COLUMN_UID + " TEXT," +
                    Downloads.COLUMN_NAME + " TEXT," +
                    Downloads.COLUMN_TYPE + " TEXT," +
                    Downloads.COLUMN_TOTAL_SIZE + " INTEGER," +
                    Downloads.COLUMN_DOWNLOAD_SIZE + " INTEGER," +
                    Downloads.COLUMN_DOWNLOAD_STATUS + " INTEGER," +
                    Downloads.COLUMN_DOWNLOAD_URL + " TEXT," +
                    Downloads.COLUMN_FILE_PATH + " TEXT" + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + Downloads.TABLE_NAME);
            onCreate(db);
        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + Downloads.TABLE_NAME);
            onCreate(db);
        }
    }
}
