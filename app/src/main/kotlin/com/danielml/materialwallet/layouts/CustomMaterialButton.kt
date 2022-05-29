package com.danielml.materialwallet.layouts

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import com.google.android.material.R
import com.google.android.material.button.MaterialButton


class CustomMaterialButton(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    MaterialButton(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, R.attr.materialButtonStyle)
    constructor(context: Context) : this(context, null)

    private val animationMs = 200L

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                setScaleAnimated(0.95f)
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                setScaleAnimated(1f)
            }
        }

        return super.onTouchEvent(event)
    }


    private fun setScaleAnimated(scale: Float) {
        val sizeAnimation = AnimatorSet()
        val scaleX = ObjectAnimator.ofFloat(this, "scaleX", scale)
        val scaleY = ObjectAnimator.ofFloat(this, "scaleY", scale)

        scaleX.duration = animationMs
        scaleY.duration = animationMs

        sizeAnimation.play(scaleX).with(scaleY)
        sizeAnimation.start()
    }
}