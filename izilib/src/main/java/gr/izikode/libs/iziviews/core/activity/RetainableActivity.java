package gr.izikode.libs.iziviews.core.activity;

import java.lang.reflect.Field;

import gr.izikode.libs.iziviews.util.ExtendedReflector;
import gr.izikode.libs.iziviews.util.Retainable;

/**
 * Created by UserOne on 12/10/2017.
 */

public abstract class RetainableActivity extends AutoViewActivity {

    protected void submergeRetainables() {
        for (Field field : ExtendedReflector.getDeclaredFieldsByType(this, Retainable.class)) {
            try {
                Retainable retainable = (Retainable) ExtendedReflector.readValue(this, field);
                if (retainable != null && retainable.getValue() != null) {
                    retainable.preSubmerged(retainable.getValue());
                }
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
            }
        }
    }

    protected void surfaceRetainables() {
        for (Field field : ExtendedReflector.getDeclaredFieldsByType(this, Retainable.class)) {
            try {
                Retainable retainable = (Retainable) ExtendedReflector.readValue(this, field);
                if (retainable != null && retainable.getValue() != null) {
                    retainable.postSurfaced(retainable.getValue());
                }
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
            }
        }
    }
}
