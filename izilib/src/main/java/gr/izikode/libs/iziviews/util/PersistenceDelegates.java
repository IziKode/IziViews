package gr.izikode.libs.iziviews.util;

/**
 * Created by UserOne on 11/11/2016.
 */

public interface PersistenceDelegates<T> {
    void preSubmerged(T value);
    void postSurfaced(T value);
}
