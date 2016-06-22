package autodao;

import java.util.Arrays;
import java.util.List;

/**
 * Created by tubingbing on 16/6/16.
 */
public class Select extends Operator{

    String[] queryColumns;
    boolean distinct = false;
    String groupBy;
    String having;
    String orderBy;
    String limit;

    public Select(){
        super();
    }

    public Select(String...columns){
        super();
        queryColumns = columns;
    }

    public Select from(Class clazz){
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

    public Select distinct(){
        distinct = true;
        return this;
    }

    public Select groupBy(String groupBy){
        this.groupBy = groupBy;
        return this;
    }

    public Select having(String having){
        this.having = having;
        return this;
    }

    public Select orderBy(String orderBy){
        this.orderBy = orderBy;
        return this;
    }

    public Select limit(String limit){
        this.limit = limit;
        return this;
    }

    public <M extends Model> List<M> select(){
        if (this.clazz == null) throw new IllegalArgumentException("Must call from(Class clazz) method to set the Class");
        if (queryColumns != null){
            List<String> columnList = Arrays.asList(queryColumns);
            if (!columnList.contains("_id")){
                columnList.add("_id");
                queryColumns = (String[]) columnList.toArray();
            }
        }
        return AutoDao.getInjector().select(distinct, clazz, queryColumns
                , mWhere.toString(), getArgments(), groupBy, having, orderBy, limit);
    }

    public <M extends Model> M selectSingle(){
        if (this.clazz == null) throw new IllegalArgumentException("Must call from(Class clazz) method to set the Class");
        if (queryColumns != null){
            List<String> columnList = Arrays.asList(queryColumns);
            if (!columnList.contains("_id")){
                columnList.add("_id");
                queryColumns = (String[]) columnList.toArray();
            }
        }
        return AutoDao.getInjector().selectSingle(distinct, clazz, queryColumns
                , mWhere.toString(), getArgments(), groupBy, having, orderBy, limit);
    }
}
