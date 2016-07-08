package autodao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by tubingbing on 16/6/15.
 */
public abstract class Operator {

    static final int FIELD_TYPE_NULL = 0;
    static final int FIELD_TYPE_INTEGER = 1;
    static final int FIELD_TYPE_FLOAT = 2;
    static final int FIELD_TYPE_STRING = 3;
    static final int FIELD_TYPE_BLOB = 4;

    Injector injector;
    Class<? extends Model> clazz;
    StringBuilder mWhere = new StringBuilder();
    List<Object> mArguments;
    String[] targetColumns;
    Model model;

    public Operator(Injector injector) {
        if (injector == null) throw new IllegalArgumentException("Injector can't be NULL");
        this.injector = injector;
        mArguments = new ArrayList<>();
    }

    void fromArg(Class<? extends Model> clazz) {
        this.clazz = clazz;
    }

    void whereArg(String clause) {
        // Chain conditions if a previous condition exists.
        if (mWhere.length() > 0) {
            mWhere.append(" AND ");
        }
        mWhere.append(clause);
    }

    void whereArg(String clause, Object... args) {
        whereArg(clause);
        addArguments(args);
    }

    void addArguments(Object[] args) {
        for (Object arg : args) {
            if (arg.getClass() == boolean.class || arg.getClass() == Boolean.class) {
                boolean flag = (boolean) arg;
                arg = flag ? 1 : 0;
            }
            mArguments.add(arg);
        }
    }

    public String[] getArgments() {
        String[] args = new String[mArguments.size()];
        for (int i = 0; i < mArguments.size(); i++) {
            args[i] = String.valueOf(mArguments.get(i));
        }
        return args;
    }

    public String[] getTargetColumns() {
        return targetColumns;
    }

    public Model getModel() {
        return model;
    }

    public String getTableName(){
        return injector.getTableName(getModelCanonicalName());
    }

    public String getModelCanonicalName(){
        return clazz.getCanonicalName();
    }

    public void bindStatement(SQLiteStatement statement){

        for (int index = 0, len = mArguments.size(); index < len; index++) {
            Object arg = mArguments.get(index);
            switch (getTypeOfObject(arg)) {
                case FIELD_TYPE_NULL:
                    statement.bindNull(index + 1);
                    break;
                case FIELD_TYPE_INTEGER:
                    statement.bindLong(index + 1, ((Number)arg).longValue());
                    break;
                case FIELD_TYPE_FLOAT:
                    statement.bindDouble(index + 1, ((Number)arg).doubleValue());
                    break;
                case FIELD_TYPE_BLOB:
                    statement.bindBlob(index + 1, (byte[])arg);
                    break;
                case FIELD_TYPE_STRING:
                default:
                    if (arg instanceof Boolean) {
                        // Provide compatibility with legacy applications which may pass
                        // Boolean values in bind args.
                        statement.bindLong(index + 1, ((Boolean)arg).booleanValue() ? 1 : 0);
                    } else {
                        statement.bindString(index + 1, arg.toString());
                    }
                    break;
            }
        }
    }

    public static int getTypeOfObject(Object obj) {
        if (obj == null) {
            return FIELD_TYPE_NULL;
        } else if (obj instanceof byte[]) {
            return FIELD_TYPE_BLOB;
        } else if (obj instanceof Float || obj instanceof Double) {
            return FIELD_TYPE_FLOAT;
        } else if (obj instanceof Long || obj instanceof Integer
                || obj instanceof Short || obj instanceof Byte) {
            return FIELD_TYPE_INTEGER;
        } else {
            return FIELD_TYPE_STRING;
        }
    }

    public String toSql(){
        throw new RuntimeException("Invalid");
    }

    public String toSql(ContentValues values){
        throw new RuntimeException("Invalid");
    }
}
