package com.duongame.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

/**
 * Created by 정수 on 2015-11-15.
 */
public class PreferenceHelper {
    private final static boolean DEBUG = false;

    private static final String TAG = "PreferenceHelper";
    private static final String PREF_NAME = "FileExplorer";
    private static final String IS_SHORTCUT = "is_shortcut";
//    private static final String PREF_IS_EXPLORER_HELP = "is_explorer_help";

    // 3번중에서 1번을 보여준다.
    private static final String START_COUNT = "start_count";
    private static final String VIEW_TYPE = "view_type";
    private static final String LAST_PATH = "last_path";
    private static final String LAST_POSITION = "last_position";
    private static final String LAST_TOP = "last_top";

    private static final String REVIEW_COUNT = "review_count";
    private static final String REVIEWED = "reviewed";
    private static final String EXIT_COUNT = "exit_count";
    private static final String HIDE_COMPLETED = "hide_completed";

    private static SharedPreferences pref = null;

    private static void checkPrefManager(Context context) {
        if (pref == null)
            pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static boolean isShortcut(Context context) {
        checkPrefManager(context);
        final boolean prefIsShortcut = pref.getBoolean(IS_SHORTCUT, false);
        return prefIsShortcut;
    }

    public static void setShortcut(Context context, boolean shortcut) {
        checkPrefManager(context);
        final SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(IS_SHORTCUT, shortcut);
        editor.commit();
    }

    public static int getStartCount(Context context) {
        checkPrefManager(context);
        final int startCount = pref.getInt(START_COUNT, 0);
        return startCount;
    }

    public static void setStartCount(Context context, int count) {
        checkPrefManager(context);
        final SharedPreferences.Editor editor = pref.edit();
        editor.putInt(START_COUNT, count);
        editor.commit();
    }

    public static int getViewType(Context context) {
        checkPrefManager(context);
        final int viewType = pref.getInt(VIEW_TYPE, 0);
        return viewType;
    }

    public static void setViewType(Context context, int viewType) {
        checkPrefManager(context);
        final SharedPreferences.Editor editor = pref.edit();
        editor.putInt(VIEW_TYPE, viewType);
        editor.commit();
    }

    public static String getLastPath(Context context) {
        checkPrefManager(context);
        final String lastPath = pref.getString(LAST_PATH, Environment.getExternalStorageDirectory().getAbsolutePath());
        return lastPath;
    }

    public static void setLastPath(Context context, String lastPath) {
        checkPrefManager(context);
        final SharedPreferences.Editor editor = pref.edit();
        editor.putString(LAST_PATH, lastPath);
        editor.commit();
    }

    public static int getLastPosition(Context context) {
        checkPrefManager(context);
        final int lastPosition = pref.getInt(LAST_POSITION, 0);
        return lastPosition;
    }

    public static void setLastPosition(Context context, int lastPosition) {
        checkPrefManager(context);
        final SharedPreferences.Editor editor = pref.edit();
        editor.putInt(LAST_POSITION, lastPosition);
        editor.commit();
    }

    public static int getLastTop(Context context) {
        checkPrefManager(context);
        final int lastTop = pref.getInt(LAST_TOP, 0);
        return lastTop;
    }

    public static void setLastTop(Context context, int lastTop) {
        checkPrefManager(context);
        final SharedPreferences.Editor editor = pref.edit();
        editor.putInt(LAST_TOP, lastTop);
        editor.commit();
    }

    public static int getReviewCount(Context context) {
        checkPrefManager(context);
        final int review = pref.getInt(REVIEW_COUNT, 0);
        return review;
    }

    public static void setReviewCount(Context context, int count) {
        checkPrefManager(context);
        final SharedPreferences.Editor editor = pref.edit();
        editor.putInt(REVIEW_COUNT, count);
        editor.commit();
    }

    public static boolean getReviewed(Context context) {
        checkPrefManager(context);
        return pref.getBoolean(REVIEWED, false);
    }

    public static void setReviewed(Context context, boolean reviewed) {
        checkPrefManager(context);
        final SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(REVIEWED, reviewed);
        editor.commit();
    }

    public static int getExitAdCount(Context context) {
        checkPrefManager(context);
        return pref.getInt(EXIT_COUNT, 0);
    }

    public static void setExitAdCount(Context context, int count) {
        checkPrefManager(context);
        final SharedPreferences.Editor editor = pref.edit();
        editor.putInt(EXIT_COUNT, count);
        editor.commit();
    }

    public static boolean getHideCompleted(Context context) {
        checkPrefManager(context);
        return pref.getBoolean(HIDE_COMPLETED, false);
    }

    public static void setHideCompleted(Context context, boolean hide) {
        checkPrefManager(context);
        final SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(HIDE_COMPLETED, hide);
        editor.commit();
    }

}