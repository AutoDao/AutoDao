package autodao;

import android.database.sqlite.SQLiteDatabase;

import java.util.List;

/**
 * Created by tubingbing on 16/6/2.
 */
public interface Injector {

    void executePragma(SQLiteDatabase db);
    void createTable(SQLiteDatabase db);
    void createIndex(SQLiteDatabase db);
    long save(Class clazz, Object obj);
    int delete(Class clazz, String whereClause, String[] whereArgs);
    int update(Class clazz, Object obj, String whereClause, String[] whereArgs, String[] updateColumns);
    <M extends Model> List<M> select(boolean distinct, Class clazz, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit);
    <M extends Model> M selectSingle(boolean distinct, Class clazz, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit);
    ModelDao getModelDao(String clazzName);
    String getTableName(String clazzName);
}
