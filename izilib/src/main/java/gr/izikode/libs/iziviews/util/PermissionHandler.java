package gr.izikode.libs.iziviews.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by UserOne on 28/12/2016.
 */

public class PermissionHandler {
    private Context context;

    public boolean shouldRequest() {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M) {
            return false;
        }

        String[] permissions = getPermissions();
        if (permissions != null) {
            for (String permission : permissions) {
                if (Permissions.isDangerous(permission) || Permissions.isSensitive(permission)) {
                    if (!isGranted(permission)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public String[] getPermissions() {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS);
            return packageInfo.requestedPermissions;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String[] getUngrantedPermissions() {
        String[] permissions = getPermissions();
        List<String> ungrantedPermissions = new ArrayList<>();

        for (String permission : permissions) {
            if (!isGranted(permission)) {
                ungrantedPermissions.add(permission);
            }
        }

        return ungrantedPermissions.toArray(new String[ungrantedPermissions.size()]);
    }

    public boolean isGranted(String permission) {
        if (Permissions.isDangerous(permission)) {
            int check = ContextCompat.checkSelfPermission(context, permission);
            return check == PackageManager.PERMISSION_GRANTED;
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Permissions.Sensitive.SYSTEM_ALERT_WINDOW.equals(permission)) {
                    return Settings.canDrawOverlays(context);
                } else {
                    return Settings.System.canWrite(context);
                }
            } else {
                return true;
            }
        }
    }
}
