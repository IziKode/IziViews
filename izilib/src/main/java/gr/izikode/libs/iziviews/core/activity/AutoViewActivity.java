package gr.izikode.libs.iziviews.core.activity;

import android.view.View;

import java.lang.reflect.Field;
import java.util.Locale;

import gr.izikode.libs.iziviews.util.ExtendedReflector;

/**
 * Created by UserOne on 08/10/2017.
 */

public abstract class AutoViewActivity extends FragmentedActivity {
    private static final String DEFAULT_NAME_FORMAT = "%1$s_%2$s";

    /**
     * Sets the naming policy for the ids of the View objects.
     * The usable String Format parts are the following,
     * %1$s : Class name
     * %2$s : Field name
     * %3$s : Field type name
     * @return The Format String for the naming policy of the ids of the Views.
     */
    protected String getViewNameFormat() {
        return DEFAULT_NAME_FORMAT;
    }

    protected void onViewLoading() {
        for (Field field : ExtendedReflector.getDeclaredFieldsByType(this, View.class)) {
            String resource = String.format(getViewNameFormat(),
                    /* Activity name */ field.getDeclaringClass().getSimpleName(),
                    /* View name */ field.getName(),
                    /* View class name */field.getType().getClass().getSimpleName()
                ).toLowerCase(Locale.getDefault());

            int id = getResources().getIdentifier(resource, "id", getApplicationContext().getPackageName());
            if (id != 0) {
                try {
                    ExtendedReflector.setValue(this, field, field.getType().cast(findViewById(id)));
                } catch (ReflectiveOperationException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
