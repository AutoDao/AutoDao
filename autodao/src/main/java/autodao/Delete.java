package autodao;

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

    public int delete() {
        if (this.clazz == null)
            throw new IllegalArgumentException("Must call from(Class clazz) to set the Class");
        return injector.delete(clazz, mWhere.toString(), getArgments());
    }

}
