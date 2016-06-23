package com.example.autodao;

import android.app.Application;
import android.util.Log;

import autodao.AutoDao;

/**
 * Created by tubingbing on 16/6/8.
 */
public class MyApplication extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        AutoDao.init(new MySQLiteOpenHelper(this));
    }
}
