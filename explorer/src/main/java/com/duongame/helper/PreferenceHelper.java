package com.duongame.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import static com.duongame.fragment.BaseFragment.CLOUD_LOCAL;

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

    private static final String SORT_TYPE = "sort_type";
    private static final String SORT_DIRECTION = "sort_direction";

    private static final String ACCOUNT_DROPBOX = "account_dropbox";
    private static final String ACCOUNT_GOOGLE_DRIVE = "account_google_drive";

    private static final String LAST_CLOUD = "last_cloud";

    private static final String NIGHT_MODE = "night_mode";
    private static final String JAPANESE_DIRECTION = "japanese_direction";
    private static final String THUMBNAIL = "thumbnail";

    private static SharedPreferences pref = null;

    private static void checkPrefManager(Context context) {
        if (pref == null) {
            if (context == null)
                return;

            pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        }
    }

    public static boolean isShortcut(Context context) {
        checkPrefManager(context);
        return pref.getBoolean(IS_SHORTCUT, false);
    }

    public static void setShortcut(Context context, boolean shortcut) {
        checkPrefManager(context);
        final SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(IS_SHORTCUT, shortcut);
        editor.apply();
    }

    public static int getStartCount(Context context) {
        checkPrefManager(context);
        return pref.getInt(START_COUNT, 0);
    }

    public static void setStartCount(Context context, int count) {
        checkPrefManager(context);
        final SharedPreferences.Editor editor = pref.edit();
        editor.putInt(START_COUNT, count);
        editor.apply();
    }

    public static int getViewType(Context context) {
        checkPrefManager(context);
        return pref.getInt(VIEW_TYPE, 0);
    }

    public static void setViewType(Context context, int viewType) {
        checkPrefManager(context);
        final SharedPreferences.Editor editor = pref.edit();
        editor.putInt(VIEW_TYPE, viewType);
        editor.apply();
    }

    public static String getLastPath(Context context) {
        checkPrefManager(context);
        return pref.getString(LAST_PATH, Environment.getExternalStorageDirectory().getAbsolutePath());
    }

    public static void setLastPath(Context context, String lastPath) {
        checkPrefManager(context);
        final SharedPreferences.Editor editor = pref.edit();
        editor.putString(LAST_PATH, lastPath);
        editor.apply();
    }

    public static int getLastPosition(Context context) {
        checkPrefManager(context);
        return pref.getInt(LAST_POSITION, 0);
    }

    public static void setLastPosition(Context context, int lastPosition) {
        checkPrefManager(context);
        final SharedPreferences.Editor editor = pref.edit();
        editor.putInt(LAST_POSITION, lastPosition);
        editor.apply();
    }

    public static int getLastTop(Context context) {
        checkPrefManager(context);
        return pref.getInt(LAST_TOP, 0);
    }

    public static void setLastTop(Context context, int lastTop) {
        checkPrefManager(context);
        final SharedPreferences.Editor editor = pref.edit();
        editor.putInt(LAST_TOP, lastTop);
        editor.apply();
    }

    public static int getReviewCount(Context context) {
        checkPrefManager(context);
        return pref.getInt(REVIEW_COUNT, 0);
    }

    public static void setReviewCount(Context context, int count) {
        checkPrefManager(context);
        final SharedPreferences.Editor editor = pref.edit();
        editor.putInt(REVIEW_COUNT, count);
        editor.apply();
    }

    public static boolean getReviewed(Context context) {
        checkPrefManager(context);
        return pref.getBoolean(REVIEWED, false);
    }

    public static void setReviewed(Context context, boolean reviewed) {
        checkPrefManager(context);
        final SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(REVIEWED, reviewed);
        editor.apply();
    }

    public static int getExitAdCount(Context context) {
        checkPrefManager(context);
        return pref.getInt(EXIT_COUNT, 0);
    }

    public static void setExitAdCount(Context context, int count) {
        checkPrefManager(context);
        final SharedPreferences.Editor editor = pref.edit();
        editor.putInt(EXIT_COUNT, count);
        editor.apply();
    }

    public static boolean getHideCompleted(Context context) {
        checkPrefManager(context);
        return pref.getBoolean(HIDE_COMPLETED, false);
    }

    public static void setHideCompleted(Context context, boolean hide) {
        checkPrefManager(context);
        final SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(HIDE_COMPLETED, hide);
        editor.apply();
    }

    public static int getSortType(Context context) {
        checkPrefManager(context);
        return pref.getInt(SORT_TYPE, 0);
    }

    public static void setSortType(Context context, int count) {
        checkPrefManager(context);
        final SharedPreferences.Editor editor = pref.edit();
        editor.putInt(SORT_TYPE, count);
        editor.apply();
    }

    public static int getSortDirection(Context context) {
        checkPrefManager(context);
        return pref.getInt(SORT_DIRECTION, 0);
    }

    public static void setSortDirection(Context context, int count) {
        checkPrefManager(context);
        final SharedPreferences.Editor editor = pref.edit();
        editor.putInt(SORT_DIRECTION, count);
        editor.apply();
    }

    //BEGIN: Cloud Drive
    public static String getAccountDropbox(Context context) {
        checkPrefManager(context);
        return pref.getString(ACCOUNT_DROPBOX, null);
    }

    public static void setAccountDropbox(Context context, String accountDropbox) {
        checkPrefManager(context);
        final SharedPreferences.Editor editor = pref.edit();
        editor.putString(ACCOUNT_DROPBOX, accountDropbox);
        editor.apply();
    }

    public static String getAccountGoogleDrive(Context context) {
        checkPrefManager(context);
        return pref.getString(ACCOUNT_GOOGLE_DRIVE, null);
    }

    public static void setAccountGoogleDrive(Context context, String accountGoogleDrive) {
        checkPrefManager(context);
        final SharedPreferences.Editor editor = pref.edit();
        editor.putString(ACCOUNT_GOOGLE_DRIVE, accountGoogleDrive);
        editor.apply();
    }

    public static int getLastCloud(Context context) {
        checkPrefManager(context);
        return pref.getInt(LAST_CLOUD, CLOUD_LOCAL);
    }

    public static void setLastCloud(Context context, int cloud) {
        checkPrefManager(context);
        final SharedPreferences.Editor editor = pref.edit();
        editor.putInt(LAST_CLOUD, cloud);
        editor.apply();
    }
    //END: Cloud Drive


    public static boolean getNightMode(Context context) {
        checkPrefManager(context);
        return pref.getBoolean(NIGHT_MODE, false);
    }

    public static void setNightMode(Context context, boolean b) {
        checkPrefManager(context);
        final SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(NIGHT_MODE, b);
        editor.apply();
    }

    public static boolean getJapaneseDirection(Context context) {
        checkPrefManager(context);
        return pref.getBoolean(JAPANESE_DIRECTION, false);
    }

    public static void setJapaneseDirection(Context context, boolean b) {
        checkPrefManager(context);
        final SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(JAPANESE_DIRECTION, b);
        editor.apply();
    }

    public static boolean getThumbnail(Context context) {
        checkPrefManager(context);
        return pref.getBoolean(THUMBNAIL, false);
    }

    public static void setThumbnail(Context context, boolean b) {
        checkPrefManager(context);
        final SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(THUMBNAIL, b);
        editor.apply();
    }

}
