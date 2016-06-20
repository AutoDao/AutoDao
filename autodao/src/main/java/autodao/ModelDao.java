package autodao;

import java.util.List;

/**
 * Created by tubingbing on 16/6/14.
 */
public interface ModelDao {
    long save(Object obj);
    int update(Object obj, String whereClause, String[] whereArgs, String[] targetColumns);
    <M extends Model> List<M> select(boolean distinct, String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit);
    <M extends Model> M selectSingle(boolean distinct, String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit);
//    <M extends Model> List<M> select(boolean distinct, Class clazz, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit, Object mappingObj);
//    <M extends Model> M selectSingle(boolean distinct, Class clazz, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit, Object mappingObj);
}
