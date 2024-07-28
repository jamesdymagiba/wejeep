package com.example.wejeep;

import android.view.View;
import android.widget.ProgressBar;

public class ProgressBarHandler {
    private ProgressBar progressBar;

    public ProgressBarHandler(ProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    public void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    public void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }
}
