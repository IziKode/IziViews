package gr.izikode.libs.iziviews.view;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.AnimRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import gr.izikode.libs.iziviews.R;
import gr.izikode.libs.iziviews.annotation.IgnoreForInstanceRestoration;
import gr.izikode.libs.iziviews.annotation.InstanceRestoration;
import gr.izikode.libs.iziviews.annotation.SaveForInstanceRestoration;
import gr.izikode.libs.iziviews.util.ExtendedReflector;
import gr.izikode.libs.iziviews.util.ObjectSerializer;
import gr.izikode.libs.iziviews.util.Retainable;

/**
 * Created by izi.
 */

@InstanceRestoration
public abstract class IziFragment extends Fragment implements LifecycleDelegates<IziFragment>, Serializable {
    private static final String SAVED_INSTANCE_BUNDLE_KEY = "IziFragment_savedInstance_bundleKey";

    public final String tag;

    public IziFragment() {
        super();
        tag = UUID.nameUUIDFromBytes(this.getClass().getName().getBytes()).toString();
    }

    private boolean initialized;
    private View rootView;

    protected View getRootView() {
        return rootView;
    }

    public boolean isStackable() {
        return true;
    }

    protected abstract int getContentResource();

    protected int getBackgroundColor() {
        return getContext().getResources().getColor(R.color.colorGenericBackground);
    }

    /**
     * Sets the naming policy for the ids of the View objects.
     * The usable String Format parts are the following,
     * %1$s : Class name
     * %2$s : Field name
     * %3$s : Field type name
     * @return The Format String for the naming policy of the ids of the Views.
     */
    protected String getViewNameFormat() {
        return "%1$s_%2$s";
    }

    public IziActivity getParent() {
        Activity activity = getActivity();
        if (activity instanceof IziActivity) {
            return (IziActivity) activity;
        }

        return null;
    }

    public View findViewById(int viewId) {
        if (rootView != null) {
            return rootView.findViewById(viewId);
        }

        return null;
    }

