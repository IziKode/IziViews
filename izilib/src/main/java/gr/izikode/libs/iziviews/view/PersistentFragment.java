package gr.izikode.libs.iziviews.view;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import gr.izikode.libs.iziviews.util.ExtendedReflector;
import gr.izikode.libs.iziviews.util.Retainable;

/**
 * Created by izi.
 */

@SuppressLint("ValidFragment")
public class PersistentFragment extends Fragment {
    public static final String TAG = UUID.nameUUIDFromBytes(PersistentFragment.class.getName().getBytes()).toString();

    private HashMap<String, Retainable> activityRetainables;
    private HashMap<String, Object> activityNonSerializables;

    private HashMap<String, HashMap<String, Retainable>> fragmentRetainables;
    private HashMap<String, HashMap<String, Object>> fragmentNonSerializables;

    public PersistentFragment() {
        super();

        activityRetainables = new HashMap<>();
        activityNonSerializables = new HashMap<>();

        fragmentRetainables = new HashMap<>();
        fragmentNonSerializables = new HashMap<>();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public boolean containsRetainables() {
        return activityRetainables.size() > 0;
    }

    public boolean containsRetainables(IziFragment fragment) {
        return fragmentRetainables.containsKey(fragment.tag) &&
                ((HashMap)fragmentRetainables.get(fragment.tag)).size() > 0;
    }

    public void appendRetainables() {
        appendRetainableFieldsToHashmap(getActivity(), activityRetainables);
    }

    public void appendRetainables(IziFragment fragment) {
        HashMap<String, Retainable> retainables = null;

        if (fragmentRetainables.containsKey(fragment.tag)) {
            retainables = fragmentRetainables.get(fragment.tag);
        } else {
            retainables = new HashMap<>();
            fragmentRetainables.put(fragment.tag, retainables);
        }

        appendRetainableFieldsToHashmap(fragment, retainables);
    }

    private void appendRetainableFieldsToHashmap(Object instance, HashMap<String, Retainable> hashMap) {
        for (Field field : instance.getClass().getDeclaredFields()) {
            if (Retainable.class.isAssignableFrom(field.getType())) {
                try {
                    hashMap.put(field.getName(), (Retainable) ExtendedReflector.readValue(instance, field));
                } catch (ReflectiveOperationException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void removeRetainables() {
        activityRetainables.clear();
    }

    public void removeRetainables(IziFragment fragment) {
        if (fragmentRetainables.containsKey(fragment.tag)) {
            ((HashMap) fragmentRetainables.get(fragment.tag)).clear();
            fragmentRetainables.remove(fragment.tag);
        }
    }

    public void clearAll() {
        activityRetainables.clear();

        for (HashMap map : fragmentRetainables.values()) {
            map.clear();
        }

        fragmentRetainables.clear();
    }

    public void notifyAttached() {
        notifyRetainablesAttached(getActivity(), activityRetainables);
    }

    public void notifyAttached(IziFragment fragment) {
        if (fragmentRetainables.containsKey(fragment.tag)) {
            HashMap<String, Retainable> retainables = fragmentRetainables.get(fragment.tag);
            notifyRetainablesAttached(fragment, retainables);
        }
    }

    private void notifyRetainablesAttached(Object instance, HashMap<String, Retainable> retainables) {
        for (Map.Entry<String, Retainable> retainable : retainables.entrySet()) {
            String name = retainable.getKey();
            Retainable variable = retainable.getValue();

            try {
                Field field = instance.getClass().getDeclaredField(name);
                Retainable obj = (Retainable) ExtendedReflector.readValue(instance, field);

                obj.setValue(variable.getValue());

                boolean visible = true;
                if (instance instanceof IziFragment) {
                    visible = ((IziFragment) instance).isTopmost();
                }

                obj.postSurfaced(obj.getValue());
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
            }
        }
    }

    public void holdObject(Field field) {
        holdNonSerializableObject(getActivity(), activityNonSerializables, field);
    }

    public void holdObject(IziFragment fragment, Field field) {
        HashMap<String, Object> nonSerializables = null;

        if (fragmentNonSerializables.containsKey(fragment.tag)) {
            nonSerializables = fragmentNonSerializables.get(fragment.tag);
        } else {
            nonSerializables = new HashMap<>();
            fragmentNonSerializables.put(fragment.tag, nonSerializables);
        }

        holdNonSerializableObject(fragment, nonSerializables, field);
    }

    private void holdNonSerializableObject(Object instance, HashMap<String, Object> hashMap, Field field) {
        try {
            Object object = handleObject(ExtendedReflector.readValue(instance, field));
            hashMap.put(field.getName(), object);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    private Object handleObject(Object object) {
        if (object instanceof View) {
            try {
                Method internalClone = ExtendedReflector.locateMethodInSuperclass(object, Object.class, "internalClone");
                Object cloneObject = ExtendedReflector.getValue(object, internalClone);

                if (cloneObject != null) {
                    Field mContext = ExtendedReflector.locateFieldInSuperclass(cloneObject, View.class, "mContext");
                    ExtendedReflector.setValue(cloneObject, mContext, null);

                    return cloneObject;
                }
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
            }
        }

        return object;
    }

    public HashMap<String, Object> getObjectMap() {
        return activityNonSerializables;
    }

    public HashMap<String, Object> getObjectMap(IziFragment fragment) {
        if (fragmentNonSerializables.containsKey(fragment.tag)) {
            return fragmentNonSerializables.get(fragment.tag);
        }

        return null;
    }

    public HashMap<String, Retainable> getRetainableMap() {
        return activityRetainables;
    }

    public HashMap<String, Retainable> getRetainableMap(IziFragment fragment) {
        if (fragmentRetainables.containsKey(fragment.tag)) {
            return fragmentRetainables.get(fragment.tag);
        }

        return null;
    }
}
