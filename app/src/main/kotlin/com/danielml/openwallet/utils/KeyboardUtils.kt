package com.danielml.openwallet.utils

import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager

class KeyboardUtils {
    companion object {
        fun dismissKeyboard(context: Context) {
            val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            val currentFocus = (context as Activity).currentFocus

            if (inputMethodManager.isAcceptingText && currentFocus != null) {
                inputMethodManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)
            }
        }
    }
}