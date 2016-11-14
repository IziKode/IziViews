package gr.izikode.libs.iziviews.util;

import android.app.Fragment;
import android.content.Context;
import android.view.View;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Created by UserOne on 11/11/2016.
 */

public class ObjectSerializer {
    public static boolean isBoxable(Object value) {
        if (value instanceof View) { return false; }
        if (value instanceof Context) { return false; }
        if (value instanceof Fragment) { return false; }

        return value instanceof Serializable;
    }

    public static Serializable box(Object value) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutput objectOutput = null;

        try {
            objectOutput = new ObjectOutputStream(byteArrayOutputStream);
            objectOutput.writeObject(value);
            objectOutput.flush();

            byte[] yourBytes = byteArrayOutputStream.toByteArray();
            return (Serializable) yourBytes;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                byteArrayOutputStream.close();
            } catch (IOException ex) {}
        }

        return null;
    }

    public static Object unbox(Serializable value) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream((byte[]) value);
        ObjectInput objectInput = null;

        try {
            objectInput = new ObjectInputStream(byteArrayInputStream);
            Object o = objectInput.readObject();

            return o;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (objectInput != null) {
                    objectInput.close();
                }
            } catch (IOException ex) {}
        }

        return null;
    }
}
