package com.danielml.materialwallet.barcode

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.function.Consumer

@ExperimentalGetImage
class BarcodeImageAnalyzer {
    companion object {
        fun analyze(
            imageProxy: ImageProxy,
            onScanSuccess: Consumer<List<String>>,
            onScanFailed: Consumer<Exception>,
            onBarcodeNotFound: Runnable
        ) {
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val image =
                    InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                val scanner = BarcodeScanning.getClient()

                scanner.process(image)
                    // If the scan succeeds, there are two cases:
                    // - No barcode was found, in whose case the onBarcodeNotFound will be executed
                    //   which should trigger a re-scan
                    // - There are barcodes, and the onScanSuccess will be executed in order to
                    //   deliver the scanned barcodes
                    .addOnSuccessListener { barcodes ->
                        if (barcodes.isEmpty()) {
                            onBarcodeNotFound.run()
                        } else {
                            val barcodeStrings = ArrayList<String>()
                            for (barcode in barcodes) {
                                val barcodeValue = barcode.rawValue
                                if (barcodeValue != null) {
                                    barcodeStrings.add(barcodeValue)
                                }
                            }

                            onScanSuccess.accept(barcodeStrings)
                        }
                    }

                    // If the scan fails due to an exception, the onScanFailed
                    // function will deliver the exception
                    .addOnFailureListener { exception ->
                        onScanFailed.accept(exception)
                    }
            }
        }
    }
}