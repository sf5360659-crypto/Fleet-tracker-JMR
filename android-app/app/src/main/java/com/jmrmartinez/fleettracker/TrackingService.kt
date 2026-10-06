package com.jmrmartinez.fleettracker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.database.FirebaseDatabase

class TrackingService : Service() {

    companion object {
        const val EXTRA_TRUCK_ID = "truck_id"
        const val EXTRA_DRIVER = "driver"
        private const val CHANNEL_ID = "tracking_channel"
        private const val NOTIFICATION_ID = 1
        private const val UPDATE_INTERVAL_MS = 15_000L
    }

    private lateinit var fusedClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val database =
        FirebaseDatabase.getInstance("https://fleet-tracker-jmr-a82c1-default-rtdb.firebaseio.com").reference
    private var truckId: String = "truck-01"
    private var driver: String = "Chofer"

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        fusedClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { sendLocation(it) }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        truckId = intent?.getStringExtra(EXTRA_TRUCK_ID) ?: "truck-01"
        driver = intent?.getStringExtra(EXTRA_DRIVER) ?: "Chofer"

        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID, notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        startLocationUpdates()
        return START_STICKY
    }

    private fun startLocationUpdates() {
        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, UPDATE_INTERVAL_MS
        ).setMinUpdateIntervalMillis(UPDATE_INTERVAL_MS).build()

        try {
            fusedClient.requestLocationUpdates(
                request, locationCallback, Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            stopSelf()
        }
    }

    private fun sendLocation(location: Location) {
        val data = mapOf(
            "lat" to location.latitude,
            "lng" to location.longitude,
            "accuracy" to location.accuracy.toDouble(),
            "speed" to location.speed.toDouble(), // m/s
            "driver" to driver,
            "updatedAt" to System.currentTimeMillis()
        )
        database
            .child("companies")
            .child(MainActivity.COMPANY_ID)
            .child("trucks")
            .child(truckId)
            .child("live")
            .setValue(data)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.channel_name),
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text, truckId))
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .build()

    override fun onDestroy() {
        fusedClient.removeLocationUpdates(locationCallback)
        super.onDestroy()
    }
}
