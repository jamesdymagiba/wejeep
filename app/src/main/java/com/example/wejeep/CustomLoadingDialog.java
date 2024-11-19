package com.example.wejeep;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;
import android.view.WindowManager;

public class CustomLoadingDialog {
    private final Dialog dialog;

    public CustomLoadingDialog(Activity activity) {
        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_loading_screen);
        dialog.setCancelable(false); // prevent closing the dialog on touch

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    public void showLoadingScreen() {
        dialog.show();
    }

    public void hideLoadingScreen() {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}