package autodao;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by tubingbing on 16/5/31.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
public @interface Column {

    /**
     * column name
     * @return
     */
    String name() default "";

    /**
     * UNIQUE CONSTRAINT
     * @return
     */
    boolean unique() default false;

    /**
     * NOT NULL CONSTRAINT
     * @return
     */
    boolean notNULL() default false;

    /**
     * DEFAULT CONSTRAINT
     * @return
     */
    String defaultValue() default "";

    /**
     * CHECK CONSTRAINT
     * @return
     */
    String check() default "";

    /**
     * ignore the column
     * @return
     */
    boolean ignore() default false;

    String mappingColumnName() default "";
}