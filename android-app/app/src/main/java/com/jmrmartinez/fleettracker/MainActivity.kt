package com.jmrmartinez.fleettracker

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    companion object {
        const val COMPANY_ID = "jmr-martinez"
        private const val PERMISSION_REQUEST_CODE = 100
    }

    private lateinit var truckIdInput: EditText
    private lateinit var driverInput: EditText
    private lateinit var toggleButton: Button
    private lateinit var statusText: TextView
    private var tracking = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        truckIdInput = findViewById(R.id.truckIdInput)
        driverInput = findViewById(R.id.driverInput)
        toggleButton = findViewById(R.id.toggleButton)
        statusText = findViewById(R.id.statusText)

        toggleButton.setOnClickListener {
            if (!tracking) startTracking() else stopTracking()
        }
    }

    private fun requiredPermissions(): Array<String> {
        val perms = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            perms.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        return perms.toTypedArray()
    }

    private fun hasPermissions(): Boolean =
        requiredPermissions().all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

    private fun startTracking() {
        if (!hasPermissions()) {
            ActivityCompat.requestPermissions(this, requiredPermissions(), PERMISSION_REQUEST_CODE)
            return
        }
        val truckId = truckIdInput.text.toString().trim().ifEmpty { "truck-01" }
        val driver = driverInput.text.toString().trim().ifEmpty { "Chofer" }

        val intent = Intent(this, TrackingService::class.java).apply {
            putExtra(TrackingService.EXTRA_TRUCK_ID, truckId)
            putExtra(TrackingService.EXTRA_DRIVER, driver)
        }
        ContextCompat.startForegroundService(this, intent)
        tracking = true
        toggleButton.text = getString(R.string.stop_tracking)
        statusText.text = getString(R.string.tracking_active, truckId)
    }

    private fun stopTracking() {
        stopService(Intent(this, TrackingService::class.java))
        tracking = false
        toggleButton.text = getString(R.string.start_tracking)
        statusText.text = getString(R.string.tracking_stopped)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE &&
            grantResults.isNotEmpty() &&
            grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        ) {
            startTracking()
        } else {
            statusText.text = getString(R.string.permission_needed)
        }
    }
}