    public MenuInflater getMenuInflater() {
        IziActivity parent = getParent();
        if (parent != null) {
            return parent.getMenuInflater();
        }

        return null;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    public void invalidateOptionsMenu() {
        IziActivity parent = getParent();
        if (parent != null) {
            parent.invalidateOptionsMenu();
            parent.supportInvalidateOptionsMenu();
        }
    }

    public void setSupportActionBar(Toolbar actionBar) {
        IziActivity parent = getParent();
        if (parent != null) {
            parent.setSupportActionBar(actionBar);
        }
    }

    protected void onBackPressed() {
        IziActivity parent = getParent();
        if (parent != null) {
            parent.onBackPressed();
        }
    }

    @Override
    public void onViewLoading() {
        for (Field field : ExtendedReflector.getDeclaredFieldsByType(this, View.class)) {
            String resource = String.format(
                getViewNameFormat(),
                    field.getDeclaringClass().getSimpleName(),
                    field.getName(),
                    field.getType().getSimpleName()
            ).toLowerCase(Locale.getDefault());

            int id = getActivity().getResources().getIdentifier(
                    resource, "id",
                    getActivity().getApplicationContext().getPackageName()
            );

            if (id != 0) {
                try {
                    ExtendedReflector.setValue(this, field, findViewById(id));
                } catch (ReflectiveOperationException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void preShown() {}

    @Override
    public void preConcealed() {}

    @Override
    public void preDismissed() {}

    @Deprecated
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        initialized = savedInstanceState != null;

        if (rootView == null) {
            rootView = inflater.inflate(getContentResource(), container, false);

            rootView.getRootView().setClickable(true);
            rootView.getRootView().setBackgroundColor(getBackgroundColor());

            onViewLoading();
            onCreation();
        }

        getParent().updatePersistence(this);

        if (savedInstanceState == null) {
            onInitialization();
        }

        return rootView;
    }

    @Deprecated
    @Override
    public void onResume() {
        preShown();
        super.onResume();
        invalidateOptionsMenu();
        postShown();

        if (initialized) {
            surfaceRetainables();
        }
    }

    @Deprecated
    @Override
    public void onPause() {
        preConcealed();
        submergeRetainables();
        super.onPause();
        postConcealed();
    }

    @Deprecated
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        InstanceRestoration restorationAnnotation = this.getClass().getAnnotation(InstanceRestoration.class);
        Field[] fields = null;

        if (restorationAnnotation.policy() == InstanceRestoration.RestorationPolicy.IGNORE_ALL_FIELDS) {
            fields = ExtendedReflector.getAnnotatedFields(this, SaveForInstanceRestoration.class);
        } else {
            fields = ExtendedReflector.getNonAnnotatedFields(this, IgnoreForInstanceRestoration.class);
        }

        if (fields != null && fields.length > 0) {
            HashMap<String, Serializable> outInstanceMap = new HashMap<>();

            for (Field field : fields) {
                try {
                    Object object = ExtendedReflector.readValue(this, field);

                    if (ObjectSerializer.isBoxable(object)) {
                        outInstanceMap.put(field.getName(), ObjectSerializer.box(object));
                    } else {
                        getParent().holdObject(this, field);
                    }
                } catch (ReflectiveOperationException e) {
                    e.printStackTrace();
                }
            }

            if (outInstanceMap.size() > 0) {
                outState.putSerializable(SAVED_INSTANCE_BUNDLE_KEY, outInstanceMap);
            }
        }
    }

    @Deprecated
    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        Object savedInstance = null;

        if (savedInstanceState != null && savedInstanceState.containsKey(SAVED_INSTANCE_BUNDLE_KEY)) {
            HashMap<String, Serializable> savedInstanceMap =
                    (HashMap<String, Serializable>) savedInstanceState.getSerializable(SAVED_INSTANCE_BUNDLE_KEY);

            if (savedInstanceMap != null) {
                try {
                    savedInstance = this.getClass().newInstance();
                } catch (java.lang.InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                    return;
                }

                for (Map.Entry<String, Serializable> entry : savedInstanceMap.entrySet()) {
                    String name = entry.getKey();
                    Object value = ObjectSerializer.unbox(entry.getValue());

                    try {
                        Field field = savedInstance.getClass().getDeclaredField(name);
                        ExtendedReflector.setValue(savedInstance, field, value);
                    } catch (ReflectiveOperationException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        HashMap<String, Object> objectMap = getParent().getPersistentObjectMap(this);
        if (objectMap != null && objectMap.size() > 0) {
            if (savedInstance == null) {
                try {
                    savedInstance = this.getClass().newInstance();
                } catch (java.lang.InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                    return;
                }
            }

            for (Map.Entry<String, Object> entry : objectMap.entrySet()) {
                String name = entry.getKey();
                Object value = entry.getValue();

                try {
                    Field field = savedInstance.getClass().getDeclaredField(name);
                    ExtendedReflector.setValue(savedInstance, field, value);
                } catch (ReflectiveOperationException e) {
                    e.printStackTrace();
                }
            }
        }

        HashMap<String, Retainable> retainableMap = getParent().getRetainableObjectMap(this);
        if (retainableMap != null && retainableMap.size() > 0) {
            for (Field field : ExtendedReflector.getDeclaredFieldsByType(this, Retainable.class)) {
                try {
                    Retainable retainable = (Retainable) ExtendedReflector.readValue(this, field);
                    Retainable retained = retainableMap.get(field.getName());

                    if (retainable != null && retained != null && retained.getValue() != null) {
                        retainable.setValue(retained.getValue());
                    }
                } catch (ReflectiveOperationException e) {
                    e.printStackTrace();
                }
            }
        }

        if (savedInstance != null) {
            onRestoration((IziFragment) savedInstance);
        }
    }

    public boolean isTopmost() {
        IziActivity parent = getParent();
        if (parent != null) {
            return equals(parent.getTopmostFragment());
        }

        return false;
    }

    public @AnimRes int getEnterAnimation() {
        return R.anim.izi_fadein_anim;
    }

    public @AnimRes int getExitAnimation() {
        return R.anim.izi_fadeout_anim;
    }

    private void submergeRetainables() {
        for (Field field : ExtendedReflector.getDeclaredFieldsByType(this, Retainable.class)) {
            try {
                Retainable retainable = (Retainable) ExtendedReflector.readValue(this, field);
                if (retainable != null  && retainable.getValue() != null) {
                    retainable.preSubmerged(retainable.getValue());
                }
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
            }
        }
    }

    private void surfaceRetainables() {
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

    protected int getColor(int resourceId) {
        return getContext().getResources().getColor(resourceId);
    }
}
