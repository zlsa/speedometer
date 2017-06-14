package com.zlsadesign.speedometer

import android.annotation.SuppressLint
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.location.LocationListener
import android.os.Handler


class SpeedManager(val location_manager: LocationManager, val speedometer: SpeedometerActivity): LocationListener {

  val INTERVAL = 0.toLong()
  val DISTANCE = 0.toFloat()

  val SMOOTH_FACTOR = 0.8

  var last_measured_speed: Double = 0.0
  var smoothed_speed: Double = 0.0

  val handler: Handler = Handler()

  var running: Boolean = false

  val runnable = object : Runnable {
    override fun run() {
      smoothSpeed()

      if(running)
        handler.postDelayed(this, 50)
    }
  }

  init {
    Log.d("SpeedManager", "Initializing")

  }

  @SuppressLint("MissingPermission")
  fun start() {

    this.running = true

    this.withLocation(this.location_manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER))

    this.location_manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0.toLong(), 0.toFloat(), this)
    this.location_manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, INTERVAL, DISTANCE, this)

    this.handler.postDelayed(this.runnable, 50)

    Log.d("SpeedManager", "Starting")
  }

  fun stop() {
    this.running = false

    this.location_manager.removeUpdates(this)

    Log.d("SpeedManager", "Stopping")
  }

  override fun onLocationChanged(location: Location) {
    this.withLocation(location);
  }

  override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}

  override fun onProviderEnabled(provider: String) {}

  override fun onProviderDisabled(provider: String) {}

  fun withLocation(location: Location) {
    Log.d("SpeedManager", "${location.getSpeed()} m/s")


    this.last_measured_speed = location.getSpeed().toDouble()
  }

  fun smoothSpeed() {
    this.smoothed_speed = this.smoothed_speed * SMOOTH_FACTOR + this.last_measured_speed * (1 - SMOOTH_FACTOR)

    this.speedometer.setSpeed(this.smoothed_speed)
  }

}
