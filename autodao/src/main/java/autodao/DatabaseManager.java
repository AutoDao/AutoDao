package autodao;

import android.database.sqlite.SQLiteDatabase;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by tubingbing on 16/5/31.
 */
public class DatabaseManager {

    private static DatabaseManager instance;
    private AtomicInteger mOpenCounter = new AtomicInteger();
    private SQLiteDatabase mDatabase;

    private DatabaseManager(){}

    public static void init(){
        getInstance();
    }

    static synchronized DatabaseManager getInstance(){
        if(instance == null) instance = new DatabaseManager();
        return instance;
    }

    synchronized SQLiteDatabase openDatabase() {
        if (mOpenCounter.incrementAndGet() == 1) {
            mDatabase = AutoDao.getSQLiteOpenHelper().getWritableDatabase();
        }
        return mDatabase;
    }

    synchronized void closeDatabase() {
        if (mOpenCounter.decrementAndGet() == 0) {
            mDatabase.close();
        }
    }
}
