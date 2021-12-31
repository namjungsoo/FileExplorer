package com.duongame.cloud.dropbox

import com.dropbox.core.v2.DbxClientV2
import android.os.AsyncTask
import com.dropbox.core.v2.users.FullAccount
import com.dropbox.core.DbxException
import java.lang.Exception

/**
 * Async task for getting user account info
 */
class GetCurrentAccountTask(private val mDbxClient: DbxClientV2?, private val mCallback: Callback) :
    AsyncTask<Void?, Void?, FullAccount?>() {
    private var mException: Exception? = null

    interface Callback {
        fun onComplete(result: FullAccount?)
        fun onError(e: Exception?)
    }

    override fun onPostExecute(account: FullAccount?) {
        super.onPostExecute(account)
        if (mException != null) {
            mCallback.onError(mException)
        } else {
            mCallback.onComplete(account)
        }
    }

    protected override fun doInBackground(vararg params: Void?): FullAccount? {
        mException = try {
            return mDbxClient?.users()?.currentAccount
        } catch (e: DbxException) {
            e
        }
        return null
    }
}