/*
 * Copyright (C) 2013 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.namelessrom.setupwizard;


import android.app.Application;
import android.app.StatusBarManager;
import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.content.pm.PackageManager;
import android.provider.Settings;

import com.cyanogenmod.setupwizard.util.SetupWizardUtils;

public class SetupWizardApp extends Application {

    public static final String TAG = SetupWizardApp.class.getSimpleName();

    private static final String PROP_DEBUG = "ro.nameless.debug";

    public static final boolean DEBUG = SystemProperties.getBoolean(PROP_DEBUG, false);

    public static final String ACTION_FINISHED = "com.cyanogenmod.setupwizard.SETUP_FINISHED";

    public static final String ACCOUNT_TYPE_GMS = "com.google";

    public static final String ACTION_SETUP_WIFI = "com.android.net.wifi.SETUP_WIFI_NETWORK";

    public static final String EXTRA_FIRST_RUN = "firstRun";
    public static final String EXTRA_ALLOW_SKIP = "allowSkip";
    public static final String EXTRA_AUTO_FINISH = "wifi_auto_finish_on_connect";
    public static final String EXTRA_SHOW_BUTTON_BAR = "extra_prefs_show_button_bar";
    public static final String EXTRA_USE_IMMERSIVE = "useImmersiveMode";
    public static final String EXTRA_THEME = "theme";
    public static final String EXTRA_MATERIAL_LIGHT = "material_light";

    private static final String[] THEME_PACKAGES = {
            "org.cyanogenmod.theme.chooser",
            "com.cyngn.theme.chooser",
            "com.cyngn.themestore"
    };

    public static final int REQUEST_CODE_SETUP_WIFI = 0;
    public static final int REQUEST_CODE_SETUP_GMS= 1;
    public static final int REQUEST_CODE_RESTORE_GMS= 2;
    public static final int REQUEST_CODE_SETUP_CYANOGEN= 3;
    public static final int REQUEST_CODE_SETUP_CAPTIVE_PORTAL= 4;
    public static final int REQUEST_CODE_SETUP_BLUETOOTH= 5;

    private StatusBarManager mStatusBarManager;

    private static SetupWizardApp sInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        mStatusBarManager = (StatusBarManager)getSystemService(Context.STATUS_BAR_SERVICE);
        try {
            // Since this is a new component, we need to disable here if the user
            // has already been through setup on a previous version.
            final boolean isOwner = SetupWizardUtils.isOwner();
            if (!isOwner
                    || Settings.Secure.getInt(getContentResolver(),
                    Settings.Secure.USER_SETUP_COMPLETE) == 1) {
                SetupWizardUtils.disableGMSSetupWizard(this);
                SetupWizardUtils.disableSetupWizard(this);
                if (!isOwner) {
                    disableThemeComponentsForSecondaryUser();
                }
            }  else {
                disableCaptivePortalDetection();
            }
        } catch (Settings.SettingNotFoundException e) {
            // Continue with setup
            disableCaptivePortalDetection();
        }
    }

    public static SetupWizardApp get() {
        return sInstance;
    }

    public void disableStatusBar() {
        mStatusBarManager.disable(StatusBarManager.DISABLE_EXPAND | StatusBarManager.DISABLE_NOTIFICATION_ALERTS
                | StatusBarManager.DISABLE_NOTIFICATION_TICKER | StatusBarManager.DISABLE_RECENT | StatusBarManager.DISABLE_HOME
                | StatusBarManager.DISABLE_SEARCH);
    }

    public void enableStatusBar() {
        mStatusBarManager.disable(StatusBarManager.DISABLE_NONE);
    }

    public void disableCaptivePortalDetection() {
        Settings.Global.putInt(getContentResolver(),
                Settings.Global.CAPTIVE_PORTAL_DETECTION_ENABLED, 0);
    }

    public void enableCaptivePortalDetection() {
        Settings.Global.putInt(getContentResolver(),
                Settings.Global.CAPTIVE_PORTAL_DETECTION_ENABLED, 1);
    }

    private void disableThemeComponentsForSecondaryUser() {
        PackageManager pm = getPackageManager();
        for(String pkgName : THEME_PACKAGES) {
            try {
                pm.getApplicationInfo(pkgName, 0);
                pm.setApplicationEnabledSetting(pkgName,
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER, 0);
            } catch (PackageManager.NameNotFoundException e) {
                // don't care
            }
        }
    }
}
