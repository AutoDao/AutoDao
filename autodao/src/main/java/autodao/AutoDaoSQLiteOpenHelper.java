package autodao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by tubingbing on 16/6/2.
 */
public abstract class AutoDaoSQLiteOpenHelper extends SQLiteOpenHelper{

    public AutoDaoSQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        AutoDao.getInjector().createTable(db);
        AutoDao.getInjector().createIndex(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        AutoDao.getInjector().executePragma(db);
    }

}
