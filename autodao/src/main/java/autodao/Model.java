package autodao;

import android.util.Log;

/**
 * Created by tubingbing on 16/6/2.
 */
public abstract class Model {

    @Column(name = "_id")
    public long _id;

    public long save(){
        return AutoDao.getInjector().save(getClass(), this);
    }

    public static int delete(Class clazz, long _id){
        return new Delete().from(clazz).where("_id=?", _id).delete();
    }
}
