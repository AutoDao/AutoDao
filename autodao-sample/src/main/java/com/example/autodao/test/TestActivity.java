package com.example.autodao.test;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.autodao.Address;
import com.example.autodao.MySQLiteOpenHelper;
import com.example.autodao.R;

import autodao.AutoSQLiteOpenHelper;
import autodao.Injector;
import autodao.Insert;

/**
 * This activity is used to profiling with android monitor or ddms
 *
 * Created by bill_lv on 2016/7/11.
 */
public class TestActivity extends AppCompatActivity {

    private static long TEST_COUNT = 100000;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        // test auto-orm insert
        findViewById(R.id.test_insert).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AutoSQLiteOpenHelper helper = new MySQLiteOpenHelper(TestActivity.this, "xxxxx.db", null, 2);
                SQLiteDatabase db = helper.getWritableDatabase();

                // start insert
                db.beginTransaction();
                Injector injector = helper.getInjector(db);
                Address address = new Address();
                Insert from = new Insert(injector).from(Address.class).with(address);
                for (int i = 0; i < TEST_COUNT; i++) {
                    address.name = "xxx" + i;
                    address.userId = i;
                    from.insert();
                }
                db.setTransactionSuccessful();
                db.endTransaction();
            }
        });

        // test raw sql insert
        findViewById(R.id.test_sql_insert).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AutoSQLiteOpenHelper helper = new MySQLiteOpenHelper(TestActivity.this, "xxxxx.db", null, 2);
                SQLiteDatabase db = helper.getWritableDatabase();
                helper.dropAllTables(db);
                // create table
                String createTable = "CREATE TABLE IF NOT EXISTS address(_id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT,userId INTEGER)";
                db.execSQL(createTable);
                db.beginTransaction();
                // start insert
                String sql = "insert into address(name, userId) values (?, ?) ";
                SQLiteStatement statement = db.compileStatement(sql);
                for (int i = 0; i < TEST_COUNT; i++) {
                    statement.bindLong(1, i);
                    statement.bindString(2, "xxxx" + i);
                    statement.executeInsert();
                    statement.clearBindings();
                }
                db.setTransactionSuccessful();
                db.endTransaction();
            }
        });
    }
}
