package autodao;

/**
 * Created by tubingbing on 16/6/15.
 */
public class Insert extends Operator {

    Model model;

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
        return AutoDao.getInjector().save(clazz, model);
    }
}
