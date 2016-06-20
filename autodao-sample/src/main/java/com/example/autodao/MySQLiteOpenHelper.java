package com.example.autodao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import autodao.AutoDaoSQLiteOpenHelper;

/**
 * Created by tubingbing on 16/6/8.
 */
public class MySQLiteOpenHelper extends AutoDaoSQLiteOpenHelper{

    static final String TAG = "autodao";
    final static String DB_NAME = "test.db";
    final static int DB_VERSION = 1;

    public MySQLiteOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        Log.d(TAG, "MySQLiteOpenHelper create...");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        super.onCreate(db);
        Log.d(TAG, "MySQLiteOpenHelper onCreate...");
    }
}
