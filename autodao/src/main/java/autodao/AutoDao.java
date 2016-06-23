package autodao;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v4.util.LruCache;

import java.util.List;

/**
 * Created by tubingbing on 16/6/2.
 */
public final class AutoDao {

    public static final String INJECTOR_PKG = "autodao";
    public static final String INJECTOR_NAME = "AutoDaoInjector";
    private static Injector injector;

    static AutoDaoSQLiteOpenHelper sqLiteOpenHelper;
    static LruCache<String, Model> sModels;
    static LruCache<String, List<? extends Model>> sModelLists;

    private AutoDao() {

    }

    public static void init(AutoDaoSQLiteOpenHelper helper) {
        sqLiteOpenHelper = helper;
        sModels = new LruCache<>(2048);
        sModelLists = new LruCache<>(4096);
        DatabaseManager.init();
    }

    static SQLiteOpenHelper getSQLiteOpenHelper() {
        if (sqLiteOpenHelper == null)
            throw new IllegalArgumentException("SQLiteOpenHelper must init first!!!");
        return sqLiteOpenHelper;
    }

    public static synchronized Injector getInjector() {
        if (injector == null) {
            try {
                Class injectorClzz = Class.forName(String.format("%s.%s"
                        , INJECTOR_PKG, INJECTOR_NAME));
                injector = (Injector) injectorClzz.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(String.format(
                        "%s %s.%s %s",
                        "Can't find",
                        INJECTOR_PKG,
                        INJECTOR_NAME,
                        "Class"));
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

    public static void beginTransaction() {
        openDatabase().beginTransaction();
    }

    public static void setTransactionSuccessful() {
        openDatabase().setTransactionSuccessful();
    }

    public static void endTransaction() {
        openDatabase().endTransaction();
    }

    public static boolean inTransaction() {
        return openDatabase().inTransaction();
    }

    public synchronized static void cacheModel(String key, Model model) {
        sModels.put(key, model);
    }

    public static Model getModel(String key) {
        return sModels.get(key);
    }

    public synchronized static void cacheModelList(String key, List<? extends Model> modelList) {
        sModelLists.put(key, modelList);
    }

    public static List<? extends Model> getModelList(String key) {
        return sModelLists.get(key);
    }
}
