package gr.izikode.libs.iziviews.view;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import gr.izikode.libs.iziviews.annotation.IgnoreForInstanceRestoration;
import gr.izikode.libs.iziviews.annotation.InstanceRestoration;
import gr.izikode.libs.iziviews.annotation.SaveForInstanceRestoration;
import gr.izikode.libs.iziviews.util.ExtendedReflector;
import gr.izikode.libs.iziviews.util.ObjectSerializer;
import gr.izikode.libs.iziviews.util.Parameterized;
import gr.izikode.libs.iziviews.util.Retainable;

/**
 * Created by izi.
 */

@InstanceRestoration
public abstract class IziActivity extends AppCompatActivity implements LifecycleDelegates<IziActivity>, Serializable {
    private static final String SAVED_INSTANCE_BUNDLE_KEY = "IziActivity_savedInstance_bundleKey";
    private static final String FRAGMENT_STACK_COUNT_INSTANCE_BUNDLE_KEY = "IziActivity_fragmentStackCount_bundleKey";

    private boolean initialized;

    private FragmentManager fragmentManager;
    private List<Fragment> currentFragments;

    private View fragmentContainer;
    private int fragmentStackCount;

    private PersistentFragment persistentFragment;

    private List<IziFragment> pendingPersistency;
    private HashMap<IziFragment, Field> pendingObjects;

    public IziActivity() {
        super();
        pendingPersistency = new ArrayList<>();
        pendingObjects = new HashMap<>();
    }

    protected abstract int getContentResource();
    protected abstract int getFragmentContainerId();

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

    @Override
    public void preShown() {}

    @Override
    public void preConcealed() {}

    @Override
    public void preDismissed() {}

