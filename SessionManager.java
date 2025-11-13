package com.evaluacion.condominios;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF = "condominios_session";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_DEPARTMENT_ID = "department_id";
    private static final String KEY_ROLE = "role";
    private static final String KEY_NAME = "name";

    public static void save(Context ctx, int userId, int departmentId, String role, String name) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        sp.edit()
                .putInt(KEY_USER_ID, userId)
                .putInt(KEY_DEPARTMENT_ID, departmentId)
                .putString(KEY_ROLE, role)
                .putString(KEY_NAME, name)
                .apply();
    }

    public static int getUserId(Context ctx) {
        return ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).getInt(KEY_USER_ID, -1);
    }

    public static int getDepartmentId(Context ctx) {
        return ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).getInt(KEY_DEPARTMENT_ID, -1);
    }

    public static String getRole(Context ctx) {
        return ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).getString(KEY_ROLE, null);
    }

    public static String getName(Context ctx) {
        return ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).getString(KEY_NAME, null);
    }

    public static void clear(Context ctx) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().clear().apply();
    }
}
