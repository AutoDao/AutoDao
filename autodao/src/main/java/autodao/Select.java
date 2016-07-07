package autodao;

import android.database.sqlite.SQLiteQueryBuilder;

import java.util.Arrays;
import java.util.List;

/**
 * Created by tubingbing on 16/6/16.
 */
public class Select extends Operator {

    protected boolean distinct = false;
    protected String groupBy;
    protected String having;
    protected String orderBy;
    protected String limit;

    public Select(Injector injector) {
        super(injector);
    }

    public Select(Injector injector, String...columns) {
        super(injector);
        targetColumns = columns;
    }

    public Select from(Class<? extends Model> clazz) {
        fromArg(clazz);
        return this;
    }

    public Select where(String clause) {
        whereArg(clause);
        return this;
    }

    public Select where(String clause, Object... args) {
        whereArg(clause, args);
        return this;
    }

    public Select distinct() {
        distinct = true;
        return this;
    }

    public Select groupBy(String groupBy) {
        this.groupBy = groupBy;
        return this;
    }

    public Select having(String having) {
        this.having = having;
        return this;
    }

    public Select orderBy(String orderBy) {
        this.orderBy = orderBy;
        return this;
    }

    public Select limit(String limit) {
        this.limit = limit;
        return this;
    }

    public <M extends Model> List<M> select() {
        checkSelect();
        return injector.select(this);
    }

    public <M extends Model> M selectSingle() {
        checkSelect();
        return injector.selectSingle(this);
    }

    @Override
    public String toSql() {
        String sql = SQLiteQueryBuilder.buildQueryString(distinct,
                getTableName(),
                targetColumns,
                mWhere.toString(),
                groupBy,
                having, orderBy, limit);
        if (AutoDaoLog.isDebug())
            AutoDaoLog.d(sql);
        return sql;
    }

    private void checkSelect() {
        if (this.clazz == null)
            throw new IllegalArgumentException("Must call from(Class clazz) to set the Class");
        if (targetColumns != null) {
            List<String> columnList = Arrays.asList(targetColumns);
            if (!columnList.contains("_id")) {
                columnList.add("_id");
                targetColumns = (String[]) columnList.toArray();
            }
        }
    }

}