    @Deprecated
    @Override
    public void onResume() {
        preShown();
        super.onResume();
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

    @Override
    public void onViewLoading() {
        for (Field field : ExtendedReflector.getDeclaredFieldsByType(this, View.class)) {
            String resource = String.format(
                getViewNameFormat(),
                    field.getDeclaringClass().getSimpleName(),
                    field.getName(),
                    field.getType().getClass().getSimpleName()
            ).toLowerCase(Locale.getDefault());

            int id = getResources().getIdentifier(
                    resource, "id",
                    getApplicationContext().getPackageName()
            );

            if (id != 0) {
                try {
                    ExtendedReflector.setValue(this, field, field.getType().cast(findViewById(id)));
                } catch (ReflectiveOperationException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void addFragment(IziFragment fragment) {
        boolean addToBackStack = false;

        IziFragment topmost = getTopmostFragment();
        if (topmost != null && topmost.isStackable() && fragmentStackCount > 0) {
            addToBackStack = true;
        } else if (topmost != null && !topmost.isStackable()) {
            persistentFragment.removeRetainables(topmost);
        }

        FragmentTransaction transaction = fragmentManager.beginTransaction();

        if (topmost != null && topmost.isStackable()) {
            transaction.setCustomAnimations(fragment.getEnterAnimation(), fragment.getExitAnimation(),
                    topmost.getEnterAnimation(), topmost.getExitAnimation());
        } else {
            transaction.setCustomAnimations(fragment.getEnterAnimation(), fragment.getExitAnimation());
        }

        transaction.add(fragmentContainer.getId(), fragment, fragment.tag);
        currentFragments.add(fragment);

        if (addToBackStack) {
            transaction.addToBackStack(topmost.tag);
        }

        if (fragment.isStackable()) {
            fragmentStackCount = (fragmentStackCount >= 0 ? fragmentStackCount : 0) + 1;
        }

        transaction.commit();

        if (topmost != null) {
            topmost.onPause();
        }
    }

    public void addFragment(Parameterized fragment) {
        try {
            addFragment(fragment.getFragment());
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    public void addSoleFragment(IziFragment fragment) {
        clearFragments();
        addFragment(fragment);
    }

    public void addSoleFragment(Parameterized fragment) {
        try {
            addSoleFragment(fragment.getFragment());
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    public IziFragment getFragment(String tag) {
        Fragment fragment = fragmentManager.findFragmentByTag(tag);
        if (fragment != null && fragment instanceof IziFragment) {
            return (IziFragment) fragment;
        }

        return null;
    }

    public IziFragment getTopmostFragment() {
        Fragment fragment = fragmentManager.findFragmentById(fragmentContainer.getId());
        if (fragment != null && fragment instanceof IziFragment) {
            return (IziFragment) fragment;
        }

        return null;
    }

    public void clearFragments() {
        for (Fragment fragment : fragmentManager.getFragments()) {
            fragmentManager.beginTransaction().remove(fragment).commit();
        }

        currentFragments.clear();
        persistentFragment.clearAll();

        fragmentStackCount = 0;
    }

    @Override
    public void onBackPressed() {
        IziFragment pop = getTopmostFragment();

        if (pop != null) {
            pop.preDismissed();
            persistentFragment.removeRetainables(pop);

            if (currentFragments.contains(pop)) {
                currentFragments.remove(pop);
            }
        }

        super.onBackPressed();
        fragmentStackCount--;

        IziFragment top = getTopmostFragment();
        if (top != null) {
            if (top.isStackable()) {
                top.onResume();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        IziFragment topmost = getTopmostFragment();
        if (topmost != null) {
            topmost.onCreateOptionsMenu(menu);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        IziFragment topmost = getTopmostFragment();
        if (topmost != null) {
            topmost.onOptionsItemSelected(item);
        }

        return super.onOptionsItemSelected(item);
    }

    public String getVersionCode() {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            return String.format("%1$s (%2$s)", packageInfo.versionName, packageInfo.versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "-";
        }
    }

    @Deprecated
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        initialized = savedInstanceState != null;

        super.onCreate(savedInstanceState);
        setContentView(getContentResource());

        fragmentManager = getSupportFragmentManager();
        currentFragments = new ArrayList<>();

        fragmentContainer = findViewById(getFragmentContainerId());
        persistentFragment = (PersistentFragment) fragmentManager.findFragmentByTag(PersistentFragment.TAG);

        if (persistentFragment == null) {
            persistentFragment = new PersistentFragment();
            fragmentManager.beginTransaction().add(persistentFragment, PersistentFragment.TAG).commit();
        }

        onViewLoading();
        onCreation();

        if (savedInstanceState == null) {
            fragmentStackCount = 0;
            onInitialization();
        }
    }

    @Deprecated
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(FRAGMENT_STACK_COUNT_INSTANCE_BUNDLE_KEY, fragmentStackCount);

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
                        persistentFragment.holdObject(field);
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
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        Object savedInstance = null;

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(FRAGMENT_STACK_COUNT_INSTANCE_BUNDLE_KEY)) {
                fragmentStackCount = savedInstanceState.getInt(FRAGMENT_STACK_COUNT_INSTANCE_BUNDLE_KEY);
            }

            if (savedInstanceState.containsKey(SAVED_INSTANCE_BUNDLE_KEY)) {
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
        }

        HashMap<String, Object> objectMap = persistentFragment.getObjectMap();
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

        if (savedInstance != null) {
            onRestoration((IziActivity) savedInstance);
        }
    }

    public void updatePersistence(IziFragment fragment) {
        pendingPersistency.add(fragment);

        if (persistentFragment != null) {
            for (IziFragment pending : pendingPersistency) {
                if (!persistentFragment.containsRetainables(pending)) {
                    persistentFragment.appendRetainables(pending);
                }
            }

            pendingPersistency.clear();
        }
    }

    public void holdObject(IziFragment fragment, Field field) {
        pendingObjects.put(fragment, field);

        if (persistentFragment != null) {
            for (Map.Entry<IziFragment, Field> pending : pendingObjects.entrySet()) {
                persistentFragment.holdObject(pending.getKey(), pending.getValue());
            }

            pendingObjects.clear();
        }
    }

    public HashMap<String, Object> getPersistentObjectMap(IziFragment fragment) {
        if (persistentFragment != null) {
            return persistentFragment.getObjectMap(fragment);
        }

        return null;
    }

    public HashMap<String, Retainable> getRetainableObjectMap(IziFragment fragment) {
        if (persistentFragment != null) {
            return persistentFragment.getRetainableMap(fragment);
        }

        return null;
    }

    private void submergeRetainables() {
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

    public void updateInsets(boolean fitsSystemWindows) {
        fragmentContainer.setFitsSystemWindows(fitsSystemWindows);
    }

    public void hideKeyboard() {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
        }
    }

    public String getLocale() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return getResources().getConfiguration().getLocales().get(0).getLanguage();
        } else {
            return getResources().getConfiguration().locale.getLanguage();
        }
    }
}