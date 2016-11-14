package gr.izikode.libs.iziviews.util;

import java.io.Serializable;

/**
 * Created by UserOne on 11/11/2016.
 */

public abstract class Retainable<T> implements PersistenceDelegates<T>, Serializable {
    private T persistentValue;

    public T getValue() {
        return persistentValue;
    }

    public void setValue(T value) {
        persistentValue = value;
    }
}
