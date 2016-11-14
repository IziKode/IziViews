package gr.izikode.libs.iziviews.util;

/**
 * Created by izi.
 */

public interface PersistenceDelegates<T> {
    void preSubmerged(T value);
    void postSurfaced(T value);
}
