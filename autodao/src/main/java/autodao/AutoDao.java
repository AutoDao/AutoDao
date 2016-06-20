package autodao;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by tubingbing on 16/6/2.
 */
public final class AutoDao {

    public static final String INJECTOR_PKG = "autodao";
    public static final String INJECTOR_NAME = "AutoDaoInjector";
    private static Injector injector;

    static AutoDaoSQLiteOpenHelper sqLiteOpenHelper;

    public static void init(AutoDaoSQLiteOpenHelper helper){
        sqLiteOpenHelper = helper;
        DatabaseManager.init();
    }

    static SQLiteOpenHelper getSQLiteOpenHelper(){
        if (sqLiteOpenHelper == null) throw new IllegalArgumentException("SQLiteOpenHelper must init first!!!");
        return sqLiteOpenHelper;
    }

    public static synchronized Injector getInjector(){
        if (injector == null){
            try {
                Class injectorClzz = Class.forName(String.format("%s.%s", INJECTOR_PKG, INJECTOR_NAME));
                injector = (Injector) injectorClzz.newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Can't find " + String.format("%s.%s", INJECTOR_PKG, INJECTOR_NAME) + " class");
            }
        }
        return injector;
    }

    public static SQLiteDatabase openDatabase() {
        return DatabaseManager.getInstance().openDatabase();
    }

    public static void closeDatabase() {
        DatabaseManager.getInstance().closeDatabase();
    }

    public static void beginTransaction(){
        openDatabase().beginTransaction();
    }

    public static void setTransactionSuccessful(){
        openDatabase().setTransactionSuccessful();
    }

    public static void endTransaction(){
        openDatabase().endTransaction();
    }

    public static boolean inTransaction() {
        return openDatabase().inTransaction();
    }

}
