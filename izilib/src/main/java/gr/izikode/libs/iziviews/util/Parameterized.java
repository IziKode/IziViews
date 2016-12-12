package gr.izikode.libs.iziviews.util;

import android.os.Bundle;

import java.lang.reflect.Type;

import gr.izikode.libs.iziviews.view.IziFragment;

/**
 * Created by UserOne on 12/12/2016.
 */

public abstract class Parameterized<F extends IziFragment> implements InitializationDelegates{
    private final Class<F> type;

    public Parameterized(Class<F> parameterisedType) {
        type = parameterisedType;
    }

    public F getFragment() throws IllegalAccessException, InstantiationException {
        F fragment = type.newInstance();

        Bundle params = new Bundle();
        setParameters(params);
        fragment.setArguments(params);

        return fragment;
    }
}
