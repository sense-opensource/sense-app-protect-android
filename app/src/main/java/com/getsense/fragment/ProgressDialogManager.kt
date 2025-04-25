package com.getsense.fragment

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.getsense.R

object ProgressDialogManager {
    private var progressDialog: Dialog? = null

    /**
     * Show the progress dialog
     *
     * @param context The context in which the dialog should be shown.
     */
    fun show(context: Context) {
        // If a dialog is already showing, don't create a new one
        if (progressDialog != null && progressDialog?.isShowing == true) {
            return
        }

        // Initialize the dialog
        progressDialog = Dialog(context)

        // Inflate the custom view
        val view: View = LayoutInflater.from(context).inflate(R.layout.loading, null)

        // Set the custom view
        progressDialog?.setContentView(view)

        // Make the dialog non-cancelable
        progressDialog?.setCancelable(false)

        // Show the dialog
        progressDialog?.show()
    }

    /**
     * Dismiss the progress dialog
     */
    fun dismiss() {
        // Dismiss the dialog if it's showing
        if (progressDialog != null && progressDialog?.isShowing == true) {
            progressDialog?.dismiss()
            progressDialog = null
        }
    }

    /**
     * Check if the progress dialog is currently showing
     *
     * @return True if the progress dialog is showing, false otherwise
     */
    fun isShowing(): Boolean {
        return progressDialog != null && progressDialog?.isShowing == true
    }
}