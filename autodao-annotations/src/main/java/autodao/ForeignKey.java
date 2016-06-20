package autodao;

/**
 * Created by tubingbing on 16/6/3.
 */
public @interface ForeignKey {

    String referenceTableName();

    /**
     * reference column must unique
     * @return
     */
    String referenceColumnName() default "_id";

    String action() default "";
}
