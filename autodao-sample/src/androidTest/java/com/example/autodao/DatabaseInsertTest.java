package com.example.autodao;

import android.app.Application;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.List;

import autodao.AutoSQLiteOpenHelper;
import autodao.Injector;
import autodao.Insert;
import autodao.Model;
import autodao.Select;

/**
 * Created by bill_lv on 2016/7/12.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class DatabaseInsertTest {


    private static long TEST_COUNT = 100000;
    private static String TAG = "DatabaseInsertTest";

    private static String DEFAULT_TEST_DB_NAME = "auto_test.db";

    private AutoSQLiteOpenHelper helper;

    @Test
    public void testAutoDAOTransactionInsert() {

        SQLiteDatabase db = helper.getWritableDatabase();

        // start insert
        TestTimeUtils.start(TAG, "testAutoDAOTransactionInsert");
        db.beginTransaction();
        Injector injector = helper.getInjector(db);
        Insert from = new Insert(injector).from(Address.class);
        Address address = new Address();
        for (int i = 0; i < TEST_COUNT; i++) {
            address.name = "xxx" + i;
            address.userId = i;
            from.with(address).insert();
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        TestTimeUtils.stop();
        // test insert count
        long count = new Select(injector).from(Address.class).select().size();
        Assert.assertEquals(TEST_COUNT, count);
    }

    @Test
    public void testSqlTransactionInsertWithPreCompile() {

        SQLiteDatabase db = helper.getWritableDatabase();
        // create table
        String createTable = "CREATE TABLE IF NOT EXISTS address(_id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT,userId INTEGER)";
        db.execSQL(createTable);
        db.beginTransaction();
        // start insert
        TestTimeUtils.start(TAG, "testSqlTransactionInsertWithPreCompile");
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
        TestTimeUtils.stop();

        // test insert count
        Cursor c = db.rawQuery("select * from address", null);
        long count = c.getCount();
        Assert.assertEquals(TEST_COUNT, count);

    }

    @Test
    public void testSqlSingleInsert() {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL("CREATE TABLE IF NOT EXISTS TestUser(_id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT,age INTEGER)");

        TestTimeUtils.start(TAG, "testSqlSingleInsert");
        db.execSQL("insert into TestUser(name, age) values('bill',101) ");
        TestTimeUtils.stop();
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables("TestUser");
        queryBuilder.appendWhere("name='bill'");
        Cursor cursor = queryBuilder.query(db, null, null, null, null, null, null);
        Assert.assertEquals(1, cursor.getCount());
        if (cursor.moveToFirst()) {
            Assert.assertEquals("bill", cursor.getString(1));
            Assert.assertEquals(101, cursor.getInt(2));
        }
    }

    @Test
    public void testAutoDAOSingleInsert() {
        // insert item
        SQLiteDatabase db = helper.getWritableDatabase();
        Injector injector = helper.getInjector(db);
        TestTimeUtils.start(TAG, "testAutoDAOSingleInsert");
        TestUser user = new TestUser();
        user.name = "bill";
        user.age = 100;
        new Insert(injector).from(TestUser.class).with(user).insert();
        TestTimeUtils.stop();

        List<Model> select = new Select(injector).from(TestUser.class).where("name='bill'").select();
        Assert.assertEquals(1, select.size());
        TestUser u = (TestUser) select.get(0);
        Assert.assertEquals(user.name, u.name);
        Assert.assertEquals(user.age, u.age);
    }

    @Before
    public void createDB() {
        helper = new MySQLiteOpenHelper(InstrumentationRegistry.getTargetContext(), DEFAULT_TEST_DB_NAME, null, 2);
    }

    @After
    public void clearDB() {
        Log.d(TAG, "clear db " + deleteDefaultDBFile());
    }

    public boolean deleteDefaultDBFile() {
        return deleteDBFile(DEFAULT_TEST_DB_NAME);
    }

    public boolean deleteDBFile(String dbName) {
        File databasePath = InstrumentationRegistry.getTargetContext().getDatabasePath(dbName);
        if (!databasePath.exists()) {
            Log.d(TAG, dbName + " is not exists");
        }
        File databaseLogPath = InstrumentationRegistry.getTargetContext().getDatabasePath(dbName + "-journal");
        if (!databaseLogPath.exists()) {
            Log.d(TAG, dbName + "-journal" + " is not exists");
        }
        return databasePath.delete() && databaseLogPath.delete();
    }
}
