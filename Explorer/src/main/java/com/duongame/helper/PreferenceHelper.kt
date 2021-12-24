package com.duongame.helper

import android.content.Context
import android.content.SharedPreferences
import com.duongame.helper.PreferenceHelper
import com.duongame.fragment.ExplorerFragment
import com.duongame.fragment.BaseFragment
import android.os.Build
import android.os.Environment
import androidx.core.content.edit
import com.duongame.MainApplication
import java.util.*

/**
 * Created by 정수 on 2015-11-15.
 */
object PreferenceHelper {
    private const val DEBUG = false
    private const val PREF_NAME = "FileExplorer"
    private const val IS_SHORTCUT = "is_shortcut"

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
    private lateinit var locale: Locale

    fun init(context: Context) {
        if (pref == null) {
            pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            locale = getCurrentLocale(context)
        }
    }

    var shortcut: Boolean
        get() = pref?.getBoolean(IS_SHORTCUT, false) == true
        set(value) {
            pref?.edit {
                putBoolean(IS_SHORTCUT, value)
            }
        }

    var viewType: Int
        get() = pref?.getInt(VIEW_TYPE, ExplorerFragment.SWITCH_NARROW) ?: ExplorerFragment.SWITCH_NARROW
        set(value) {
            pref?.edit {
                putInt(VIEW_TYPE, value)
            }
        }

    var lastPath: String?
        get() = pref?.getString(LAST_PATH, Environment.getExternalStorageDirectory().absolutePath)
        set(value) {
            pref?.edit {
                putString(LAST_PATH, value)
            }
        }

    var lastPosition: Int
        get() = pref?.getInt(LAST_POSITION, 0) ?: 0
        set(value) {
            pref?.edit {
                putInt(LAST_POSITION, value)
            }
        }

    var lastTop: Int
        get() = pref?.getInt(LAST_TOP, 0) ?: 0
        set(value) {
            pref?.edit {
                putInt(LAST_TOP, value)
            }
        }

    var reviewCount: Int
        get() = pref?.getInt(REVIEW_COUNT, 0) ?: 0
        set(value) {
            pref?.edit {
                putInt(REVIEW_COUNT, value)
            }
        }

    var reviewed: Boolean
        get() = pref?.getBoolean(REVIEWED, false) == true
        set(value) {
            pref?.edit {
                putBoolean(REVIEWED, value)
            }
        }

    var exitAdCount: Int
        get() = pref?.getInt(EXIT_COUNT, 0) ?: 0
        set(value) {
            pref?.edit {
                putInt(EXIT_COUNT, value)
            }
        }

    var hideCompleted: Boolean
        get() = pref?.getBoolean(HIDE_COMPLETED, false) == true
        set(value) {
            pref?.edit {
                putBoolean(HIDE_COMPLETED, value)
            }
        }

    var sortType: Int
        get() = pref?.getInt(SORT_TYPE, 0) ?: 0
        set(value) {
            pref?.edit {
                putInt(SORT_TYPE, value)
            }
        }

    var sortDirection: Int
        get() = pref?.getInt(SORT_DIRECTION, 0) ?: 0
        set(value) {
            pref?.edit {
                putInt(SORT_DIRECTION, value)
            }
        }

    //BEGIN: Cloud Drive
    var accountDropbox: String?
        get() = pref?.getString(ACCOUNT_DROPBOX, null)
        set(value) {
            pref?.edit {
                putString(ACCOUNT_DROPBOX, value)
            }
        }

    var accountGoogleDrive: String?
        get() = pref?.getString(ACCOUNT_GOOGLE_DRIVE, null)
        set(value) {
            pref?.edit {
                putString(ACCOUNT_GOOGLE_DRIVE, value)
            }
        }

    var lastCloud: Int
        get() = pref?.getInt(LAST_CLOUD, BaseFragment.CLOUD_LOCAL) ?: BaseFragment.CLOUD_LOCAL
        set(value) {
            pref?.edit {
                putInt(LAST_CLOUD, value)
            }
        }
    //END: Cloud Drive

    var nightMode: Boolean
        get() = pref?.getBoolean(NIGHT_MODE, false) == true
        set(value) {
            pref?.edit {
                putBoolean(NIGHT_MODE, value)
            }
        }

    private fun getCurrentLocale(context: Context): Locale {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales[0]
        } else {
            context.resources.configuration.locale
        }
    }

    var japaneseDirection: Boolean
        get() {
            var defaultValue = false
            if (locale.language == "ja") {
                defaultValue = true
            }
            return pref?.getBoolean(JAPANESE_DIRECTION, false) == true
        }
        set(value) {
            pref?.edit {
                putBoolean(JAPANESE_DIRECTION, value)
            }
        }

    var thumbnailDisabled: Boolean
        get() = pref?.getBoolean(THUMBNAIL_DISABLED, false) == true
        set(value) {
            pref?.edit {
                putBoolean(THUMBNAIL_DISABLED, value)
            }
        }

    var permissionAgreed: Boolean
        get() = pref?.getBoolean(PERMISSION_AGREED, false) == true
        set(value) {
            pref?.edit {
                putBoolean(PERMISSION_AGREED, value)
            }
        }

    var pagingAnimationDisabled: Boolean
        get() = pref?.getBoolean(PAGING_ANIMATION_DISABLED, false) == true
        set(value) {
            pref?.edit {
                putBoolean(PAGING_ANIMATION_DISABLED, value)
            }
        }

    var autoPagingTime: Int
        get() = pref?.getInt(AUTO_PAGING_TIME, 0) ?: 0
        set(value) {
            pref?.edit {
                putInt(AUTO_PAGING_TIME, value)
            }
        }

    var adRemoveTimeReward: Long
        get() = pref?.getLong(AD_REMOVE_TIME_REWARD, 0L) ?: 0L
        set(value) {
            pref?.edit {
                putLong(AD_REMOVE_TIME_REWARD, value)
            }
        }
}