package autodao;

/**
 * Created by tubingbing on 16/6/15.
 */
public class Delete extends Operator{

    public Delete from(Class clazz){
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

    public int delete(){
        if (this.clazz == null) throw new IllegalArgumentException("Must call from(Class clazz) method to set the Class");
        return AutoDao.getInjector().delete(clazz, mWhere.toString(), getArgments());
    }

}
