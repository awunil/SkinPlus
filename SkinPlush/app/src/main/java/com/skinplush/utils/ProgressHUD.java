package com.skinplush.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.skinplush.R;


public class ProgressHUD extends Dialog {
    public ProgressHUD(final Context context) {
        super(context);
    }

    public ProgressHUD(final Context context, final int theme) {
        super(context, theme);
    }

    @Override
    public void onWindowFocusChanged(final boolean hasFocus) {
        final ImageView imageView = findViewById(R.id.spinnerImageView);
        final AnimationDrawable spinner = (AnimationDrawable) imageView.getBackground();
        spinner.start();
    }

    public void setMessage(final CharSequence message) {
        TextView txt = findViewById(R.id.message);
        if (message != null && message.length() > 0) {
            txt.setVisibility(View.VISIBLE);
            txt.setText(message);
            txt.invalidate();
        }
    }

    public static ProgressHUD show(final Context context, final CharSequence message, final boolean indeterminate, final boolean cancelable, final OnCancelListener cancelListener) {
        final ProgressHUD dialog = new ProgressHUD(context, R.style.ProgressHUD);
        dialog.setTitle("");
        dialog.setContentView(R.layout.progress_hud);
        if (message == null || message.length() == 0) {
            dialog.findViewById(R.id.message).setVisibility(View.GONE);
        } else {
            final TextView txt = dialog.findViewById(R.id.message);
            txt.setText(message);
        }
        dialog.setCancelable(cancelable);
        dialog.setOnCancelListener(cancelListener);
        assert dialog.getWindow() != null;
        dialog.getWindow().getAttributes().gravity = Gravity.CENTER;
        final WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        lp.dimAmount = 0.2f;
        dialog.getWindow().setAttributes(lp);
        // dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        dialog.show();
        return dialog;
    }
}
