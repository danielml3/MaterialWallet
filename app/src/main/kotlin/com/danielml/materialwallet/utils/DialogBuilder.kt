package com.danielml.materialwallet.utils

import android.content.Context
import android.content.DialogInterface
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class DialogBuilder(private val context: Context) {
    private var title: String? = null
    private var message: String? = null

    private var cancelable = false

    private var content: View? = null

    private var onPositiveButton: DialogInterface.OnClickListener? = null
    private var onNegativeButton: DialogInterface.OnClickListener? = null
    private var onDismiss: DialogInterface.OnDismissListener = DialogInterface.OnDismissListener {}

    fun setTitle(title: String): DialogBuilder {
        this.title = title
        return this
    }

    fun setTitle(titleResId: Int): DialogBuilder {
        return setTitle(context.resources.getString(titleResId))
    }

    fun setMessage(message: String): DialogBuilder {
        this.message = message
        return this
    }

    fun setMessage(messageResId: Int): DialogBuilder {
        return setMessage(context.resources.getString(messageResId))
    }

    fun setOnPositiveButton(onPositiveButton: DialogInterface.OnClickListener): DialogBuilder {
        this.onPositiveButton = onPositiveButton
        return this
    }

    fun setOnNegativeButton(onNegativeButton: DialogInterface.OnClickListener): DialogBuilder {
        this.onNegativeButton = onNegativeButton
        return this
    }

    fun setOnDismiss(onDismiss: DialogInterface.OnDismissListener): DialogBuilder {
        this.onDismiss = onDismiss
        return this
    }

    fun setContent(content: View): DialogBuilder {
        this.content = content
        return this
    }

    fun setCancelable(cancelable: Boolean): DialogBuilder {
        this.cancelable = cancelable
        return this
    }

    /*
     * @returns an AlertDialog created with the given details
     */
    fun buildDialog(): AlertDialog {
        val builder = MaterialAlertDialogBuilder(context)

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

        builder.setOnDismissListener(onDismiss)

        return builder.create()
    }

}