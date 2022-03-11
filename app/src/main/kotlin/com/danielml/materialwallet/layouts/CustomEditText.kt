package com.danielml.materialwallet.layouts

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import com.danielml.materialwallet.utils.InputUtils
import com.google.android.material.R
import com.google.android.material.textfield.TextInputEditText


class CustomEditText(context: Context, attributeSet: AttributeSet?, defStyle: Int) :
    TextInputEditText(context, attributeSet, defStyle) {
    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, R.attr.editTextStyle)
    constructor(context: Context) : this(context, null)

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)

        if (!focused) {
            InputUtils.hideKeyboard(context, this)
        }
    }
}