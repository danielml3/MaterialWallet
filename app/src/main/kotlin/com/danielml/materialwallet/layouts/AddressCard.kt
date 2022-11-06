package com.danielml.materialwallet.layouts

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
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


class AddressCard(context: Context, address: Address, isCurrentAddress: Boolean, root: ViewGroup) {
    private val view: MaterialCardView
    private var addressQRShown = false
    private val addressString = address.toString()
    private val addressQRImageView: ImageView

    private var addressQRBitmap: Bitmap? = null

    init {
        val layout = if (isCurrentAddress) {
            R.layout.current_address_card
        } else {
            R.layout.used_address_card
        }
        view = (context as Activity).layoutInflater.inflate(layout, root, false) as MaterialCardView

        val addressTextView = view.findViewById<TextView>(R.id.address_text)
        val copyAddressButton = view.findViewById<MaterialButton>(R.id.copy_address)
        addressQRImageView = view.findViewById<ImageView>(R.id.address_qr)

        addressTextView.text = addressString
        copyAddressButton.setOnClickListener {
            ClipboardUtils.copyToClipboard(context, addressString)
        }

        view.setOnClickListener {
            alternateQRVisibility()
        }
    }

    fun getView(): MaterialCardView {
        return view
    }

    private fun alternateQRVisibility() {
        addressQRImageView.visibility = if (addressQRShown) {
            addressQRImageView.setImageBitmap(null)
            addressQRShown = false
            View.GONE
        } else {
            addressQRImageView.setImageBitmap(getAddressQRBitmap())
            addressQRShown = true
            View.VISIBLE
        }

        TransitionManager.beginDelayedTransition(view)
    }

    private fun getAddressQRBitmap(): Bitmap {
        if (addressQRBitmap == null) {
            generateAddressQRBitmap()
        }

        return addressQRBitmap!!
    }

    private fun generateAddressQRBitmap() {
        val size = 512
        val hints: MutableMap<EncodeHintType, Any> = EnumMap(EncodeHintType::class.java)
        hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
        hints[EncodeHintType.MARGIN] = 0

        val bits = QRCodeWriter().encode(addressString, BarcodeFormat.QR_CODE, size, size, hints)
        addressQRBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565).also {
            for (x in 0 until size) {
                for (y in 0 until size) {
                    it.setPixel(x, y, if (bits[x, y]) Color.BLACK else Color.WHITE)
                }
            }
        }
    }
}