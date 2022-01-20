package com.danielml.materialwallet.layouts

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import androidx.core.view.*
import com.danielml.materialwallet.Global.Companion.TAG
import com.danielml.materialwallet.R
import kotlin.math.abs

class DraggableLinearLayout(context: Context, attributeSet: AttributeSet?, defStyle: Int) :
    LinearLayout(context, attributeSet, defStyle), View.OnTouchListener {
    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)
    constructor(context: Context) : this(context, null)

    private val handleView: CardView
    private val handleContainer: LinearLayout

    private var maxTranslationY: Float = 0f
    private var lastStaticTranslation: Float = 0f

    private var lastMotionEvent: Int = -1

    private val topBottomMargin = 60
    private val animationDelay: Long = 300
    private val handleCornerRadius = 10f
    private val handleWidth = LayoutParams.MATCH_PARENT
    private val handleHeight = 15

    private val setStaticTranslationRunnable: Runnable

    init {
        handleView = CardView(context)
        val layoutParams = LayoutParams(handleWidth, handleHeight)

        layoutParams.gravity = Gravity.CENTER_HORIZONTAL
        handleView.setCardBackgroundColor(context.getColor(R.color.gray))
        handleView.radius =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, handleCornerRadius, context.resources.displayMetrics)
        handleView.layoutParams = layoutParams

        val containerLayoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        handleContainer = LinearLayout(context)
        handleContainer.setOnTouchListener(this)
        handleContainer.layoutParams = containerLayoutParams
        handleContainer.addView(handleView)

        addView(handleContainer)

        setStaticTranslationRunnable = Runnable {
            lastStaticTranslation = translationY
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)

        if (maxTranslationY == 0f) {
            maxTranslationY = height - (handleView.height + handleView.marginTop + handleView.marginBottom).toFloat()
            translationY = maxTranslationY
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        (layoutParams as ViewGroup.LayoutParams).apply {
            gravity = Gravity.BOTTOM
        }

        (handleView.layoutParams as LayoutParams).apply {
            setMargins(topBottomMargin * 2, topBottomMargin, topBottomMargin * 2, topBottomMargin)
            gravity = Gravity.CENTER
        }
    }

    override fun onTouch(view: View, event: MotionEvent?): Boolean {
        if (event == null) {
            return false
        }

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastStaticTranslation = translationY
            }

            MotionEvent.ACTION_MOVE -> {
                setTranslationInstant(translationY + event.y)
                handler.removeCallbacks(setStaticTranslationRunnable)
                handler.postDelayed(setStaticTranslationRunnable, 100)
            }

            MotionEvent.ACTION_UP -> {
                if (lastMotionEvent != MotionEvent.ACTION_DOWN) {
                    if (lastStaticTranslation - translationY < 0) {
                        setTranslationAnimated(Float.MAX_VALUE)
                    } else {
                        setTranslationAnimated(0f)
                    }
                } else {
                    if (translationY > maxTranslationY / 2) {
                        setTranslationAnimated(0f)
                    } else {
                        setTranslationAnimated(Float.MAX_VALUE)
                    }
                }
            }

            else -> {
                return false
            }
        }

        lastMotionEvent = event.action
        return true
    }

    private fun setTranslationInstant(height: Float) {
        setTranslationAnimated(height, 0)
    }

    private fun setTranslationAnimated(height: Float) {
        setTranslationAnimated(height, animationDelay)
    }

    private fun setTranslationAnimated(translation: Float, duration: Long) {
        val targetTranslationY = if (translation < 0) {
            0f
        } else if (translation > maxTranslationY) {
            maxTranslationY
        } else {
            translation
        }

        val slideAnimator = ValueAnimator
            .ofFloat(translationY, targetTranslationY)
            .setDuration(duration)

        slideAnimator.addUpdateListener { animation ->
            translationY = animation.animatedValue as Float
        }

        val animationSet = AnimatorSet()
        animationSet.play(slideAnimator)
        animationSet.start()
    }
}