package com.duongame.helper

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Environment
import com.duongame.fragment.BaseFragment
import com.duongame.fragment.ExplorerFragment
import java.util.*

/**
 * Created by 정수 on 2015-11-15.
 */
//TODO: 더 간단히 pref 작업을 kotlin으로 수행할것
object PreferenceHelper {
    private const val DEBUG = false
    private const val TAG = "PreferenceHelper"
    private const val PREF_NAME = "FileExplorer"
    private const val IS_SHORTCUT = "is_shortcut"

    //    private static final String PREF_IS_EXPLORER_HELP = "is_explorer_help";
    // 3번중에서 1번을 보여준다.
    private const val START_COUNT = "start_count"
    private const val VIEW_TYPE = "view_type"
    private const val LAST_PATH = "last_path"
    private const val LAST_POSITION = "last_position"
    private const val LAST_TOP = "last_top"
    private const val REVIEW_COUNT = "review_count"
    private const val REVIEWED = "reviewed"
    private const val EXIT_COUNT = "exit_count"
    private const val HIDE_COMPLETED = "hide_completed"
    private const val SORT_TYPE = "sort_type"
    private const val SORT_DIRECTION = "sort_direction"
    private const val ACCOUNT_DROPBOX = "account_dropbox"
    private const val ACCOUNT_GOOGLE_DRIVE = "account_google_drive"
    private const val LAST_CLOUD = "last_cloud"
    private const val NIGHT_MODE = "night_mode"
    private const val JAPANESE_DIRECTION = "japanese_direction"
    private const val THUMBNAIL_DISABLED = "thumbnail_disabled"
    private const val PERMISSION_AGREED = "permission_agreed"
    private const val PAGING_ANIMATION_DISABLED = "paging_animation_disabled"
    private const val AUTO_PAGING_TIME = "auto_paging_time"
    private const val AD_REMOVE_TIME_REWARD = "ad_remove_time_reward"
    private var pref: SharedPreferences? = null
    private fun checkPrefManager(context: Context?) {
        if (pref == null) {
            if (context == null) return
            pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        }
    }

    fun isShortcut(context: Context?): Boolean {
        checkPrefManager(context)
        return pref!!.getBoolean(IS_SHORTCUT, false)
    }

    fun setShortcut(context: Context?, shortcut: Boolean) {
        checkPrefManager(context)
        val editor = pref!!.edit()
        editor.putBoolean(IS_SHORTCUT, shortcut)
        editor.apply()
    }

    fun getStartCount(context: Context?): Int {
        checkPrefManager(context)
        return pref!!.getInt(START_COUNT, 0)
    }

    fun setStartCount(context: Context?, count: Int) {
        checkPrefManager(context)
        val editor = pref!!.edit()
        editor.putInt(START_COUNT, count)
        editor.apply()
    }

    @JvmStatic
    fun getViewType(context: Context?): Int {
        checkPrefManager(context)
        return pref!!.getInt(VIEW_TYPE, ExplorerFragment.SWITCH_NARROW)
    }

    @JvmStatic
    fun setViewType(context: Context?, viewType: Int) {
        checkPrefManager(context)
        val editor = pref!!.edit()
        editor.putInt(VIEW_TYPE, viewType)
        editor.apply()
    }

    @JvmStatic
    fun getLastPath(context: Context?): String? {
        checkPrefManager(context)
        return pref!!.getString(LAST_PATH, Environment.getExternalStorageDirectory().absolutePath)
    }

    @JvmStatic
    fun setLastPath(context: Context?, lastPath: String?) {
        checkPrefManager(context)
        val editor = pref!!.edit()
        editor.putString(LAST_PATH, lastPath)
        editor.apply()
    }

    fun getLastPosition(context: Context?): Int {
        checkPrefManager(context)
        return pref!!.getInt(LAST_POSITION, 0)
    }

