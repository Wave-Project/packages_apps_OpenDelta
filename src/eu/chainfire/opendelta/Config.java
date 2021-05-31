/*
 * Copyright (C) 2013-2014 Jorrit "Chainfire" Jongma
 * Copyright (C) 2013-2015 The OmniROM Project
 */
/*
 * This file is part of OpenDelta.
 *
 * OpenDelta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenDelta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenDelta. If not, see <http://www.gnu.org/licenses/>.
 */

package eu.chainfire.opendelta;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Environment;
import android.os.SystemProperties;
import android.preference.PreferenceManager;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class Config {
    private final static String PREF_SECURE_MODE_NAME = "secure_mode";
    private final static String PREF_SHOWN_RECOVERY_WARNING_SECURE_NAME = "shown_recovery_warning_secure";
    private final static String PREF_SHOWN_RECOVERY_WARNING_NOT_SECURE_NAME = "shown_recovery_warning_not_secure";
    private final static String PREF_SHOW_INFO_NAME = "show_info";
    private final static String PREF_AB_PERF_MODE_NAME = "ab_perf_mode";
    private final static boolean PREF_AB_PERF_MODE_DEFAULT = true;
    private static final String PROP_AB_DEVICE = "ro.build.ab_update";
    private static Config instance = null;
    private final SharedPreferences prefs;
    private final String propertyVersion;
    private final String propertyDevice;
    private final String filenameBase;
    private final String pathBase;
    private final String pathFlashAfterUpdate;
    private final String urlBaseDelta;
    private final String urlBaseUpdate;
    private final String urlBaseFull;
    private final String urlBaseFullSum;
    private final String urlBaseSuffix;
    private final boolean applySignature;
    private final boolean injectSignatureEnable;
    private final String injectSignatureKeys;
    private final boolean secureModeEnable;
    private final boolean secureModeDefault;
    private final boolean keepScreenOn;
    private final String filenameBasePrefix;
    private final String urlBaseJson;
    private final String officialVersionTag;
    private final String androidVersion;
    private final String weeklyVersionTag;
    private final String securityVersionTag;

    private Config(Context context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);

        Resources res = context.getResources();

        propertyVersion = getProperty(context,
                res.getString(R.string.property_version));
        propertyDevice = getProperty(context,
                res.getString(R.string.property_device));
        filenameBase = String.format(Locale.ENGLISH,
                res.getString(R.string.filename_base), propertyVersion);

        pathBase = String.format(Locale.ENGLISH, "%s%s%s%s", Environment
                        .getExternalStorageDirectory().getAbsolutePath(),
                File.separator, res.getString(R.string.path_base),
                File.separator);
        pathFlashAfterUpdate = String.format(Locale.ENGLISH, "%s%s%s",
                pathBase, "FlashAfterUpdate", File.separator);
        urlBaseDelta = String.format(Locale.ENGLISH,
                res.getString(R.string.url_base_delta), propertyDevice);
        urlBaseUpdate = String.format(Locale.ENGLISH,
                res.getString(R.string.url_base_update), propertyDevice);
        urlBaseFull = String.format(
                res.getString(R.string.url_base_full), propertyDevice);
        urlBaseFullSum = String.format(
                res.getString(R.string.url_base_full_sum), propertyDevice);
        urlBaseSuffix = res.getString(R.string.url_base_suffix);
        applySignature = res.getBoolean(R.bool.apply_signature);
        injectSignatureEnable = res
                .getBoolean(R.bool.inject_signature_enable);
        injectSignatureKeys = res.getString(R.string.inject_signature_keys);
        secureModeEnable = res.getBoolean(R.bool.secure_mode_enable);
        secureModeDefault = res.getBoolean(R.bool.secure_mode_default);
        urlBaseJson = String.format(
                res.getString(R.string.url_base_json),
                propertyDevice, propertyDevice);
        officialVersionTag = res.getString(R.string.official_version_tag);
        weeklyVersionTag = res.getString(R.string.weekly_version_tag);
        securityVersionTag = res.getString(R.string.security_version_tag);
        androidVersion = getProperty(context,
                res.getString(R.string.android_version));
        filenameBasePrefix = String.format(Locale.ENGLISH,
                res.getString(R.string.filename_base), androidVersion);
        boolean keep_screen_on = false;
        try {
            String[] devices = res.getStringArray(R.array.keep_screen_on_devices);
            if (devices != null) {
                for (String device : devices) {
                    if (propertyDevice != null && propertyDevice.equals(device)) {
                        keep_screen_on = true;
                        break;
                    }
                }
            }
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
        this.keepScreenOn = keep_screen_on;

        Logger.d("property_version: %s", propertyVersion);
        Logger.d("property_device: %s", propertyDevice);
        Logger.d("filename_base: %s", filenameBase);
        Logger.d("filename_base_prefix: %s", filenameBasePrefix);
        Logger.d("path_base: %s", pathBase);
        Logger.d("path_flash_after_update: %s", pathFlashAfterUpdate);
        Logger.d("url_base_delta: %s", urlBaseDelta);
        Logger.d("url_base_update: %s", urlBaseUpdate);
        Logger.d("url_base_full: %s", urlBaseFull);
        Logger.d("url_base_full_sum: %s", urlBaseFullSum);
        Logger.d("url_base_json: %s", urlBaseJson);
        Logger.d("apply_signature: %d", applySignature ? 1 : 0);
        Logger.d("inject_signature_enable: %d", injectSignatureEnable ? 1 : 0);
        Logger.d("inject_signature_keys: %s", injectSignatureKeys);
        Logger.d("secure_mode_enable: %d", secureModeEnable ? 1 : 0);
        Logger.d("secure_mode_default: %d", secureModeDefault ? 1 : 0);
        Logger.d("keep_screen_on: %d", keep_screen_on ? 1 : 0);
    }

    public static Config getInstance(Context context) {
        if (instance == null) {
            instance = new Config(context.getApplicationContext());
        }
        return instance;
    }

    public static boolean isABDevice() {
        return SystemProperties.getBoolean(PROP_AB_DEVICE, false);
    }

    /*
     * Using reflection voodoo instead calling the hidden class directly, to
     * dev/test outside of AOSP tree
     */
    private String getProperty(Context context, String key) {
        try {
            Class<?> SystemProperties = context.getClassLoader().loadClass(
                    "android.os.SystemProperties");
            Method get = SystemProperties.getMethod("get", String.class, String.class);
            return (String) get.invoke(null, new Object[]{key, ""});
        } catch (Exception e) {
            // A lot of voodoo could go wrong here, return failure instead of
            // crash
            Logger.ex(e);
        }
        return null;
    }

    public String getFilenameBase() {
        return filenameBase;
    }

    public String getPathBase() {
        return pathBase;
    }

    public String getPathFlashAfterUpdate() {
        return pathFlashAfterUpdate;
    }

    public String getUrlBaseDelta() {
        return urlBaseDelta;
    }

    public String getUrlBaseUpdate() {
        return urlBaseUpdate;
    }

    public String getUrlBaseFull() {
        return urlBaseFull;
    }

    public String getUrlBaseFullSum() {
        return urlBaseFullSum;
    }

    public String getUrlSuffix() {
        return urlBaseSuffix;
    }

    public boolean getApplySignature() {
        return applySignature;
    }

    public boolean getInjectSignatureEnable() {
        // If we have full secure mode, let signature depend on secure mode
        // setting. If not, let signature depend on config setting only

        if (getSecureModeEnable()) {
            return getSecureModeCurrent();
        } else {
            return injectSignatureEnable;
        }
    }

    public String getInjectSignatureKeys() {
        return injectSignatureKeys;
    }

    public boolean getSecureModeEnable() {
        return applySignature && injectSignatureEnable && secureModeEnable;
    }

    public boolean getSecureModeDefault() {
        return secureModeDefault && getSecureModeEnable();
    }

    public boolean getSecureModeCurrent() {
        return getSecureModeEnable()
                && prefs.getBoolean(PREF_SECURE_MODE_NAME,
                getSecureModeDefault());
    }

    public boolean setSecureModeCurrent(boolean enable) {
        prefs.edit().putBoolean(PREF_SECURE_MODE_NAME,
                getSecureModeEnable() && enable).apply();
        return getSecureModeCurrent();
    }

    public boolean getABPerfModeCurrent() {
        return prefs.getBoolean(PREF_AB_PERF_MODE_NAME, PREF_AB_PERF_MODE_DEFAULT);
    }

    public void setABPerfModeCurrent(boolean enable) {
        prefs.edit().putBoolean(PREF_AB_PERF_MODE_NAME, enable).apply();
    }

    public boolean getShowInfo() {
        return prefs.getBoolean(PREF_SHOW_INFO_NAME, true);
    }

    public void setShowInfo(boolean enable) {
        prefs.edit().putBoolean(PREF_SHOW_INFO_NAME, enable).apply();
    }

    public List<String> getFlashAfterUpdateZIPs() {
        List<String> extras = new ArrayList<>();

        File[] files = (new File(getPathFlashAfterUpdate())).listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.getName().toLowerCase(Locale.ENGLISH).endsWith(".zip")) {
                    String filename = f.getAbsolutePath();
                    if (filename.startsWith(getPathBase())) {
                        extras.add(filename);
                    }
                }
            }
            Collections.sort(extras);
        }

        return extras;
    }

    public boolean getShownRecoveryWarningSecure() {
        return prefs.getBoolean(PREF_SHOWN_RECOVERY_WARNING_SECURE_NAME, false);
    }

    public void setShownRecoveryWarningSecure() {
        prefs.edit().putBoolean(PREF_SHOWN_RECOVERY_WARNING_SECURE_NAME, true).apply();
    }

    public boolean getShownRecoveryWarningNotSecure() {
        return prefs.getBoolean(PREF_SHOWN_RECOVERY_WARNING_NOT_SECURE_NAME,
                false);
    }

    public void setShownRecoveryWarningNotSecure() {
        prefs.edit().putBoolean(PREF_SHOWN_RECOVERY_WARNING_NOT_SECURE_NAME, true).apply();
    }

    public boolean getKeepScreenOn() {
        return keepScreenOn;
    }

    public String getDevice() {
        return propertyDevice;
    }

    public String getVersion() {
        return propertyVersion;
    }

    public String getFileBaseNamePrefix() {
        return filenameBasePrefix;
    }

    public String getUrlBaseJson() {
        return urlBaseJson;
    }

    public boolean isOfficialVersion() {
        return getVersion().contains(officialVersionTag) ||
                getVersion().contains(weeklyVersionTag) ||
                getVersion().contains(securityVersionTag);
    }

    public String getAndroidVersion() {
        return androidVersion;
    }
}
