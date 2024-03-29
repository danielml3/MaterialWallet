package com.danielml.materialwallet.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

class ClipboardUtils {
    companion object {
        /*
         * Copies the given test to the clipboard
         */
        fun copyToClipboard(context: Context, text: String) {
            val clipboard: ClipboardManager =
                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(text, text)
            clipboard.setPrimaryClip(clip)
        }

        /*
         * @returns the content of the clipboard
         */
        fun readClipboard(context: Context): String {
            val clipboard: ClipboardManager =
                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            return clipboard.primaryClip?.getItemAt(0)?.text?.toString() ?: ""
        }
    }
}