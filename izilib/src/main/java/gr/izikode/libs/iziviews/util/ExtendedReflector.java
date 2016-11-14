package gr.izikode.libs.iziviews.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by izi.
 */

public class ExtendedReflector {
    public static <R> Class getMatchingSuperclass(Object instance, Class<R> matchingClass) throws ReflectiveOperationException {
        Class objectClass = instance.getClass();
        while (objectClass != null && !matchingClass.equals(objectClass)) {
            objectClass = objectClass.getSuperclass();
        }

        if (objectClass != null) {
            return objectClass;
        } else {
            throw new ReflectiveOperationException(new NoClassDefFoundError("No matching superclass found."));
        }
    }

    public static <R> Field locateFieldInSuperclass(Object instance, Class<R> superClass, String fieldName) throws ReflectiveOperationException {
        Class superclass = getMatchingSuperclass(instance, superClass);
        Field field = superclass.getDeclaredField(fieldName);

        if (field != null) {
            return field;
        } else {
            throw new ReflectiveOperationException(new NoSuchFieldException("No matching field found."));
        }
    }

    public static <R> Method locateMethodInSuperclass(Object instance, Class<R> superType, String methodName) throws ReflectiveOperationException {
        Class superclass = getMatchingSuperclass(instance, superType);
        Method method = superclass.getDeclaredMethod(methodName);

        if (method != null) {
            return method;
        } else {
            throw new ReflectiveOperationException(new NoSuchFieldException("No matching method found."));
        }
    }

    public static void setValue(Object instance, Field field, Object value) throws ReflectiveOperationException {
        boolean originalAccessibility = field.isAccessible();
        field.setAccessible(true);

        try {
            field.set(instance, value != null ? field.getType().cast(value) : null);
        } catch (IllegalAccessException e) {
            throw new ReflectiveOperationException("Unable to manipulate field accessibility.", e);
        } finally {
            field.setAccessible(originalAccessibility);
        }
    }

    public static Object readValue(Object instance, Field field) throws ReflectiveOperationException {
        boolean originalAccessibility = field.isAccessible();
        field.setAccessible(true);

        try {
            Object value = field.get(instance);
            return value;
        } catch (IllegalAccessException e) {
            throw new ReflectiveOperationException("Unable to manipulate field accessibility.", e);
        } finally {
            field.setAccessible(originalAccessibility);
        }
    }

    public static Object getValue(Object object, Method method, Object... params) throws ReflectiveOperationException {
        boolean originalAccessibility = method.isAccessible();
        method.setAccessible(true);

        try {
            Object value = null;

            if (params != null && params.length > 0) {
                value = method.invoke(object, params);
            } else {
                value = method.invoke(object);
            }

            return value;
        } catch (InvocationTargetException e) {
            throw new ReflectiveOperationException("Unable to invoke selected method.", e);
        } finally {
            method.setAccessible(originalAccessibility);
        }
    }

    public static <R extends Annotation> Field[] getAnnotatedFields(Object instance, Class<R> annotationClass) {
        List<Field> fields = new ArrayList<>();

        for (Field field : instance.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(annotationClass)) {
                fields.add(field);
            }
        }

        return fields.toArray(new Field[fields.size()]);
    }

    public static <R extends Annotation> Field[] getNonAnnotatedFields(Object instance, Class<R> annotationClass) {
        List<Field> fields = new ArrayList<>();

        for (Field field : instance.getClass().getDeclaredFields()) {
            if (!field.isAnnotationPresent(annotationClass)) {
                fields.add(field);
            }
        }

        return fields.toArray(new Field[fields.size()]);
    }

    public static Field[] getDeclaredFieldsByType(Object instance, Class type) {
        List<Field> fields = new ArrayList<>();

        for (Field field : instance.getClass().getDeclaredFields()) {
            if (type.isAssignableFrom(field.getType())) {
                fields.add(field);
            }
        }

        return fields.toArray(new Field[fields.size()]);
    }
}
