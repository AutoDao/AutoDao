package autodao;

/**
 * Created by tubingbing on 16/6/16.
 */
public class Update extends Operator {

    Model model;
    String[] targetColumns;

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
        return injector.update(clazz,
                model,
                mWhere.toString(),
                getArgments(),
                targetColumns);
    }

}
