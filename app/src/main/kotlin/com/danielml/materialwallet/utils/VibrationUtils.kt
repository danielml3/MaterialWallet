package com.danielml.materialwallet.utils

import android.content.Context
import android.os.Vibrator

class VibrationUtils {
    companion object {
        private const val DEFAULT_VIBRATION: Long = 50

        fun vibrateDefault(context: Context) {
            vibrate(context, DEFAULT_VIBRATION)
        }

        fun vibrate(context: Context, duration: Long) {
            (context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(duration)
        }
    }
}