package com.danielml.materialwallet

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.danielml.materialwallet.Global.Companion.TAG
import com.danielml.materialwallet.barcode.BarcodeImageAnalyzer
import com.danielml.materialwallet.utils.DialogBuilder

class BarcodeScanActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_BARCODES = "barcodes"

        private const val PERMISSIONS_REQUEST_CODE = 0
        private val REQUIRED_PERMISSIONS = listOf(
            Manifest.permission.CAMERA
        ).toTypedArray()
    }

    @ExperimentalGetImage
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barcode_scan)

        // Verify the needed permissions and start the scan
        checkPermissionsAndStartScan()
    }

    @ExperimentalGetImage
    private fun checkPermissionsAndStartScan() {
        var missingPermissions = false

        // Request the camera permission
        for (permission in REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) == PackageManager.PERMISSION_DENIED
            ) {
                missingPermissions = true
                ActivityCompat.requestPermissions(
                    this,
                    REQUIRED_PERMISSIONS,
                    PERMISSIONS_REQUEST_CODE
                )
            }
        }

        if (!missingPermissions) {
            // Once all permissions are granted, start the scan
            startScan()
        }
    }

    @ExperimentalGetImage
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        var missingPermission = false
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE -> {
                for (result in grantResults) {
                    Log.e(TAG, result.toString())
                    if (result == PackageManager.PERMISSION_DENIED) {
                        missingPermission = true
                        showCameraPermissionNeededDialog()
                    }
                }

                if (!missingPermission && grantResults.isNotEmpty()) {
                    // If all permissions are allowed, start the scan
                    Log.e(TAG, "wtf")
                    startScan()
                }
            }
        }
    }

    private fun showCameraPermissionNeededDialog() {
        if (isFinishing) {
            return
        }

        DialogBuilder(this)
            .setTitle(R.string.camera_permission_title)
            .setMessage(R.string.camera_permission_message)
            .setOnPositiveButton { _, _ ->
                finish()
            }.buildDialog().show()
    }

    private fun showScanFailedDialog() {
        if (isFinishing) {
            return
        }

        DialogBuilder(this)
            .setTitle(R.string.failed_to_scan_title)
            .setMessage(R.string.failed_to_scan_message)
            .setOnPositiveButton { _, _ ->
                finish()
            }.buildDialog().show()
    }

    private fun handleException(exception: Exception) {
        showScanFailedDialog()
        Log.e(TAG, exception.message.toString())
    }

    @ExperimentalGetImage
    private fun startScan() {
        val previewView = findViewById<PreviewView>(R.id.preview_view)
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        // Initialize the imageCapture and preview use cases
        val imageAnalyzer = BarcodeImageAnalyzer(
            onScanSuccess = { barcodes ->
                setResult(
                    Activity.RESULT_OK,
                    Intent().putExtra(EXTRA_BARCODES, barcodes.toTypedArray())
                )
                finish()
            },
            // If the scan failed due to an exception, let the user know
            onScanFailed = { exception ->
                handleException(exception)
            },
        )
        val imageAnalysis = ImageAnalysis.Builder().build().apply {
            setAnalyzer(ContextCompat.getMainExecutor(applicationContext), imageAnalyzer)
        }
        val preview = Preview.Builder().build().apply {
            setSurfaceProvider(previewView.surfaceProvider)
        }

        cameraProviderFuture.addListener({
            // Assign the camera to the use cases
            cameraProviderFuture.get()
                .bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalysis)
        }, ContextCompat.getMainExecutor(this))
    }
}