package com.danielml.materialwallet.layouts.addresscards

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.updateLayoutParams
import androidx.core.view.updateMargins
import androidx.transition.TransitionManager
import com.danielml.materialwallet.R
import com.danielml.materialwallet.utils.ClipboardUtils
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import org.bitcoinj.core.Address
import java.util.*

/*
    This class is only meant to be initialized programmatically, not from a
    XML layout
*/
@SuppressLint("ViewConstructor")
open class AddressCard(context: Context, address: Address, style: Int) :
    MaterialCardView(context, null, style) {
    companion object {
        const val QR_SIZE = 512
    }

    private var addressQRShown = false
    private val addressString = address.toString()
    private lateinit var addressQRImageView: ImageView

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        updateLayoutParams<LinearLayout.LayoutParams> {
            updateMargins(20, 20, 20, 20)
        }

        inflate(context, R.layout.address_card_content, this)

        val addressTextView = findViewById<TextView>(R.id.address_text)
        val copyAddressButton = findViewById<MaterialButton>(R.id.copy_address)
        addressQRImageView = findViewById(R.id.address_qr)

        addressTextView.text = addressString
        copyAddressButton.setOnClickListener {
            ClipboardUtils.copyToClipboard(context, addressString)
            Toast.makeText(context, R.string.address_copied, Toast.LENGTH_SHORT).show()
        }

        this.setOnClickListener {
            addressQRShown = !addressQRShown
            setQRVisibility(addressQRShown)
        }
    }

    private fun setQRVisibility(visible: Boolean) {
       if (!visible) {
            addressQRImageView.setImageBitmap(null)
        } else {
            addressQRImageView.setImageBitmap(getAddressQRBitmap())
        }

        TransitionManager.beginDelayedTransition(rootView as ViewGroup)
    }

    private fun getAddressQRBitmap() : Bitmap {
        val hints: MutableMap<EncodeHintType, Any> = EnumMap(EncodeHintType::class.java)
        hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
        hints[EncodeHintType.MARGIN] = 0

        val bits = QRCodeWriter().encode(addressString, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE, hints)
        return Bitmap.createBitmap(QR_SIZE, QR_SIZE, Bitmap.Config.RGB_565).also {
            for (x in 0 until QR_SIZE) {
                for (y in 0 until QR_SIZE) {
                    it.setPixel(x, y, if (bits[x, y]) Color.BLACK else Color.WHITE)
                }
            }
        }
    }
}