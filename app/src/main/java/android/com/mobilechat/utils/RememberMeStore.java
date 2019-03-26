package android.com.mobilechat.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

public class RememberMeStore {

    private static final String REMEMBERED_EMAIL = "email";
    private static final String REMEMBERED_PASSWORD = "password";
    private static final String IS_SELECTED = "is_selected";

    public static String getRememberedEmail(Context context) {
        String value = null;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (preferences != null) {
            value = preferences.getString(REMEMBERED_EMAIL, null);
        }
        return value;
    }

    public static void saveRememberedEmail(Context context, String value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (preferences != null && !TextUtils.isEmpty(REMEMBERED_EMAIL)) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(REMEMBERED_EMAIL, value);
            editor.apply();
        }
    }

    public static String getRememberedPassword(Context context) {
        String value = null;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (preferences != null) {
            value = preferences.getString(REMEMBERED_PASSWORD, null);
        }
        return value;
    }

    public static void saveRememberedPassword(Context context, String value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (preferences != null && !TextUtils.isEmpty(REMEMBERED_PASSWORD)) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(REMEMBERED_PASSWORD, value);
            editor.apply();
        }
    }

    public static boolean isCheckBoxSelected(Context context) {
        boolean value = false;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (preferences != null) {
            value = preferences.getBoolean(IS_SELECTED, false);
        }
        return value;
    }

    public static void saveCheckBoxSelection(Context context, Boolean value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (preferences != null && !TextUtils.isEmpty(IS_SELECTED)) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(IS_SELECTED, value);
            editor.apply();
        }
    }

}
