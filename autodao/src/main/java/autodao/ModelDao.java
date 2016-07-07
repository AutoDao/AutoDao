package autodao;

import android.database.Cursor;

import java.util.List;

/**
 * Created by tubingbing on 16/6/14.
 */
public interface ModelDao {
    long save(Operator operator);
    int delete(Operator operator);
    int update(Operator operator);
    <M extends Model> List<M> select(Operator operator);
    <M extends Model> M selectSingle(Operator operator);
    Cursor joinSelect(Operator operator);

}
