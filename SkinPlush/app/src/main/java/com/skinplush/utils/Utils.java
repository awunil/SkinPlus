package com.skinplush.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings.Secure;
import android.util.Log;

/**
 * Created by Awuni Junior on 28/09/2020.
 */
public class Utils {

    private static final String TAG = Utils.class.getSimpleName();

    public static ProgressHUD mProgressHUD;

    public final static int DEFAULT_TIMEOUT = 30 * 1000;

    public final static String BASE_API = "http://145.14.157.88:5001/";
    public final static String COMPARE_API = BASE_API + "file";

    public static void showAlert(final Activity context, String msg) {
        AlertDialog.Builder alertBuilder = new AlertDialog
                .Builder(context);
        alertBuilder.setCancelable(false);
        alertBuilder.setMessage("Failed to process image");

        alertBuilder.setPositiveButton("OK",
                (dialog, which) -> dialog.dismiss());

        alertBuilder.create().show();
    }

    public static boolean isInternetConnected(final Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        if (activeNetwork != null) {

            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                Log.e(TAG, "WIFI");

            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                Log.e(TAG, "MOBILE");
            }

            return true;

        } else {
            return false;
        }
    }

    public static void showProgress(Activity activity) {
        try {
            mProgressHUD = ProgressHUD.show(activity, "",
                    true, false,
                    new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(final DialogInterface arg0) {
                    hideProgress();
                }
            });
        } catch (final Exception e) {
            Log.i(TAG, "showProgress() : " + Log.getStackTraceString(e));
        }
    }

    public static void hideProgress() {
        try {
            if (mProgressHUD != null && mProgressHUD.isShowing()) {
                mProgressHUD.dismiss();
            }
        } catch (final Exception e) {
            Log.i(TAG, "hideProgress() : " + Log.getStackTraceString(e));
        }
    }

    @SuppressLint("HardwareIds")
    public static String getDeviceID(Context context) {
        return  Secure.getString(context.getContentResolver(),
                Secure.ANDROID_ID);

    }

}
