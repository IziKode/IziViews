package gr.izikode.libs.iziviews.util;

import android.support.annotation.NonNull;

/**
 * Created by izi.
 */

public interface PersistenceDelegates<T> {
    void preSubmerged(@NonNull T value);
    void postSurfaced(@NonNull T value);
}
