package com.zlsadesign.speedometer

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.TextView
import java.util.*
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.Display




class SpeedometerActivity : AppCompatActivity() {

  val LOCATION_PERMISSION_CODE = 100
  var speed_manager: SpeedManager? = null

  var speed_padding: TextView? = null
  var speed_view: TextView? = null
  var speed_unit: TextView? = null
  var version_view: TextView? = null

  class SpeedUnit(val resource: Int, val factor: Double) {

  }


  val SPEED_UNIT_MPH = SpeedUnit(R.string.speed_unit_mph, 2.23694)
  val SPEED_UNIT_KMH = SpeedUnit(R.string.speed_unit_kmh, 3.6)
  val SPEED_UNIT_MS = SpeedUnit(R.string.speed_unit_ms, 1.0)
  val SPEED_UNIT_MACH = SpeedUnit(R.string.speed_unit_mach, 0.00291545)

  val UNITS: Array<SpeedUnit> = arrayOf(
      SPEED_UNIT_MPH,
      SPEED_UNIT_KMH,
      SPEED_UNIT_MS,
      SPEED_UNIT_MACH
  )

  var current_speed_unit: SpeedUnit = SpeedUnit(0, 0.0)
  var current_speed_unit_index: Int = 0

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_speedometer)

    this.speed_padding = this.findViewById(R.id.speed_padding) as TextView
    this.speed_view = this.findViewById(R.id.speed) as TextView
    this.speed_unit = this.findViewById(R.id.speed_units) as TextView
    this.version_view = this.findViewById(R.id.version) as TextView

    this.version_view?.text = BuildConfig.VERSION_NAME

    this.setSpeedUnitIndex(0)

    this.setSpeed(40.0)

    if (this.hasSpeedPermission()) {
      this.initSpeedManager()
    } else {
      ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_CODE);
    }
  }


  override fun onWindowFocusChanged(hasFocus: Boolean) {
    super.onWindowFocusChanged(hasFocus);
    val decorView = window.decorView

    if (hasFocus) {
      decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
          View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
          View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
          View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
          View.SYSTEM_UI_FLAG_FULLSCREEN or
          View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    }
  }

  override fun onPause() {
    super.onPause()

    this.speed_manager?.stop()
  }

  override fun onResume() {
    super.onResume()

    this.speed_manager?.start()
  }

  override fun onRequestPermissionsResult(code: Int, permissions: Array<String>, results: IntArray) {

    when(code) {
      LOCATION_PERMISSION_CODE -> {
        if(results.isNotEmpty() && results[0] == PackageManager.PERMISSION_GRANTED) {
          this.initSpeedManager()
        } else {
          finish()
        }
        return
      }
    }

  }

  fun cycleSpeedUnit(view: View) {
    cycleSpeedUnit()
  }

  fun cycleSpeedUnit() {
    this.setSpeedUnitIndex(this.current_speed_unit_index + 1)
  }

  fun setSpeedUnitIndex(index: Int) {
    this.current_speed_unit_index = index % UNITS.size
    this.current_speed_unit = UNITS.get(this.current_speed_unit_index)
  }

  fun hasSpeedPermission(): Boolean {
    return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
  }

  fun initSpeedManager() {
    val location_manager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    this.speed_manager = SpeedManager(location_manager, this)
  }

  fun setSpeed(meters_per_second: Double) {
    var total_width: Int = 5
    var speed_format = "%.0f"

    if(this.current_speed_unit == SPEED_UNIT_MACH) {
      speed_format = "%.3f"
    }

    this.speed_view?.text = speed_format.format(Math.abs(meters_per_second * this.current_speed_unit.factor))

    val chars = CharArray(total_width - this.speed_view?.text?.length as Int)
    Arrays.fill(chars, '0')

    this.speed_padding?.text = String(chars)

    this.speed_unit?.text = getResources().getString(this.current_speed_unit.resource)

    this.setFontSize()
  }

  fun setFontSize() {
    val display = windowManager.defaultDisplay
    val outMetrics = DisplayMetrics()
    display.getMetrics(outMetrics)

    val density = resources.displayMetrics.density
    val display_width = outMetrics.widthPixels / density

    val CHARACTERS = 5
    val CHARACTER_WIDTH = 0.6

    val CHARACTER_SIZE = (display_width / (CHARACTERS * CHARACTER_WIDTH)) * 0.8

    var size = CHARACTER_SIZE.toFloat()

    this.speed_view?.setTextSize(TypedValue.COMPLEX_UNIT_SP, size)
    this.speed_padding?.setTextSize(TypedValue.COMPLEX_UNIT_SP, size)
  }

}
