package autodao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import java.util.List;

/**
 * Created by tubingbing on 16/6/2.
 */
public interface Injector {

    long save(Operator operator);
    int delete(Operator operator);
    int update(Operator operator);
    <M extends Model> List<M> select(Operator operator);
    <M extends Model> M selectSingle(Operator operator);
    Cursor joinSelect(Operator operator);
    ModelDao getModelDao(String clazzName);
    String getTableName(String clazzName);
    TypeSerializer getSerializer(String serializerCanonicalName);
    SQLiteStatement getStatement(String mappingSql);
    void putStatement(String mappingSql, SQLiteStatement statement);
}
