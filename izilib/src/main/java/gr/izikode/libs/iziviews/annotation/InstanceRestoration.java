package gr.izikode.libs.iziviews.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by UserOne on 09/11/2016.
 */

@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface InstanceRestoration {
    RestorationPolicy policy() default RestorationPolicy.IGNORE_ALL_FIELDS;

    enum RestorationPolicy {
        IGNORE_ALL_FIELDS, SAVE_ALL_FIELDS
    }
}
