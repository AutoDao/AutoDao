package autodao;

import android.database.Cursor;
import android.text.TextUtils;

import java.util.regex.Pattern;

/**
 * Created by tubingbing on 16/7/4.
 */
public class JoinSelect extends Operator{

    private static final Pattern sLimitPattern =
            Pattern.compile("\\s*\\d+\\s*(,\\s*\\d+\\s*)?");

    boolean distinct = false;
    String groupBy;
    String having;
    String orderBy;
    String limit;

    String fromTableAlias;
    String joinTableAlias;

    Class<? extends Model> joinTable;
    int joinType;
    String on;

    public JoinSelect(Injector injector) {
        super(injector);
    }

    public JoinSelect(Injector injector, String...columns) {
        super(injector);
        targetColumns = columns;
    }

    public JoinSelect from(Class<? extends Model> clazz, String alias) {
        fromArg(clazz);
        this.fromTableAlias = alias;
        return this;
    }

    public JoinSelect from(Class<? extends Model> clazz) {
        fromArg(clazz);
        return this;
    }

    public JoinSelect where(String clause) {
        whereArg(clause);
        return this;
    }

    public JoinSelect where(String clause, Object... args) {
        whereArg(clause, args);
        return this;
    }

    public JoinSelect and(String clause) {
        andArg(clause);
        return this;
    }

    public JoinSelect and(String clause, Object... args) {
        andArg(clause, args);
        return this;
    }

    public JoinSelect or(String clause) {
        orArg(clause);
        return this;
    }

    public JoinSelect or(String clause, Object... args) {
        orArg(clause, args);
        return this;
    }

    public JoinSelect distinct() {
        distinct = true;
        return this;
    }

    public JoinSelect groupBy(String groupBy) {
        this.groupBy = groupBy;
        return this;
    }

    public JoinSelect having(String having) {
        this.having = having;
        return this;
    }

    public JoinSelect orderBy(String orderBy) {
        this.orderBy = orderBy;
        return this;
    }

    public JoinSelect limit(String limit) {
        this.limit = limit;
        return this;
    }

    public JoinSelect on(String on){
        this.on = on;
        return this;
    }

    public JoinSelect leftJoin(Class<? extends Model> joinTable, String alias){
        this.joinType = JoinType.LEFT;
        this.joinTable = joinTable;
        this.joinTableAlias = alias;
        return this;
    }

    public JoinSelect leftJoin(Class<? extends Model> joinTable){
        this.joinType = JoinType.LEFT;
        this.joinTable = joinTable;
        return this;
    }

    public JoinSelect outerJoin(Class<? extends Model> joinTable, String alias){
        this.joinType = JoinType.OUTER;
        this.joinTable = joinTable;
        this.joinTableAlias = alias;
        return this;
    }

    public JoinSelect outerJoin(Class<? extends Model> joinTable){
        this.joinType = JoinType.OUTER;
        this.joinTable = joinTable;
        return this;
    }

    public JoinSelect innerJoin(Class<? extends Model> joinTable){
        this.joinType = JoinType.INNER;
        this.joinTable = joinTable;
        return this;
    }

    public JoinSelect innerJoin(Class<? extends Model> joinTable, String alias){
        this.joinType = JoinType.INNER;
        this.joinTable = joinTable;
        this.joinTableAlias = alias;
        return this;
    }

    public JoinSelect crossJoin(Class<? extends Model> joinTable, String alias){
        this.joinType = JoinType.CROSS;
        this.joinTable = joinTable;
        this.joinTableAlias = alias;
        return this;
    }

    public JoinSelect crossJoin(Class<? extends Model> joinTable){
        this.joinType = JoinType.CROSS;
        this.joinTable = joinTable;
        return this;
    }

    public Cursor select(){
        if (this.clazz == null)
            throw new IllegalArgumentException("Must call from(Class clazz) to set the Class");
        return injector.joinSelect(this);
    }

    public Object select(CursorHandler cursorHandler){
        if (cursorHandler == null)
            throw new IllegalArgumentException("CursorHandler can't be NULL");
        Cursor cursor = select();
        return cursorHandler.onHandle(cursor);
    }

    @Override
    public String toSql() {
        return buildJoinQueryString();
    }

    private String buildJoinQueryString() {
        if (TextUtils.isEmpty(groupBy) && !TextUtils.isEmpty(having)) {
            throw new IllegalArgumentException(
                    "HAVING clauses are only permitted when using a groupBy clause");
        }
        if (!TextUtils.isEmpty(limit) && !sLimitPattern.matcher(limit).matches()) {
            throw new IllegalArgumentException("invalid LIMIT clauses:" + limit);
        }

        StringBuilder query = new StringBuilder(120);

        query.append("SELECT ");
        if (distinct) {
            query.append("DISTINCT ");
        }
        if (targetColumns != null && targetColumns.length != 0) {
            appendColumns(query, targetColumns);
        } else {
            query.append("* ");
        }
        query.append("FROM ");
        query.append(getTableName());
        if (!TextUtils.isEmpty(fromTableAlias)) {
            query.append(" ").append(fromTableAlias);
        }

        String joinType = getJoinTypeStr();
        query.append(" ").append(joinType);
        query.append(" ").append(getJoinTableName());
        if (!TextUtils.isEmpty(joinTableAlias)) {
            query.append(" ").append(joinTableAlias);
        }
        query.append(" ON ").append(on);

        appendClause(query, " WHERE ", mWhere.toString());
        appendClause(query, " GROUP BY ", groupBy);
        appendClause(query, " HAVING ", having);
        appendClause(query, " ORDER BY ", orderBy);
        appendClause(query, " LIMIT ", limit);

        String sql = query.toString();
        if (AutoDaoLog.isDebug())
            AutoDaoLog.d(sql);
        return sql;
    }

    private String getJoinTableName(){
        return injector.getTableName(joinTable.getCanonicalName());
    }

    private String getJoinTypeStr() {
        String type;
        switch (joinType) {
            case JoinType.LEFT:
                type = "LEFT JOIN";
                break;
            case JoinType.OUTER:
                type = "OUTER JOIN";
                break;
            case JoinType.INNER:
                type = "INNER JOIN";
                break;
            case JoinType.CROSS:
                type = "CROSS JOIN";
                break;
            default:
                type = "INNER JOIN";
                break;
        }
        return type;
    }

    private static void appendClause(StringBuilder s, String name, String clause) {
        if (!TextUtils.isEmpty(clause)) {
            s.append(name);
            s.append(clause);
        }
    }

    private static void appendColumns(StringBuilder s, String[] columns) {
        int n = columns.length;
        for (int i = 0; i < n; i++) {
            String column = columns[i];

            if (column != null) {
                if (i > 0) {
                    s.append(", ");
                }
                s.append(column);
            }
        }
        s.append(' ');
    }

    public interface CursorHandler{
        Object onHandle(Cursor cursor);
    }

    interface JoinType{
        int LEFT = 1;
        int OUTER = 2;
        int INNER = 3;
        int CROSS = 4;
    }
}
