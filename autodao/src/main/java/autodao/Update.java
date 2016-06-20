package autodao;

/**
 * Created by tubingbing on 16/6/16.
 */
public class Update extends Operator{

    Model model;
    String[] targetColumns;

    public Update(){
        super();
    }

    public Update(String...columns){
        super();
        targetColumns = columns;
    }

    public Update from(Class clazz){
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

    public Update with(Model model){
        this.model = model;
        return this;
    }

    public int update(){
        if (this.clazz == null) throw new IllegalArgumentException("Must call from(Class clazz) method to set the Class");
        if (this.model == null) throw new IllegalArgumentException("Must call with(Model model) method to set the Model");
        return AutoDao.getInjector().update(clazz, model, mWhere.toString(), getArgments(), targetColumns);
    }

}
