package gr.izikode.libs.iziviews.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.RequiresApi;

/**
 * Created by UserOne on 28/12/2016.
 */

public class Permissions {
    private static final String PREFIX = "android.permission.";

    public static boolean isDangerous(String permission) {
        for (Dangerous dangerousPermission : Dangerous.values()) {
            if (dangerousPermission.equals(permission)) {
                return true;
            }
        }

        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static boolean isSensitive(String permission) {
        for (Sensitive sensitivePermission : Sensitive.values()) {
            if (sensitivePermission.equals(permission)) {
                return true;
            }
        }

        return false;
    }

    public enum Dangerous {
        READ_CALENDAR("READ_CALENDAR"), WRITE_CALENDAR("WRITE_CALENDAR"),
        CAMERA("CAMERA"),
        READ_CONTACTS("READ_CONTACTS"), WRITE_CONTACTS("WRITE_CONTACTS"), GET_ACCOUNTS("GET_ACCOUNTS"),
        ACCESS_FINE_LOCATION("ACCESS_FINE_LOCATION"), ACCESS_COARSE_LOCATION("ACCESS_COARSE_LOCATION"),
        RECORD_AUDIO("RECORD_AUDIO"),
        READ_PHONE_STATE("READ_PHONE_STATE"), CALL_PHONE("CALL_PHONE"), READ_CALL_LOG("READ_CALL_LOG"), WRITE_CALL_LOG("WRITE_CALL_LOG"),
        ADD_VOICEMAIL("ADD_VOICEMAIL"), USE_SIP("USE_SIP"), PROCESS_OUTGOING_CALLS("PROCESS_OUTGOING_CALLS"),
        BODY_SENSORS("BODY_SENSORS"),
        SEND_SMS("SEND_SMS"), RECEIVE_SMS("RECEIVE_SMS"), READ_SMS("READ_SMS"),
        RECEIVE_WAP_PUSH("RECEIVE_WAP_PUSH"), RECEIVE_MMS("RECEIVE_MMS"),
        READ_EXTERNAL_STORAGE("READ_EXTERNAL_STORAGE"), WRITE_EXTERNAL_STORAGE("WRITE_EXTERNAL_STORAGE");

        final String value;
        Dangerous(String permissionValue) {
            value = permissionValue;
        }

        boolean equals(String permission) {
            return (PREFIX + value).equals(permission);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public enum Sensitive {
        SYSTEM_ALERT_WINDOW("SYSTEM_ALERT_WINDOW", Settings.ACTION_MANAGE_OVERLAY_PERMISSION),
        WRITE_SETTINGS("WRITE_SETTINGS", Settings.ACTION_MANAGE_WRITE_SETTINGS);

        final String value;
        private final String intent;
        Sensitive(String permissionValue, String permissionIntent) {
            value = permissionValue;
            intent = permissionIntent;
        }

        boolean equals(String permission) {
            return (PREFIX + value).equals(permission);
        }

        Intent getIntent(Context context) {
            Uri intentUri = Uri.parse("package:" + context.getPackageName());
            return new Intent(intent, intentUri);
        }
    }
}
