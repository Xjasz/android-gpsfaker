package com.delvedapps.core.util.root;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.delvedapps.core.Singleton;
import com.delvedapps.core.util.contstant.Constants;

import java.util.List;

public class RootCheck {
    private static final String TAG = RootCheck.class.getSimpleName();

    public static boolean checkForSystemApp() {
        List<ApplicationInfo> packages = Singleton.getAppContext().getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);

        Log.d(TAG, "PACKAGE_NAME " + Singleton.PACKAGE_NAME);
        Log.d(TAG, "packages size = " + packages.size());
        for (ApplicationInfo packageInfo : packages) {
            if (Singleton.PACKAGE_NAME.equals(packageInfo.packageName)) {
                Log.d(TAG, "Source dir : " + packageInfo.sourceDir);
                return packageInfo.sourceDir.contains(Constants.SYSTEM_DIRECTORY);
            }
        }
        return false;
    }
}
