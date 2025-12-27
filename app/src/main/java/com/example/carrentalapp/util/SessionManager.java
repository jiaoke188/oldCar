package com.example.carrentalapp.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "car_rental_session";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_ROLE = "role";
    private static final String KEY_DISPLAY_NAME = "display_name";

    private final SharedPreferences preferences;

    public SessionManager(Context context) {
        this.preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // 保存当前登录用户，方便多界面共享身份信息
    public void saveUser(long userId, String username, String role, String displayName) {
        preferences.edit()
                .putLong(KEY_USER_ID, userId)
                .putString(KEY_USERNAME, username)
                .putString(KEY_ROLE, role)
                .putString(KEY_DISPLAY_NAME, displayName)
                .apply();
    }

    public long getUserId() {
        return preferences.getLong(KEY_USER_ID, -1);
    }

    public String getUsername() {
        return preferences.getString(KEY_USERNAME, "");
    }

    public String getRole() {
        return preferences.getString(KEY_ROLE, "");
    }

    public String getDisplayName() {
        return preferences.getString(KEY_DISPLAY_NAME, "");
    }

    public boolean isLoggedIn() {
        return getUserId() > 0;
    }

    public void clear() {
        preferences.edit().clear().apply();
    }
}