    @JvmStatic
    fun setLastPosition(context: Context?, lastPosition: Int) {
        checkPrefManager(context)
        val editor = pref!!.edit()
        editor.putInt(LAST_POSITION, lastPosition)
        editor.apply()
    }

    fun getLastTop(context: Context?): Int {
        checkPrefManager(context)
        return pref!!.getInt(LAST_TOP, 0)
    }

    @JvmStatic
    fun setLastTop(context: Context?, lastTop: Int) {
        checkPrefManager(context)
        val editor = pref!!.edit()
        editor.putInt(LAST_TOP, lastTop)
        editor.apply()
    }

    @JvmStatic
    fun getReviewCount(context: Context?): Int {
        checkPrefManager(context)
        return pref!!.getInt(REVIEW_COUNT, 0)
    }

    @JvmStatic
    fun setReviewCount(context: Context?, count: Int) {
        checkPrefManager(context)
        val editor = pref!!.edit()
        editor.putInt(REVIEW_COUNT, count)
        editor.apply()
    }

    @JvmStatic
    fun getReviewed(context: Context?): Boolean {
        checkPrefManager(context)
        return pref!!.getBoolean(REVIEWED, false)
    }

    @JvmStatic
    fun setReviewed(context: Context?, reviewed: Boolean) {
        checkPrefManager(context)
        val editor = pref!!.edit()
        editor.putBoolean(REVIEWED, reviewed)
        editor.apply()
    }

    @JvmStatic
    fun getExitAdCount(context: Context?): Int {
        checkPrefManager(context)
        return pref!!.getInt(EXIT_COUNT, 0)
    }

    @JvmStatic
    fun setExitAdCount(context: Context?, count: Int) {
        checkPrefManager(context)
        val editor = pref!!.edit()
        editor.putInt(EXIT_COUNT, count)
        editor.apply()
    }

    @JvmStatic
    fun getHideCompleted(context: Context?): Boolean {
        checkPrefManager(context)
        return pref!!.getBoolean(HIDE_COMPLETED, false)
    }

    @JvmStatic
    fun setHideCompleted(context: Context?, hide: Boolean) {
        checkPrefManager(context)
        val editor = pref!!.edit()
        editor.putBoolean(HIDE_COMPLETED, hide)
        editor.apply()
    }

    @JvmStatic
    fun getSortType(context: Context?): Int {
        checkPrefManager(context)
        return pref!!.getInt(SORT_TYPE, 0)
    }

    @JvmStatic
    fun setSortType(context: Context?, count: Int) {
        checkPrefManager(context)
        val editor = pref!!.edit()
        editor.putInt(SORT_TYPE, count)
        editor.apply()
    }

    @JvmStatic
    fun getSortDirection(context: Context?): Int {
        checkPrefManager(context)
        return pref!!.getInt(SORT_DIRECTION, 0)
    }

    @JvmStatic
    fun setSortDirection(context: Context?, count: Int) {
        checkPrefManager(context)
        val editor = pref!!.edit()
        editor.putInt(SORT_DIRECTION, count)
        editor.apply()
    }

    //BEGIN: Cloud Drive
    @JvmStatic
    fun getAccountDropbox(context: Context?): String? {
        checkPrefManager(context)
        return pref!!.getString(ACCOUNT_DROPBOX, null)
    }

    @JvmStatic
    fun setAccountDropbox(context: Context?, accountDropbox: String?) {
        checkPrefManager(context)
        val editor = pref!!.edit()
        editor.putString(ACCOUNT_DROPBOX, accountDropbox)
        editor.apply()
    }

    @JvmStatic
    fun getAccountGoogleDrive(context: Context?): String? {
        checkPrefManager(context)
        return pref!!.getString(ACCOUNT_GOOGLE_DRIVE, null)
    }

    @JvmStatic
    fun setAccountGoogleDrive(context: Context?, accountGoogleDrive: String?) {
        checkPrefManager(context)
        val editor = pref!!.edit()
        editor.putString(ACCOUNT_GOOGLE_DRIVE, accountGoogleDrive)
        editor.apply()
    }

