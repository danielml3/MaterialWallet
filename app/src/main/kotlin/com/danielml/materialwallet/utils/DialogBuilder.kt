package com.danielml.materialwallet.utils

import android.content.Context
import android.content.DialogInterface
import android.view.View
import androidx.appcompat.app.AlertDialog

object DialogBuilder {
    /*
     * @returns an AlertDialog created with the given details
     */
    fun buildDialog(
        context: Context,
        onPositiveButton: DialogInterface.OnClickListener?,
        onNegativeButton: DialogInterface.OnClickListener?,
        content: View?,
        cancelable: Boolean,
        title: String?,
        message: String?
    ): AlertDialog {
        val builder = AlertDialog.Builder(context)

        if (title != null) {
            builder.setTitle(title)
        }

        if (message != null) {
            builder.setMessage(message)
        }

        builder.setCancelable(cancelable)

        if (content != null) {
            builder.setView(content)
        }

        if (onPositiveButton != null) {
            builder.setPositiveButton(android.R.string.ok, onPositiveButton)
        }

        if (onNegativeButton != null) {
            builder.setNegativeButton(android.R.string.cancel, onNegativeButton)
        }

        return builder.create()
    }

    /*
     * @returns an AlertDialog created with the given details
     */
    fun buildDialog(
        context: Context,
        onPositiveButton: DialogInterface.OnClickListener?,
        onNegativeButton: DialogInterface.OnClickListener?,
        content: View?,
        cancelable: Boolean,
        titleResId: Int,
        messageResId: Int
    ): AlertDialog {

        var title: String? = null
        var message: String? = null

        if (titleResId != 0) {
            title = context.getString(titleResId)
        }

        if (messageResId != 0) {
            message = context.getString(messageResId)
        }

        return buildDialog(
            context, onPositiveButton, onNegativeButton, content, cancelable,
            title, message
        )
    }
}