package com.duongame.cloud.googledrive

import android.accounts.AccountManager
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes

object GoogleDriveManager {
    //public static final int REQUEST_CODE_RESOLUTION = 1;
    const val REQUEST_AUTHORIZATION = 1
    const val REQUEST_ACCOUNT_PICKER = 2
    private val m_transport = AndroidHttp.newCompatibleTransport()
    private val m_jsonFactory: JsonFactory = GsonFactory.getDefaultInstance()
    @SuppressLint("StaticFieldLeak")
    var credential: GoogleAccountCredential? = null
        private set
    var client: Drive? = null
        private set

    private fun loginCore(context: Activity) {
        // Google Accounts using OAuth2
        credential = GoogleAccountCredential.usingOAuth2(context, setOf(DriveScopes.DRIVE))
        client = Drive.Builder(
            m_transport, m_jsonFactory, credential
        ).setApplicationName("AppName/1.0")
            .build()
    }

    fun login(context: Activity, accountName: String?) {
        loginCore(context)
        credential!!.selectedAccountName = accountName
    }

    @JvmStatic
    fun login(context: Activity) {
        loginCore(context)
        context.startActivityForResult(
            credential!!.newChooseAccountIntent(),
            REQUEST_ACCOUNT_PICKER
        )
    }

    @JvmStatic
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (requestCode == REQUEST_ACCOUNT_PICKER || requestCode == REQUEST_AUTHORIZATION) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null && data.extras != null) {
                    val accountName = data.extras!!.getString(AccountManager.KEY_ACCOUNT_NAME)
                    if (accountName != null) {
                        credential!!.selectedAccountName = accountName
                        return true
                    }
                }
            }
        }
        return false
    }
}