    @JvmStatic
    fun getLastCloud(context: Context?): Int {
        checkPrefManager(context)
        return pref!!.getInt(LAST_CLOUD, BaseFragment.CLOUD_LOCAL)
    }

    @JvmStatic
    fun setLastCloud(context: Context?, cloud: Int) {
        checkPrefManager(context)
        val editor = pref!!.edit()
        editor.putInt(LAST_CLOUD, cloud)
        editor.apply()
    }

    //END: Cloud Drive
    fun getNightMode(context: Context?): Boolean {
        checkPrefManager(context)
        return pref!!.getBoolean(NIGHT_MODE, false)
    }

    fun setNightMode(context: Context?, b: Boolean) {
        checkPrefManager(context)
        val editor = pref!!.edit()
        editor.putBoolean(NIGHT_MODE, b)
        editor.apply()
    }

    fun getCurrentLocale(context: Context): Locale {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales[0]
        } else {
            context.resources.configuration.locale
        }
    }

    fun getJapaneseDirection(context: Context): Boolean {
        checkPrefManager(context)

        // 일본어일 경우 기본값 true
        var defaultValue = false
        if (getCurrentLocale(context).language == "ja") {
            defaultValue = true
        }
        return pref!!.getBoolean(JAPANESE_DIRECTION, defaultValue)
    }

    fun setJapaneseDirection(context: Context?, b: Boolean) {
        checkPrefManager(context)
        val editor = pref!!.edit()
        editor.putBoolean(JAPANESE_DIRECTION, b)
        editor.apply()
    }

    fun getThumbnailDisabled(context: Context?): Boolean {
        checkPrefManager(context)
        return pref!!.getBoolean(THUMBNAIL_DISABLED, false)
    }

    fun setThumbnailDisabled(context: Context?, b: Boolean) {
        checkPrefManager(context)
        val editor = pref!!.edit()
        editor.putBoolean(THUMBNAIL_DISABLED, b)
        editor.apply()
    }

    @JvmStatic
    fun getPermissionAgreed(context: Context?): Boolean {
        checkPrefManager(context)
        return pref!!.getBoolean(PERMISSION_AGREED, false)
    }

    @JvmStatic
    fun setPermissionAgreed(context: Context?, b: Boolean) {
        checkPrefManager(context)
        val editor = pref!!.edit()
        editor.putBoolean(PERMISSION_AGREED, b)
        editor.apply()
    }

    fun getPagingAnimationDisabled(context: Context?): Boolean {
        checkPrefManager(context)
        return pref!!.getBoolean(PAGING_ANIMATION_DISABLED, false)
    }

    fun setPagingAnimationDisabled(context: Context?, b: Boolean) {
        checkPrefManager(context)
        val editor = pref!!.edit()
        editor.putBoolean(PAGING_ANIMATION_DISABLED, b)
        editor.apply()
    }

    @JvmStatic
    fun getAutoPagingTime(context: Context?): Int {
        checkPrefManager(context)
        return pref!!.getInt(AUTO_PAGING_TIME, 0)
    }

    @JvmStatic
    fun setAutoPagingTime(context: Context?, time: Int) {
        checkPrefManager(context)
        val editor = pref!!.edit()
        editor.putInt(AUTO_PAGING_TIME, time)
        editor.apply()
    }

    //ad_remove_time_reward
    @JvmStatic
    fun getAdRemoveTimeReward(context: Context?): Long {
        checkPrefManager(context)
        return pref!!.getLong(AD_REMOVE_TIME_REWARD, 0L)
    }

    @JvmStatic
    fun setAdRemoveTimeReward(context: Context?, time: Long) {
        checkPrefManager(context)
        val editor = pref!!.edit()
        editor.putLong(AD_REMOVE_TIME_REWARD, time)
        editor.apply()
    }
}