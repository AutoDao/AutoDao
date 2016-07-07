package autodao;

public interface TypeSerializer {

    Object serialize(Object data);

    Object deserialize(Object data);
}