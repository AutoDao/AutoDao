package autodao;

import android.content.ContentValues;

import java.util.Arrays;
import java.util.Map;

/**
 * Created by tubingbing on 16/6/15.
 */
public class Insert extends Operator {

    public Insert(Injector injector) {
        super(injector);
    }

    public Insert from(Class<? extends Model> clazz) {
        fromArg(clazz);
        return this;
    }

    public Insert with(Model model) {
        this.model = model;
        return this;
    }

    public long insert() {
        if (this.clazz == null)
            throw new IllegalArgumentException("Must call from(Class clazz) to set the Class");
        if (this.model == null)
            throw new IllegalArgumentException("Must call with(Model model) to set the Model");
        long _id = injector.save(this);
        model._id = _id;
        return _id;
    }

    @Override
    public String toSql(ContentValues values) {
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT");
        sql.append(" INTO ");
        sql.append(getTableName());
        sql.append('(');

        Object[] bindArgs = null;
        int size = (values != null && values.size() > 0)
                ? values.size() : 0;
        if (size > 0) {
            bindArgs = new Object[size];
            int i = 0;
            for (Map.Entry<String, Object> entry : values.valueSet()) {
                sql.append((i > 0) ? "," : "");
                sql.append(entry.getKey());
                bindArgs[i++] = values.get(entry.getKey());
            }
            sql.append(')');
            sql.append(" VALUES (");
            for (i = 0; i < size; i++) {
                sql.append((i > 0) ? ",?" : "?");
            }
        } else {
            throw new IllegalArgumentException("");
        }
        sql.append(')');
        mArguments = Arrays.asList(bindArgs);
        return sql.toString();
    }
}
