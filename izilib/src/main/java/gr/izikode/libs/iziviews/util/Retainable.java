package gr.izikode.libs.iziviews.util;

import java.io.Serializable;

/**
 * Created by izi.
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
