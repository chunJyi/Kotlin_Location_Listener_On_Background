package com.tc.location_background_service_kotlin

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*

class LocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private val channelId = "location_channel"
    private val notificationId = 1

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)
                for (location in result.locations) {
                    val lat = location.latitude
                    val lng = location.longitude
                    Log.d("LocationService", "Lat: $lat, Lng: $lng")

                    // TODO: Push to Supabase or store locally

                    // Update notification with current location
                    updateNotification(lat, lng)
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(notificationId, createNotification("Getting location..."))
        startLocationUpdates()
        return START_STICKY
    }

    private fun startLocationUpdates() {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000L).build()
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    private fun createNotification(contentText: String): Notification {
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Location Tracking Active")
            .setContentText(contentText)
            .setSmallIcon(com.google.android.gms.base.R.drawable.googleg_disabled_color_18) // Replace with your own icon
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(lat: Double, lng: Double) {
        val updatedNotification = createNotification("Lat: $lat, Lng: $lng")
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId, updatedNotification)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            channelId,
            "Location Service",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}
