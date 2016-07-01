package autodao;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tubingbing on 16/6/15.
 */
public abstract class Operator {

    Injector injector;
    Class clazz;
    StringBuilder mWhere = new StringBuilder();
    List<Object> mArguments;

    public Operator(Injector injector) {
        if (injector == null) throw new IllegalArgumentException("Injector can't be NULL");
        this.injector = injector;
        mArguments = new ArrayList<>();
    }

    void fromArg(Class clazz) {
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

    String[] getArgments() {
        String[] args = new String[mArguments.size()];
        for (int i = 0; i < mArguments.size(); i++) {
            args[i] = String.valueOf(mArguments.get(i));
        }
        return args;
    }
}
