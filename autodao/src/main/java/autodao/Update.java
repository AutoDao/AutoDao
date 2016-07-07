package autodao;

import android.content.ContentValues;
import android.text.TextUtils;

import java.util.Arrays;
import java.util.Map;

/**
 * Created by tubingbing on 16/6/16.
 */
public class Update extends Operator {

    public Update(Injector injector) {
        super(injector);
    }

    public Update(Injector injector, String...columns) {
        super(injector);
        targetColumns = columns;
    }

    public Update from(Class<? extends Model> clazz) {
        fromArg(clazz);
        return this;
    }

    public Update where(String clause) {
        whereArg(clause);
        return this;
    }

    public Update where(String clause, Object... args) {
        whereArg(clause, args);
        return this;
    }

    public Update with(Model model) {
        this.model = model;
        return this;
    }

    public int update() {
        if (this.clazz == null)
            throw new IllegalArgumentException("Must call from(Class clazz) to set the Class");
        if (this.model == null)
            throw new IllegalArgumentException("Must call with(Model model) to set the Model");

        return injector.update(this);
//        return injector.update(clazz,
//                model,
//                mWhere.toString(),
//                getArgments(),
//                targetColumns);
    }

    @Override
    public String toSql(ContentValues values) {
        StringBuilder sql = new StringBuilder(120);
        sql.append("UPDATE ");
        sql.append(getTableName());
        sql.append(" SET ");

        // move all bind args to one array
        int setValuesSize = values.size();
        int bindArgsSize = (getArgments() == null) ? setValuesSize : (setValuesSize + getArgments().length);
        Object[] bindArgs = new Object[bindArgsSize];
        int i = 0;
        for (Map.Entry<String, Object> entry : values.valueSet()) {
            String colName = entry.getKey();
            sql.append((i > 0) ? "," : "");
            sql.append(colName);
            bindArgs[i++] = values.get(colName);
            sql.append("=?");
        }
        if (getArgments() != null) {
            for (i = setValuesSize; i < bindArgsSize; i++) {
                bindArgs[i] = getArgments()[i - setValuesSize];
            }
        }
        if (!TextUtils.isEmpty(mWhere.toString())) {
            sql.append(" WHERE ");
            sql.append(mWhere.toString());
        }
        mArguments = Arrays.asList(bindArgs);
        if (AutoDaoLog.isDebug())
            AutoDaoLog.d(sql.toString());
        return sql.toString();
    }
}
