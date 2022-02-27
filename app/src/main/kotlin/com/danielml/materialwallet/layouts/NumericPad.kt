package com.danielml.materialwallet.layouts

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import com.danielml.materialwallet.R
import com.google.android.material.button.MaterialButton

class NumericPad(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
    LinearLayout(context, attrs, defStyleAttr, defStyleRes) {
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    private val numberButtonList = listOf(
        Pair(R.id.button_0, "0"),
        Pair(R.id.button_1, "1"),
        Pair(R.id.button_2, "2"),
        Pair(R.id.button_3, "3"),
        Pair(R.id.button_4, "4"),
        Pair(R.id.button_5, "5"),
        Pair(R.id.button_6, "6"),
        Pair(R.id.button_7, "7"),
        Pair(R.id.button_8, "8"),
        Pair(R.id.button_9, "9")
    )

    private val maximumValueLength = 14
    private val maxDecimalsLength = 7

    private val decimalSymbol = "."

    private var valueString = "0"

    init {
        inflate(context, R.layout.numeric_pad, this)

        for (buttonInfo: Pair<Int, String> in numberButtonList) {
            val buttonId = buttonInfo.first
            val buttonValue = buttonInfo.second

            val button = findViewById<MaterialButton>(buttonId)
            button.setOnClickListener {
                if (valueString == "0") {
                    valueString = buttonValue
                } else {
                    if (valueString.length <= maximumValueLength && (getDecimalPartString().length <= maxDecimalsLength)) {
                        valueString += buttonValue
                    }
                }

                updateValueText()
            }
        }

        val dotButton = findViewById<MaterialButton>(R.id.button_dot)
        dotButton.setOnClickListener {
            if (valueString.length <= maximumValueLength) {
                if (!valueString.contains(decimalSymbol)) {
                    valueString += decimalSymbol
                }
            }

            updateValueText()
        }

        val backSlashButton = findViewById<MaterialButton>(R.id.backslash_button)
        backSlashButton.setOnClickListener {
            valueString = if (valueString.length > 1) {
                valueString.dropLast(1)
            } else {
                "0"
            }

            updateValueText()
        }

        backSlashButton.setOnLongClickListener {
            valueString = "0"
            updateValueText()
            true
        }
    }

    fun getValueString(): String {
        return valueString
    }

    fun setValueString(value: String) {
        valueString = value.replace(",", decimalSymbol)

        handler.post {
            updateValueText()
        }
    }

    private fun updateValueText() {
        val valueTextView = findViewById<TextView>(R.id.value_text)
        valueTextView.text = valueString
    }

    private fun getDecimalPartString(): String {
        return if (valueString.contains(decimalSymbol)) {
            valueString.split(decimalSymbol)[1]
        } else {
            ""
        }
    }
}