package com.danielml.materialwallet.layouts

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.danielml.materialwallet.R
import com.danielml.materialwallet.utils.ClipboardUtils
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import org.bitcoinj.core.Address

class AddressCard(context: Context, address: Address, root: ViewGroup) {
    val view: MaterialCardView

    init {
        view = (context as Activity).layoutInflater.inflate(R.layout.address_card, root, false) as MaterialCardView

        val addressString = address.toString()
        val addressTextView = view.findViewById<TextView>(R.id.address_text)
        val copyAddressButton = view.findViewById<MaterialButton>(R.id.copy_address)
        val addressQRImageView = view.findViewById<ImageView>(R.id.address_qr)
        var addressQRShown = false

        addressTextView.text = addressString
        copyAddressButton.setOnClickListener {
            ClipboardUtils.copyToClipboard(context, addressString)
        }

        view.setOnClickListener {
            addressQRImageView.visibility = if (addressQRShown) {
                addressQRImageView.setImageBitmap(null)
                addressQRShown = false
                View.GONE
            } else {
                addressQRImageView.setImageBitmap(getAddressQrBitmap(addressString))
                addressQRShown = true
                View.VISIBLE
            }
        }
    }

    private fun getAddressQrBitmap(address: String): Bitmap {
        val size = 512
        val bits = QRCodeWriter().encode(address, BarcodeFormat.QR_CODE, size, size)
        return Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565).also {
            for (x in 0 until size) {
                for (y in 0 until size) {
                    it.setPixel(x, y, if (bits[x, y]) Color.BLACK else Color.WHITE)
                }
            }
        }
    }
}