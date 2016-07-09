package autodao;

import android.text.TextUtils;

/**
 * Created by tubingbing on 16/6/15.
 */
public class Delete extends Operator {

    public Delete(Injector injector) {
        super(injector);
    }

    public Delete from(Class<? extends Model> clazz) {
        fromArg(clazz);
        return this;
    }

    public Delete where(String clause) {
        whereArg(clause);
        return this;
    }

    public Delete where(String clause, Object... args) {
        whereArg(clause, args);
        return this;
    }

    public Delete and(String clause) {
        andArg(clause);
        return this;
    }

    public Delete and(String clause, Object... args) {
        andArg(clause, args);
        return this;
    }

    public Delete or(String clause) {
        orArg(clause);
        return this;
    }

    public Delete or(String clause, Object... args) {
        orArg(clause, args);
        return this;
    }

    public int delete() {
        if (this.clazz == null)
            throw new IllegalArgumentException("Must call from(Class clazz) to set the Class");
        return injector.delete(this);
    }

    @Override
    public String toSql() {
        String sql = toDeleteSql();
        if (AutoDaoLog.isDebug())
            AutoDaoLog.d(sql);
        return sql;
    }

    private String toDeleteSql(){
        return "DELETE FROM " + getTableName()
                + (!TextUtils.isEmpty(mWhere.toString()) ? " WHERE " + mWhere.toString() : "");
    }
}
