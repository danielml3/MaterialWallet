package com.danielml.materialwallet.fragments

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.danielml.materialwallet.Global
import com.danielml.materialwallet.R
import com.danielml.materialwallet.utils.ClipboardUtils
import com.google.android.material.button.MaterialButton
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import org.bitcoinj.core.Address

class ReceiveCoinsFragment : Fragment() {

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Global.allowBackPress = true
        Global.lastWalletBackStack = Global.RECEIVE_COINS_BACKSTACK
        return inflater.inflate(R.layout.receive_coins_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val walletKit = Global.globalWalletKit!!
        val usedAddressesContainer = view.findViewById<LinearLayout>(R.id.used_address_container)
        val usedAddressesTitle = view.findViewById<TextView>(R.id.used_address_title)
        val currentAddressContainer = view.findViewById<LinearLayout>(R.id.current_address_container)

        usedAddressesTitle.visibility = View.GONE
        Thread {
            for (address: Address in walletKit.wallet().issuedReceiveAddresses.reversed()) {
                val container = if (address.toString() == walletKit.wallet().currentReceiveAddress().toString()) {
                    currentAddressContainer
                } else {
                    handler.postAtFrontOfQueue {
                        usedAddressesTitle.visibility = View.VISIBLE
                    }
                    usedAddressesContainer
                }

                if (context == null) {
                    break
                }

                val addressString = address.toString()
                val addressCard = layoutInflater.inflate(R.layout.address_card, container, false)
                val addressTextView = addressCard.findViewById<TextView>(R.id.address_text)
                val copyAddressButton = addressCard.findViewById<MaterialButton>(R.id.copy_address)
                val addressQRImageView = addressCard.findViewById<ImageView>(R.id.address_qr)
                var addressQRShown = false

                addressTextView.text = addressString
                copyAddressButton.setOnClickListener {
                    ClipboardUtils.copyToClipboard(context!!, addressString)
                }

                addressCard.setOnClickListener {
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

                handler.post {
                    container.addView(addressCard)
                }
            }
        }.start()
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