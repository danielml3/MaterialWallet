package com.danielml.materialwallet.layouts

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.doOnLayout
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.updateLayoutParams
import com.danielml.materialwallet.R
import com.danielml.materialwallet.utils.VibrationUtils
import com.google.android.material.button.MaterialButton
import com.google.android.material.elevation.SurfaceColors

@SuppressLint("ClickableViewAccessibility")
class SlideToAction(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
    LinearLayout(context, attrs, defStyleAttr, defStyleRes) {
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    private var minSliderWidth = -1
    private var listener: Runnable? = null
    private val slider: MaterialButton
    private val hintText: TextView

    private var keepHintHidden = false

    init {
        inflate(context, R.layout.slide_to_action, this)

        val root = this
        hintText = findViewById(R.id.hint_text)
        slider = findViewById(R.id.slider)

        slider.setOnTouchListener { _, event ->
            val maximumWidth = root.width - slider.marginLeft - slider.marginRight
            val triggerWidth = maximumWidth - 20

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    hideHint()
                    keepHintHidden = true
                }

                MotionEvent.ACTION_MOVE -> {
                    if (!slider.isEnabled) {
                        return@setOnTouchListener false
                    }

                    keepHintHidden = true
                    slider.updateLayoutParams<LayoutParams> {
                        if (event.x <= maximumWidth && event.x > minSliderWidth) {
                            width = event.x.toInt()
                        }
                    }
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    keepHintHidden = false
                    if (slider.width >= triggerWidth && slider.isEnabled) {
                        listener?.run()
                        VibrationUtils.vibrateDefault(context)
                    } else {
                        retractSlider()
                    }
                }
            }

            false
        }

        slider.doOnLayout {
            if (minSliderWidth == -1) {
                minSliderWidth = slider.width
            }
        }
    }

    fun setOnActionTriggeredListener(listener: Runnable) {
        this.listener = listener
    }

    fun setSliderEnabled(enabled: Boolean) {
        slider.isEnabled = enabled
    }

    fun retractSlider() {
        val initialWidth = slider.width
        slider
            .animate()
            .setDuration(1000)
            .setUpdateListener { progress ->
                val nextWidth = initialWidth - initialWidth * progress.animatedFraction

                if (nextWidth >= minSliderWidth) {
                    slider.updateLayoutParams<LayoutParams> {
                        width = nextWidth.toInt()
                    }
                }
            }
            .withEndAction {
                if (!keepHintHidden) {
                    showHint()
                }
            }
            .start()
    }

    fun setHintText(hint: String) {
        hintText.text = hint
    }

    private fun showHint() {
        hintText.visibility = View.VISIBLE
        hintText.animation?.cancel()

        hintText
            .animate()
            .setDuration(100)
            .alpha(1f)
            .start()
    }

    private fun hideHint() {
        hintText
            .animate()
            .setDuration(100)
            .alpha(0f)
            .withEndAction {
                hintText.visibility = View.GONE
            }
            .start()
    }
}