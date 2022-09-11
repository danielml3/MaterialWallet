package com.danielml.materialwallet.barcode

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.danielml.materialwallet.BarcodeScanActivity
import java.util.function.Consumer

class BarcodeScanActivityLauncher {
    private var resultLauncher: ActivityResultLauncher<Intent>? = null

    // This function must be executed on the onCreate lifecycle step of a fragment
    fun registerScanActivity(fragment: Fragment, onScanSuccess: Consumer<List<String>>) {
        resultLauncher =
            fragment.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val barcodes =
                        result.data?.getStringArrayExtra(BarcodeScanActivity.EXTRA_BARCODES)?.toList()
                            ?: listOf()
                    onScanSuccess.accept(barcodes)
                }
            }
    }

    // Run this function to start the scanner. The results will be delivered via the onScanSuccess consumer
    // on the registerScanActivity function
    fun startScanActivity(fragment: Fragment) {
        if (resultLauncher == null) {
            throw RuntimeException("registerStartActivity must be called before startScanActivity")
        }

        resultLauncher?.launch(Intent(fragment.context, BarcodeScanActivity::class.java))
    }
}