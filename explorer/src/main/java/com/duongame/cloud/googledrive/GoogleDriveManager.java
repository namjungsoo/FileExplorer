package com.duongame.cloud.googledrive;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import java.util.Collections;

public class GoogleDriveManager {
    //public static final int REQUEST_CODE_RESOLUTION = 1;
    public static final int REQUEST_AUTHORIZATION = 1;
    public static final int REQUEST_ACCOUNT_PICKER = 2;

    private static final HttpTransport m_transport = AndroidHttp.newCompatibleTransport();
    private static final com.google.api.client.json.JsonFactory m_jsonFactory = GsonFactory.getDefaultInstance();
    private static GoogleAccountCredential m_credential;
    private static Drive m_client;

    private static void loginCore(Activity context) {
        // Google Accounts using OAuth2
        m_credential = GoogleAccountCredential.usingOAuth2(context, Collections.singleton(DriveScopes.DRIVE));

        m_client = new com.google.api.services.drive.Drive.Builder(
                m_transport, m_jsonFactory, m_credential).setApplicationName("AppName/1.0")
                .build();
    }

    public static void login(Activity context, String accountName) {
        loginCore(context);
        m_credential.setSelectedAccountName(accountName);
    }

    public static void login(Activity context) {
        loginCore(context);
        context.startActivityForResult(m_credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
    }

    public static boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode == REQUEST_ACCOUNT_PICKER || requestCode == REQUEST_AUTHORIZATION)) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null && data.getExtras() != null) {
                    String accountName = data.getExtras().getString(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        m_credential.setSelectedAccountName(accountName);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static GoogleAccountCredential getCredential() {
        return m_credential;
    }

    public static Drive getClient() {
        return m_client;
    }
}
