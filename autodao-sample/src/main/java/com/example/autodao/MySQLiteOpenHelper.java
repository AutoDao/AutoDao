package com.example.autodao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import autodao.AutoSQLiteOpenHelper;

/**
 * Created by tubingbing on 16/6/8.
 */
public class MySQLiteOpenHelper extends AutoSQLiteOpenHelper{

    static final String TAG = "autodao";

    public MySQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
        Log.d(TAG, "MySQLiteOpenHelper create...");
    }

    public MySQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        Log.d(TAG, "MySQLiteOpenHelper create...");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        super.onCreate(db);
        Log.d(TAG, "MySQLiteOpenHelper onCreate...");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}
