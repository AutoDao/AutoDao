package autodao;

public abstract class TypeSerializer {

    public abstract Object serialize(Object data);

    public abstract Object deserialize(Object data);
}